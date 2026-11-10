package mcjty.ariente.blocks.utility;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.IWarper;
import mcjty.ariente.blocks.ArienteBlockEntity;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.api.StyledColor;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.varia.PersistentData;
import mcjty.lib.varia.TeleportationTools;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

public class WarperTile extends ArienteBlockEntity implements IGuiTile, IWarper {

    private AABB renderBox = null;

    private int charges = 0;

    public WarperTile(BlockPos pos, BlockState state) {
        super(Registration.WARPER_TILE.get(), pos, state);
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
    public InteractionResult onBlockActivated(
            BlockState state, Player player, InteractionHand hand, BlockHitResult result) {
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
            renderBox =
                    new AABB(getBlockPos())
                            .minmax(
                                    new AABB(
                                            new BlockPos(
                                                    worldPosition.getX(),
                                                    worldPosition.getY() + 10,
                                                    worldPosition.getZ())));
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
        super.loadAdditional(tagCompound, provider);
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound, HolderLookup.Provider provider) {
        getOrCreateInfo(tagCompound).putInt("charges", charges);
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
        // The lowest animated beam starts one block below the warper.
        return getBeamBox().expandTowards(0, -1, 0).inflate(0.05);
    }

    public void warp(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)
                || player.level() != level
                || !Ariente.setup.arienteWorld) {
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
                if (overworld.getBlockState(source.below()).is(Registration.WARPER.get())
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

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        if (getChargePercentage() < 100) {
            return registry.panel(0, 0, 8, 8)
                    .add(
                            registry.text(0, 2, 1, 1)
                                    .text("Teleportation")
                                    .color(registry.color(StyledColor.LABEL)))
                    .add(
                            registry.text(0, 4, 1, 1)
                                    .text("Charged to " + getChargePercentage() + "%"))
                    .add(
                            registry.text(0, 5, 1, 1)
                                    .text("Insufficient!")
                                    .color(registry.color(StyledColor.ERROR)));
        } else {
            return registry.panel(0, 0, 8, 8)
                    .add(
                            registry.text(0, 2, 1, 1)
                                    .text("Teleportation")
                                    .color(registry.color(StyledColor.LABEL)))
                    .add(
                            registry.button(0, 4, 3, 1)
                                    .text("Warp")
                                    .hitEvent(
                                            (component, player, entity, x, y) -> {
                                                warp(player);
                                            }));
        }
    }

    @Override
    public void syncToClient() {}
}
