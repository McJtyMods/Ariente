package mcjty.ariente.blocks.purifier;

import mcjty.ariente.items.ItemData;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;
import java.util.List;
import javax.annotation.Nullable;
import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.ExtremeMachineTile;
import mcjty.ariente.config.PowerConfiguration;
import mcjty.ariente.gui.GuiHelp;
import mcjty.ariente.gui.PlayerInventoryPanel;
import mcjty.ariente.power.IPowerReceiver;
import mcjty.ariente.power.IPowerUser;
import mcjty.ariente.power.PowerReceiverSupport;
import mcjty.ariente.recipes.PurifierRecipes;
import mcjty.ariente.recipes.PurifyingRecipe;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.api.StyledColor;
import mcjty.hologui.api.components.IPlayerSlots;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.IItemHandler;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.hologui.api.Icons.GRAY_ARROW_DOWN;
import static mcjty.hologui.api.Icons.GRAY_ARROW_UP;
import static mcjty.hologui.api.Icons.WHITE_ARROW_DOWN;
import static mcjty.hologui.api.Icons.WHITE_ARROW_UP;
import static mcjty.lib.container.SlotDefinition.generic;

public class PurifierTile extends ExtremeMachineTile implements IGuiTile, IPowerReceiver, IPowerUser {

    private static final ContainerFactory CONTAINER = new ContainerFactory(1).slot(generic().in().out(), 0, 0, 0);

    private final GenericItemHandler items = new GenericItemHandler(this, CONTAINER) {

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && isStart() && findRecipe(stack) != null;
        }

