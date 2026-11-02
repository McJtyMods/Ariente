package mcjty.ariente.display;

import mcjty.ariente.Ariente;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.api.components.IPanel;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.tileentity.TickingTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.UUID;
import java.util.function.Consumer;

public class DisplayBaseTile extends TickingTileEntity implements IGuiTile {

    public static final int MAX_HEIGHT = 10;
    public static final int MAX_MODULES = 32;
    public static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 1, 16);
    // Weak keys avoid retaining unloaded chunks or worlds. Used only for client ray targeting.
    public static final Set<DisplayBaseTile> LOADED = Collections.newSetFromMap(new WeakHashMap<>());

    private int height = 1;
    private long revision;
    private long layoutVersion = UUID.randomUUID().getLeastSignificantBits();
    private boolean reconcile = true;
    private DisplayAppearance appearance = DisplayAppearance.DEFAULT;
    private final List<InstalledModule> modules = new ArrayList<>();

    public DisplayBaseTile(BlockPos pos, BlockState state) {
        super(Registration.DISPLAY_BASE_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .properties(Block.Properties.of().mapColor(MapColor.METAL).strength(2).noOcclusion())
                .tileEntitySupplier(DisplayBaseTile::new)) {

            @Override
            public RotationType getRotationType() {
                return RotationType.HORIZROTATION;
            }

            @Override
            public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                return SHAPE;
            }
        };
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level.isClientSide) {
            LOADED.add(this);
        }
    }

    public int getHeight() {
        return height;
    }

    public DisplayBackground background() {
        return appearance.type();
    }

    public DisplayAppearance appearance() {
        return appearance;
    }

    public List<InstalledModule> modules() {
        return Collections.unmodifiableList(modules);
    }

    @Override
    public void onBlockPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide) {
            List<DisplayBaseTile> row = DisplayGeometry.row(this);
            int maxHeight = row.stream().mapToInt(DisplayBaseTile::getHeight).max().orElse(1);
            DisplayBaseTile existing = DisplayGeometry.neighbor(this, -1);
            if (existing == null) {
                existing = DisplayGeometry.neighbor(this, 1);
            }
            DisplayAppearance style = existing == null ? appearance : existing.appearance;
            long newest = row.stream().mapToLong(base -> base.revision).max().orElse(0);
            for (DisplayBaseTile base : row) {
                base.height = maxHeight;
                base.appearance = style;
                base.revision = newest;
                base.changed();
            }
        }
    }

    public void changed() {
        layoutVersion++;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    private void configure(Player player, int newHeight, DisplayAppearance newAppearance) {
        List<DisplayBaseTile> row = DisplayGeometry.row(this);
        for (DisplayBaseTile base : row) {
            if (base.modules.stream().anyMatch(module -> module.y() + module.size().height() > newHeight * 64 - 4)) {
                player.displayClientMessage(Component.literal("Remove modules below the new display height first."), true);
                return;
            }
        }
        long newest = Math.max(level.getGameTime(), row.stream().mapToLong(base -> base.revision).max().orElse(0) + 1);
        for (DisplayBaseTile base : row) {
            base.revision = newest;
            base.height = newHeight;
            base.appearance = newAppearance;
            base.changed();
        }
    }

    public boolean install(Player player, InteractionHand hand, DisplayGeometry.Hit hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof DisplayModuleItem item)
                || !DisplayGeometry.canPlace(this, item.type().size(held), hit.snapX(), hit.snapY())) {
            return false;
        }
        InstalledModule module = new InstalledModule(held, hit.snapX(), hit.snapY());
        module.refresh(level);
        modules.add(module);
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        changed();
        return true;
    }

    public void removeModule(Player player, InstalledModule module) {
        if (modules.remove(module)) {
            ItemStack stack = module.stack().copy();
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            changed();
        }
    }

    public InstalledModule findModule(UUID id) {
        return modules.stream().filter(module -> module.id().equals(id)).findFirst().orElse(null);
    }

    /** Move atomically: rejected drops leave both bases and the player's inventory untouched. */
    public boolean moveModule(Player player, InstalledModule module, DisplayBaseTile target, int x, int y) {
        if (!modules.contains(module) || isRemoved() || target == null || target.isRemoved() || player.isSpectator()
                || player.level() != level || !level.hasChunkAt(worldPosition) || level.getBlockEntity(worldPosition) != this
                || !level.hasChunkAt(target.getBlockPos()) || level.getBlockEntity(target.getBlockPos()) != target
                || !level.mayInteract(player, worldPosition) || !level.mayInteract(player, target.getBlockPos()) || !DisplayGeometry.sameScreen(this, target)
                || !DisplayGeometry.canPlace(target, module.size(), x, y, module)) {
            return false;
        }
        if (target == this && module.x() == x && module.y() == y) {
            return true;
        }
        InstalledModule replacement = module.moved(x, y);
        if (target == this) {
            modules.set(modules.indexOf(module), replacement);
        } else {
            modules.remove(module);
            target.modules.add(replacement);
            target.changed();
        }
        changed();
        return true;
    }

    public boolean configureModule(Player player, UUID id, Consumer<CompoundTag> edit) {
        InstalledModule module = findModule(id);
        if (module == null || isRemoved() || player.isSpectator() || player.level() != level
                || player.distanceToSqr(worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5) > 576
                || !level.mayInteract(player, worldPosition)) {
            return false;
        }
        ItemStack configured = module.stack().copy();
        edit.accept(DisplayModuleItem.config(configured));
        InstalledModule replacement = module.reconfigured(configured);
        boolean fits = DisplayGeometry.canPlace(this, replacement.size(), replacement.x(), replacement.y(), module);
        if (fits) {
            modules.set(modules.indexOf(module), replacement);
        } else {
            modules.remove(module);
            if (!player.getInventory().add(configured)) {
                player.drop(configured, false);
            }
            player.displayClientMessage(Component.literal("The module no longer fits and has been returned to you."), true);
        }
        changed();
        return fits;
    }

    @Override
    public void tickServer() {
        if (reconcile) {
            reconcile = false;
            List<DisplayBaseTile> row = DisplayGeometry.row(this);
            DisplayBaseTile latest = row.stream().max(Comparator.comparingLong(base -> base.revision)).orElse(this);
            int required = row.stream().flatMap(base -> base.modules.stream())
                    .mapToInt(module -> (module.y() + module.size().height() + 67) / 64).max().orElse(1);
            int commonHeight = Math.min(MAX_HEIGHT, Math.max(latest.height, required));
            for (DisplayBaseTile base : row) {
                base.reconcile = false;
                if (base.height != commonHeight || !base.appearance.equals(latest.appearance) || base.revision != latest.revision) {
                    base.height = commonHeight;
                    base.appearance = latest.appearance;
                    base.revision = latest.revision;
                    base.changed();
                }
            }
        }
        // Stagger samplers across bases. No capability reads or inventory scans on other ticks.
        if (modules.isEmpty() || Math.floorMod(level.getGameTime() + worldPosition.asLong(), 20) != 0) {
            return;
        }
        if (level.getNearestPlayer(worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5, 64, false) == null) {
            return;
        }
        List<PacketDisplayData.Entry> changes = new ArrayList<>();
        for (int index = 0; index < modules.size(); index++) {
            InstalledModule module = modules.get(index);
            if (module.refresh(level)) {
                changes.add(new PacketDisplayData.Entry(index, module.snapshot()));
            }
        }
        if (!changes.isEmpty()) {
            ArienteMessages.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> ((ServerLevel) level).getChunkAt(worldPosition)),
                    new PacketDisplayData(worldPosition, layoutVersion, List.copyOf(changes)));
        }
    }

    @Override
    public void onReplaced(Level level, BlockPos pos, BlockState state, BlockState replacement) {
        if (level.isClientSide || state.is(replacement.getBlock())
                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == replacement.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            return;
        }
        for (InstalledModule module : modules) {
            Containers.dropItemStack(level, pos.getX() + .5, pos.getY() + .2, pos.getZ() + .5, module.stack().copy());
        }
        modules.clear();
        // Return modules owned by a base to the left whose rectangle crosses this missing column.
        for (int offset = -1; offset >= -8; offset--) {
            DisplayBaseTile owner = DisplayGeometry.neighbor(this, offset);
            if (owner == null) {
                break;
            }
            int cut = -offset * 64;
            List<InstalledModule> crossed = owner.modules.stream()
                    .filter(module -> module.x() + module.size().width() > cut).toList();
            for (InstalledModule module : crossed) {
                Containers.dropItemStack(level, pos.getX() + .5, pos.getY() + .2, pos.getZ() + .5, module.stack().copy());
            }
            if (owner.modules.removeAll(crossed)) {
                owner.changed();
            }
        }
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult result) {
        Ariente.guiHandler.openHoloGui(level, worldPosition, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        if (ModuleSettings.isInstalledTag(tag)) {
            return ModuleSettings.createInstalled(this, tag, registry);
        }
        IPanel panel = HoloGuiTools.createPanelWithHelp(registry, new ResourceLocation(Ariente.MODID, "construction/displays"));
        if ("surface-color".equals(tag)) {
            panel.add(registry.text(.3, 0, 8, 1).text("SURFACE COLOR").scale(.65f).color(0x76eaff));
            panel.add(registry.button(6.4, .1, 1.6, .65).name("color_close").text("Back").scale(.4f)
                    .hitEvent((c, p, e, x, y) -> e.switchTag("default")));
            panel.add(registry.colorPicker(.4, 1, 6.8, 5.4).name("color_picker").color(() -> appearance.color())
                    .selected((p, e, rgb) -> {
                        configure(p, height, appearance.withColor(rgb));
                        e.switchTag("default");
                    }));
            panel.add(registry.text(.3, 7.4, 8, .5).text("Choose a hue, then select a shade.").scale(.38f));
            return panel;
        }
        panel.add(registry.text(.3, 0, 8, 1).text("HOLOGRAPHIC DISPLAY").scale(.65f).color(0x76eaff));
        panel.add(registry.integerSlider(.3, 1.5, 7.2, 1).name("display_height").label("Height")
                .range(1, MAX_HEIGHT).getter((p, e) -> height).changed((p, e, value) -> configure(p, value, appearance)));
        var choice = registry.textChoice(.3, 3, 7.2, .85).name("background").scale(.5f)
                .getter(p -> background().ordinal()).hitEvent((c, p, e, x, y) -> configure(p, height, appearance.withType(background().next())));
        for (DisplayBackground style : DisplayBackground.values()) {
            choice.addText("Surface: " + style.title());
        }
        panel.add(choice);
        panel.add(registry.button(.3, 4.15, 3.4, .85).name("surface_color").text("Color...").scale(.5f)
                .swatch(() -> appearance.color())
                .hitEvent((c, p, e, x, y) -> e.switchTag("surface-color")));
        panel.add(registry.textChoice(4.1, 4.15, 3.4, .85).name("surface_border").scale(.45f)
                .addText("Border: Off").addText("Border: On").getter(p -> appearance.border() ? 1 : 0)
                .hitEvent((c, p, e, x, y) -> configure(p, height, appearance.withBorder(!appearance.border()))));
        panel.add(registry.panel(.3, 5.35, 7.2, 1).visible(() -> background() == DisplayBackground.TRANSPARENT)
                .add(registry.integerSlider(.3, 5.35, 7.2, 1).name("surface_transparency").label("Transparency").suffix("%")
                        .range(0, 100).getter((p, e) -> appearance.transparency())
                        .changed((p, e, value) -> configure(p, height, appearance.withTransparency(value)))));
        panel.add(registry.text(.3, 6.8, 8, .5).text("Adjacent bases share these settings.").scale(.35f));
        panel.add(registry.text(.3, 7.4, 8, .5).text("Sneak + right click recovers a module.").scale(.32f));
        return panel;
    }

    @Override
    public void syncToClient() {
        // Configuration is sent on edits; live module data has its own one-second sampler.
    }

    public void applyReadings(PacketDisplayData packet) {
        if (packet.layoutVersion() == layoutVersion) {
            for (PacketDisplayData.Entry entry : packet.changes()) {
                if (entry.index() >= 0 && entry.index() < modules.size()) {
                    modules.get(entry.index()).updateSnapshot(entry.data());
                }
            }
        }
    }

    private void write(CompoundTag tag, boolean client) {
        if (client) {
            tag.putLong("Layout", layoutVersion);
        }
        tag.putInt("Height", height);
        tag.putLong("Revision", revision);
        appearance.write(tag);
        ListTag list = new ListTag();
        modules.forEach(module -> list.add(module.save(client)));
        tag.put("Modules", list);
    }

    private void read(CompoundTag tag) {
        if (tag.contains("Layout")) {
            layoutVersion = tag.getLong("Layout");
        }
        revision = tag.getLong("Revision");
        height = tag.contains("Height") ? Mth.clamp(tag.getInt("Height"), 1, MAX_HEIGHT) : 1;
        appearance = DisplayAppearance.read(tag);
        modules.clear();
        ListTag list = tag.getList("Modules", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(MAX_MODULES, list.size()); i++) {
            InstalledModule module = InstalledModule.load(list.getCompound(i));
            if (module != null) {
                modules.add(module);
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        write(tag, false);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        read(tag);
    }

    @Override
    public void saveClientDataToNBT(CompoundTag tag) {
        write(tag, true);
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tag) {
        read(tag);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(8, MAX_HEIGHT, 8);
    }
}
