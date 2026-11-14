package mcjty.ariente.blocks.utility;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import mcjty.ariente.Ariente;
import mcjty.ariente.api.IWarper;
import mcjty.ariente.blocks.ArienteBlockEntity;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.gui.FittedText;
import mcjty.ariente.teleport.WarperDestination;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.api.StyledColor;
import mcjty.hologui.api.components.IPanel;
import mcjty.hologui.gui.HoloGuiEntity;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.varia.PersistentData;
import mcjty.lib.varia.TeleportationTools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.ariente.setup.Registration.WARPER;
import static mcjty.ariente.setup.Registration.WARPER_TILE;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class WarperTile extends ArienteBlockEntity implements IGuiTile, IWarper {

    private AABB renderBox = null;

    public static final int DESTINATION_TIMEOUT = 30 * 20;
    public static final int MAX_NAME_LENGTH = 32;

    private int charges = 0;
    private String warperName = "";
    private WarperDestination destination;
    // Null denotes an older save that has not cached the destination name yet.
    private String destinationName;
    private CompoundTag destinationScene = new CompoundTag();
    private boolean destinationPinned;
    // -1 means no successful teleport since configuration; setting a card never starts a timer.
    private long lastTeleport = -1;
    private long nextDestinationAttempt;
    // Keep arrivals exempt until they leave the pad, including across chunk/save reloads.
    private final Set<UUID> arrivals = new HashSet<>();

    public WarperTile(BlockPos pos, BlockState state) {
        super(WARPER_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(
                new BlockBuilder()
                        .properties(
                                BlockBuilder.STANDARD_IRON.lightLevel(
                                        (light) -> {
                                            return 8;
                                        }))
                        .info(key("message.ariente.shiftmessage"))
                        .infoShift(header())
                        .topDriver(DRIVER)
                        .tileEntitySupplier(WarperTile::new)) {
            @Override
            public RotationType getRotationType() {
                return RotationType.NONE;
            }
        };
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult result) {
        if (refreshDestinationName(destinationName == null)) {
            markDirtyClient();
        }
        Ariente.guiHandler.openHoloGui(level, worldPosition, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setLevel(Level worldIn) {
        super.setLevel(worldIn);
        if (Ariente.setup.arienteWorld) {
            ResourceKey<Level> dim = ArienteWorldCompat.getArienteWorld().getDimension();
            if (worldIn != null && dim == worldIn.dimension()) {
                charges = UtilityConfiguration.WARPER_MAX_CHARGES.get();
            }
        }
    }

    private AABB getBeamBox() {
        if (renderBox == null) {
            renderBox = new AABB(getBlockPos()).minmax(new AABB(new BlockPos(worldPosition.getX(), worldPosition.getY() + 10, worldPosition.getZ())));
        }
        return renderBox;
    }

    public int getCharges() {
        return charges;
    }

    public void setCharges(int charges) {
        this.charges = charges;
        markDirtyClient();
    }

    @Override
    public void loadAdditional(CompoundTag tagCompound, HolderLookup.Provider provider) {
        CompoundTag info = tagCompound.getCompound("Info");
        if (info.contains("charges")) {
            charges = info.getInt("charges");
        }
        warperName = cleanName(info.getString("WarperName"));
        CompoundTag address = info.getCompound("Destination");
        destination = WarperDestination.load(address);
        destinationName = address.contains("Name", Tag.TAG_STRING) ? cleanName(address.getString("Name")) : null;
        destinationScene = info.getCompound("DestinationScene");
        destinationPinned = info.getBoolean("DestinationPinned");
        lastTeleport = info.contains("LastTeleport") ? info.getLong("LastTeleport") : -1;
        nextDestinationAttempt = 0;
        arrivals.clear();
        ListTag savedArrivals = info.getList("WarpArrivals", Tag.TAG_INT_ARRAY);
        for (int i = 0; i < savedArrivals.size(); i++) {
            if (savedArrivals.getIntArray(i).length == 4) {
                arrivals.add(NbtUtils.loadUUID(savedArrivals.get(i)));
            }
        }
        super.loadAdditional(tagCompound, provider);
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound, HolderLookup.Provider provider) {
        CompoundTag info = getOrCreateInfo(tagCompound);
        info.putInt("charges", charges);
        info.putString("WarperName", warperName);
        if (destination != null) {
            CompoundTag address = destination.save();
            if (destinationName != null) {
                address.putString("Name", destinationName);
            }
            info.put("Destination", address);
            info.put("DestinationScene", destinationScene);
            info.putBoolean("DestinationPinned", destinationPinned);
            info.putLong("LastTeleport", lastTeleport);
        }
        ListTag savedArrivals = new ListTag();
        arrivals.forEach(id -> savedArrivals.add(NbtUtils.createUUID(id)));
        info.put("WarpArrivals", savedArrivals);
        super.saveAdditional(tagCompound, provider);
    }

    @Override
    public void saveClientDataToNBT(CompoundTag tag, HolderLookup.Provider provider) {
        saveAdditional(tag, provider);
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
        loadAdditional(tag, provider);
    }

    public int getChargePercentage() {
        int pct;
        if (UtilityConfiguration.WARPER_MAX_CHARGES.get() <= 0) {
            pct = 100;
        } else {
            pct = charges * 100 / UtilityConfiguration.WARPER_MAX_CHARGES.get();
        }
        return pct;
    }

    public AABB getRenderBoundingBox() {
        return getBeamBox().expandTowards(0, -1, 0).inflate(0.05);
    }

    public void warp(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)
                || player.level() != level
                || !canDimensionWarp()) {
            return;
        }
        // A previously opened hologram must not bypass the server's current charge state.
        if (getChargePercentage() < 100) {
            return;
        }
        if (level.dimension().equals(Level.OVERWORLD)) {
            BlockPos destination =
                    ArienteWorldCompat.getArienteWorld()
                            .getNearestTeleportationSpot(player.blockPosition());
            // Remember the source entrance so the return trip works without a bed.
            PersistentData.get(player).putLong("arienteReturn", worldPosition.above().asLong());
            TeleportationTools.teleportToDimension(
                    player,
                    ArienteWorldCompat.getArienteWorld().getDimension(),
                    destination.getX() + .5,
                    destination.getY(),
                    destination.getZ() + .5);
        } else if (level.dimension().equals(ArienteWorldCompat.getArienteWorld().getDimension())) {
            ServerLevel overworld = serverPlayer.server.getLevel(Level.OVERWORLD);
            BlockPos destination = null;
            if (PersistentData.get(player).contains("arienteReturn")) {
                BlockPos source = BlockPos.of(PersistentData.get(player).getLong("arienteReturn"));
                overworld.getChunkAt(source);
                if (overworld.getBlockState(source.below()).is(WARPER.get())
                        && overworld.isEmptyBlock(source)
                        && overworld.isEmptyBlock(source.above())) {
                    destination = source;
                }
            }
            if (destination == null
                    && serverPlayer.getRespawnDimension().equals(Level.OVERWORLD)
                    && serverPlayer.getRespawnPosition() != null) {
                overworld.getChunkAt(serverPlayer.getRespawnPosition());
                var respawn =
                        serverPlayer.findRespawnPositionAndUseSpawnBlock(
                                true, DimensionTransition.DO_NOTHING);
                destination = BlockPos.containing(respawn.pos());
            }
            if (destination == null) {
                BlockPos spawn = overworld.getSharedSpawnPos();
                overworld.getChunkAt(spawn);
                destination =
                        overworld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn);
            }
            TeleportationTools.teleportToDimension(
                    player,
                    Level.OVERWORLD,
                    destination.getX() + .5,
                    destination.getY(),
                    destination.getZ() + .5);
        }
    }

    public String getWarperName() {
        return warperName;
    }

    private static String cleanName(String name) {
        StringBuilder clean = new StringBuilder();
        name.codePoints().map(c -> c == '\n' || c == '\r' || c == '\t' ? ' ' : c)
                .filter(c -> !Character.isISOControl(c) && c != 167)
                .limit(MAX_NAME_LENGTH).forEach(clean::appendCodePoint);
        return clean.toString().strip();
    }

    public void setWarperName(String name) {
        warperName = cleanName(name);
        markDirtyClient();
    }

    public WarperDestination getDestination() {
        return destination;
    }

    public String getDestinationDescription() {
        if (destination == null) {
            return "";
        }
        if (refreshDestinationName(destinationName == null)) {
            markDirtyClient();
        }
        return destinationName == null || destinationName.isBlank() ? destination.coordinates()
                : destinationName + " (" + destination.coordinates() + ")";
    }

    private boolean refreshDestinationName(boolean load) {
        if (destination == null || !(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        ServerLevel target = load ? destination.resolve(serverLevel.getServer()) : serverLevel.getServer().getLevel(destination.dimension());
        if (!load && (target == null || target.getChunkSource().getChunkNow(destination.pos().getX() >> 4, destination.pos().getZ() >> 4) == null)) {
            return false;
        }
        String name = target != null && target.getBlockEntity(destination.pos()) instanceof WarperTile warper
                ? warper.getWarperName() : destinationName == null ? "" : destinationName;
        if (!Objects.equals(destinationName, name)) {
            destinationName = name;
            return true;
        }
        return false;
    }

    public CompoundTag getDestinationScene() {
        return destinationScene;
    }

    public boolean isDestinationPinned() {
        return destinationPinned;
    }

    public void setDestination(WarperDestination destination, CompoundTag scene) {
        this.destination = destination;
        destinationName = null;
        refreshDestinationName(true);
        destinationScene = scene.copy();
        destinationPinned = false;
        lastTeleport = -1;
        nextDestinationAttempt = 0;
        markDirtyClient();
    }

    public void clearDestination() {
        destination = null;
        destinationName = null;
        destinationScene = new CompoundTag();
        destinationPinned = false;
        lastTeleport = -1;
        markDirtyClient();
    }

    public void setDestinationPinned(boolean pinned) {
        destinationPinned = pinned;
        markDirtyClient();
    }

    public boolean canDimensionWarp() {
        return level != null && Ariente.setup.arienteWorld && ArienteWorldCompat.getArienteWorld() != null
                && (level.dimension().equals(Level.OVERWORLD)
                || level.dimension().equals(ArienteWorldCompat.getArienteWorld().getDimension()));
    }

    public String getDimensionWarpTarget() {
        return level.dimension().equals(Level.OVERWORLD) ? "Ariente" : "Overworld";
    }

    private boolean canConfigure(Player player) {
        return !level.isClientSide && player.level() == level && player.distanceToSqr(worldPosition.getX() + .5,
                worldPosition.getY() + .5, worldPosition.getZ() + .5) <= 64 && level.getBlockEntity(worldPosition) == this;
    }

    private AABB teleportBox() {
        return new AABB(worldPosition).move(0, 1, 0).expandTowards(0, 1, 0);
    }

    @Override
    public void tickServer() {
        if (destination == null && arrivals.isEmpty()) {
            return;
        }
        long now = level.getGameTime();
        if (destination != null && !destinationPinned && lastTeleport >= 0 && now - lastTeleport >= DESTINATION_TIMEOUT) {
            clearDestination();
        }
        List<Entity> entities = level.getEntities((Entity) null, teleportBox(), entity -> !entity.isRemoved() && entity.isAlive()
                && !entity.isSpectator() && !(entity instanceof HoloGuiEntity));
        Set<UUID> present = new HashSet<>();
        entities.forEach(entity -> present.add(entity.getUUID()));
        if (arrivals.retainAll(present)) {
            setChanged();
        }
        if (destination == null || now < nextDestinationAttempt) {
            return;
        }
        entities.removeIf(entity -> arrivals.contains(entity.getUUID()) || entity.isPassenger() || entity.isVehicle());
        if (entities.isEmpty()) {
            return;
        }
        ServerLevel target = destination.resolve(level.getServer());
        if (target == null || !(target.getBlockEntity(destination.pos()) instanceof WarperTile receiver)
                || receiver == this) {
            nextDestinationAttempt = now + 20;
            return;
        }
        for (Entity entity : entities) {
            if (teleportEntity(entity, target, receiver)) {
                lastTeleport = now;
                setChanged();
            }
        }
    }

    private boolean teleportEntity(Entity entity, ServerLevel target, WarperTile receiver) {
        double x = destination.pos().getX() + .5, y = destination.pos().getY() + 1, z = destination.pos().getZ() + .5;
        AABB body = entity.getBoundingBox().move(x - entity.getX(), y - entity.getY(), z - entity.getZ());
        if (!target.getWorldBorder().isWithinBounds(body) || body.maxY > target.getMaxBuildHeight()
                || !target.noCollision(entity, body) || (target != level && !entity.canChangeDimensions(level, target))) {
            return false;
        }
        // Keep the arrival chunk tracking entities while an otherwise remote traveler is inserted.
        // Vanilla portal tickets expire automatically; no persistent force-loading is used.
        target.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(destination.pos()), 3, destination.pos());
        Entity arrived = entity.changeDimension(new DimensionTransition(target,
                new Vec3(x, y, z), Vec3.ZERO, entity.getYRot(), entity.getXRot(), DimensionTransition.DO_NOTHING));
        if (arrived == null || arrived.level() != target || arrived.distanceToSqr(x, y, z) > 1) {
            return false;
        }
        arrived.setDeltaMovement(0, 0, 0);
        arrived.fallDistance = 0;
        receiver.arrivals.add(arrived.getUUID());
        receiver.setChanged();
        return true;
    }

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        IPanel panel = registry.panel(0, 0, 8, 8)
                .add(registry.text(0, 0, 8, .8).text("Teleportation").scale(.7f).color(registry.color(StyledColor.LABEL)))
                .add(registry.text(.2, 1.35, 1.4, 1).text("Name").scale(.5f))
                .add(registry.textEntry(1.8, 1.2, 6, 1).name("warper_name").maxLength(MAX_NAME_LENGTH)
                        .text(this::getWarperName).changed((player, entity, text) -> {
                            if (canConfigure(player)) {
                                setWarperName(text);
                            }
                        }));
        // Dynamic controls also follow expiry and card configuration while this hologram stays open.
        panel.add(new FittedText(.4, 3, 7.2, .7, () -> destination == null ? "No card destination" : "Target: " + getDestinationDescription(), .4f)
                .name("destination_text"));
        panel.add(registry.text(.2, 3.7, 7.6, .6).text(() -> destination == null ? "" : destination.dimension().location().toString()).scale(.32f));
        IPanel destinationControls = registry.panel(0, 0, 8, 8).visible(() -> destination != null);
        destinationControls.add(registry.button(.2, 4.6, 3, .9).name("clear_destination").text("Clear").scale(.55f)
                .hitEvent((c, player, entity, x, y) -> {
                    if (canConfigure(player) && destination != null) {
                        clearDestination();
                    }
                }));
        destinationControls.add(registry.textChoice(4.2, 4.6, 3.5, .9).name("pin_destination").scale(.55f)
                .addText("Pin").addText("Unpin").getter(player -> destinationPinned ? 1 : 0)
                .hitEvent((c, player, entity, x, y) -> {
                    if (canConfigure(player) && destination != null) {
                        setDestinationPinned(!destinationPinned);
                    }
                }));
        destinationControls.add(registry.text(.2, 5.7, 7.6, .5).text(() -> destination == null || destinationPinned
                ? "" : "Clears 30s after last teleport").scale(.35f));
        panel.add(destinationControls);
        if (canDimensionWarp()) {
            panel.add(registry.text(.2, 6.4, 7.6, .5).text(() -> getChargePercentage() < 100
                    ? "Charged to " + getChargePercentage() + "% - Insufficient!" : "").scale(.35f));
            panel.add(registry.button(.2, 7.1, 6.5, .8).name("dimension_warp").text("Warp to " + getDimensionWarpTarget()).scale(.55f)
                    .hitEvent((c, player, entity, x, y) -> {
                        if (canConfigure(player)) {
                            warp(player);
                        }
                    }));
        }
        return panel;
    }

    @Override
    public void syncToClient() {
        if (refreshDestinationName(false)) {
            markDirtyClient();
        }
    }
}