        @Override
        protected void onUpdate(int index, ItemStack stack) {
            markDirtyClient();
        }
    };

    // This identifies work, rather than storing another item. The ore stays in the input
    // until input consumption and output delivery can be committed together on the server.
    private ItemStack processingOre = ItemStack.EMPTY;
    private int progress;
    private int tubeCount;
    private Direction direction = Direction.NORTH;
    private boolean running;
    private long usingPower;
    private String status = "structure";
    private List<BlockPos> shownTubes = List.of();

    public PurifierTile(BlockPos pos, BlockState state) {
        super(mcjty.ariente.setup.Registration.PURIFIER_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock(BlockBehaviour.Properties properties) {
        return new BaseBlock(new BlockBuilder().properties(properties.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL)).topDriver(DRIVER).tileEntitySupplier(PurifierTile::new)) {

            @Override
            public RotationType getRotationType() {
                return RotationType.NONE;
            }
        };
    }

    @Override
    public boolean acceptsExtremeCircuit() {
        return isStart();
    }

    public boolean isStart() {
        return getBlockState().is(mcjty.ariente.setup.Registration.PURIFIER_START.get());
    }

    public GenericItemHandler getItems() {
        return items;
    }

    public int getProgress() {
        return progress;
    }

    public static int yieldForTubes(int tubes) {
        return Math.min(64, PowerConfiguration.PURIFIER_BASE_YIELD.get()
                + Math.max(0, tubes - 1) * PowerConfiguration.PURIFIER_EXTRA_YIELD.get());
    }

    @Nullable
    public PurifyingRecipe findRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return null;
        }
        return PurifierRecipes.find(level, stack);
    }

    @Override
    public void tickServer() {
        if (!isStart()) {
            return;
        }
        boolean previousRunning = running;
        String previousStatus = status;
        running = false;
        usingPower = 0;
        PurifierStructure line = PurifierStructure.find(level, worldPosition);
        ItemStack input = items.getStackInSlot(0);
        PurifyingRecipe recipe = findRecipe(input);
        boolean changed = false;
        if (line == null) {
            status = "structure";
        } else {
            if (tubeCount != line.tubes().size() || direction != line.direction()) {
                progress = 0;
                tubeCount = line.tubes().size();
                direction = line.direction();
                changed = true;
            }
            if (!ItemStack.isSameItemSameComponents(input, processingOre)) {
                progress = 0;
                processingOre = input.isEmpty() ? ItemStack.EMPTY : input.copyWithCount(1);
                changed = true;
            }
            if (recipe == null) {
                progress = 0;
                status = "input";
            } else {
                ItemStack result = recipe.getResultItem(level.registryAccess()).copyWithCount(yieldForTubes(tubeCount));
                if (!line.finish().canAccept(result)) {
                    status = "output";
                } else {
                    long cost = powerCost((long) tubeCount * PowerConfiguration.PURIFIER_POWER_PER_TUBE.get());
                    if (!PowerReceiverSupport.consumePower(level, worldPosition, cost, true)) {
                        status = "power";
                    } else {
                        running = true;
                        usingPower = cost;
                        status = "working";
                        progress += operationSpeed();
                        int duration = tubeCount * PowerConfiguration.PURIFIER_TICKS_PER_TUBE.get();
                        while (progress >= duration && !items.getStackInSlot(0).isEmpty() && line.finish().canAccept(result)) {
                            ItemStack output = line.finish().items.getStackInSlot(0);
                            line.finish().items.setStackInSlot(0, result.copyWithCount(output.getCount() + result.getCount()));
                            items.extractItem(0, 1, false);
                            progress -= duration;
                            changed = true;
                        }
                        markDirtyQuick();
                    }
                }
            }
        }
        if (changed || previousRunning != running || !previousStatus.equals(status) || running && progress % 10 == 0) {
            updateVisuals(line, recipe);
            markDirtyClient();
        }
    }

    private boolean canAccept(ItemStack result) {
        ItemStack output = items.getStackInSlot(0);
        return !result.isEmpty() && (output.isEmpty() || ItemStack.isSameItemSameComponents(output, result))
                && output.getCount() + result.getCount() <= Math.min(items.getSlotLimit(0), result.getMaxStackSize());
    }

    private void updateVisuals(@Nullable PurifierStructure line, @Nullable PurifyingRecipe recipe) {
        List<BlockPos> next = line == null ? List.of() : line.tubes();
        for (BlockPos pos : shownTubes) {
            if (!next.contains(pos) && level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof PurifierTubeTile tube) {
                tube.clearVisuals();
            }
        }
        shownTubes = next;
        for (int index = 0; index < next.size(); index++) {
            if (level.getBlockEntity(next.get(index)) instanceof PurifierTubeTile tube) {
                ItemStack ore = recipe == null ? ItemStack.EMPTY : processingOre;
                ItemStack raw = recipe == null ? ItemStack.EMPTY : recipe.getResultItem(level.registryAccess()).copyWithCount(yieldForTubes(tubeCount));
                tube.setVisuals(worldPosition, direction, index, tubeCount, ore, raw, progress,
                        tubeCount * PowerConfiguration.PURIFIER_TICKS_PER_TUBE.get(), running);
            }
        }
    }

    @Override
    public void onReplaced(Level level, BlockPos pos, BlockState state, BlockState replacement) {
        if (!state.is(replacement.getBlock())) {
            updateVisuals(null, null);
        }
    }

    @Override
    public long getUsingPower() {
        return usingPower;
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult hit) {
        Ariente.guiHandler.openHoloGui(level, worldPosition, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        if (TAG_HELP.equals(tag)) {
            return GuiHelp.PURIFIER.createHelpGui(registry);
        }
        var panel = GuiHelp.PURIFIER.createPanel(registry)
                .add(registry.text(0, 0, 8, 1).text(Component.translatable(isStart() ? "block.ariente.purifier_start" : "block.ariente.purifier_finish").getString())
                        .color(registry.color(StyledColor.LABEL)))
                .add(new PurifierSlot(items).name("purifier_slot")
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> transferToPlayer(player)))
                .add(registry.text(0, 2.05, 8, .6).scale(.55f).text(() -> Component.translatable("message.ariente.purifier." + (isStart() ? status : "finish")).getString()))
                .add(registry.text(0, 2.65, 8, .6).scale(.5f).text(() -> isStart() && tubeCount > 0
                        ? Component.translatable("message.ariente.purifier.line", tubeCount, yieldForTubes(tubeCount),
                        tubeCount * PowerConfiguration.PURIFIER_TICKS_PER_TUBE.get() / 20.0, progress * 100 / Math.max(1, tubeCount * PowerConfiguration.PURIFIER_TICKS_PER_TUBE.get())).getString() : ""))
                .add(registry.iconButton(4, 3, .8, .8).icon(registry.image(GRAY_ARROW_DOWN)).hover(registry.image(WHITE_ARROW_DOWN))
                        .hitEvent((component, player, entity, x, y) -> transferToPlayer(player)))
                .add(new PlayerInventoryPanel(Component.translatable(isStart() ? "message.ariente.purifier.insert_hint" : "message.ariente.purifier.extract_hint").getString())
                        .name("playerslots").filter((stack, index) -> isStart() && findRecipe(stack) != null)
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> transferToMachine(player, index)));
        if (isStart()) {
            panel.add(registry.iconButton(3, 3, .8, .8).icon(registry.image(GRAY_ARROW_UP)).hover(registry.image(WHITE_ARROW_UP))
                    .hitEvent((component, player, entity, x, y) -> entity.findComponent("playerslots").ifPresent(selected -> {
                        if (selected instanceof IPlayerSlots slots) {
                            transferToMachine(player, slots.getSelected());
                        }
                    })));
        }
        return panel;
    }

    public void transferToMachine(Player player, int slot) {
        if (!isStart() || slot < 0 || slot >= 36 || !canInteract(player)) {
            return;
        }
        ItemStack input = player.getInventory().getItem(slot);
        player.getInventory().setItem(slot, items.insertItem(0, input, false));
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }

    public void transferToPlayer(Player player) {
        if (!canInteract(player)) {
            return;
        }
        ItemStack stored = items.getStackInSlot(0);
        if (!stored.isEmpty()) {
            ItemStack remaining = stored.copy();
            player.getInventory().add(remaining);
            items.setStackInSlot(0, remaining);
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
        }
    }

    private boolean canInteract(Player player) {
        return !level.isClientSide() && player.level() == level && !isRemoved()
                && player.distanceToSqr(worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5) <= 64;
    }

    @Override
    protected void saveInfo(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveInfo(tag, provider);
        CompoundTag info = getOrCreateInfo(tag);
        info.putInt("purifierProgress", progress);
        info.putInt("purifierTubes", tubeCount);
        info.putInt("purifierDirection", direction.get3DDataValue());
        info.put("purifierOre", ItemData.encode(provider, processingOre));
    }

    @Override
    protected void loadInfo(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadInfo(tag, provider);
        CompoundTag info = tag.getCompoundOrEmpty("Info");
        progress = Math.max(0, info.getIntOr("purifierProgress", 0));
        tubeCount = Math.max(0, info.getIntOr("purifierTubes", 0));
        direction = Direction.from3DDataValue(info.getIntOr("purifierDirection", 0));
        processingOre = ItemData.decode(provider, info.getCompoundOrEmpty("purifierOre"));
    }

    @Override
    public void saveClientDataToNBT(CompoundTag tag, HolderLookup.Provider provider) {
        tag.put("Items", items.serializeNBT(provider));
        tag.putInt("Progress", progress);
        tag.putInt("Tubes", tubeCount);
        tag.putString("Status", status);
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
        items.deserializeNBT(provider, tag.getCompoundOrEmpty("Items"));
        progress = tag.getIntOr("Progress", 0);
        tubeCount = tag.getIntOr("Tubes", 0);
        status = tag.getStringOr("Status", "");
    }

    @Override
    public void syncToClient() {
    }

    @Override
    public IItemHandler getAutomationHandler(Direction side) {
        return items;
    }

}
