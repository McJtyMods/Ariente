package mcjty.ariente.blocks.utility;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.IWarper;
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
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.TeleportationTools;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.storage.LevelData;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class WarperTile extends GenericTileEntity implements IGuiTile, IWarper {

    private AABB renderBox = null;
    private AABB detectionBox = null;

    private int charges = 0;

    public WarperTile(BlockPos pos, BlockState state) {
        super(Registration.WARPER_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .properties(BlockBuilder.STANDARD_IRON.lightLevel((light) -> { return 8; }))
//                .flags(REDSTONE_CHECK, RENDER_SOLID, RENDER_CUTOUT)
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header())
                .topDriver(DRIVER)
                .tileEntitySupplier(WarperTile::new)
        ) {
            @Override
            public RotationType getRotationType() {
                return RotationType.NONE;
            }
        };
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult result) {
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

    private AABB getDetectionBox() {
        if (detectionBox == null) {
            detectionBox = new AABB(worldPosition.getX()-3, worldPosition.getY()-2, worldPosition.getZ()-3, worldPosition.getX()+4, worldPosition.getY()+6, worldPosition.getZ()+4);
        }
        return detectionBox;
    }

    public int getCharges() {
        return charges;
    }

    public void setCharges(int charges) {
        this.charges = charges;
        markDirtyClient();
    }

    @Override
    public void load(CompoundTag tagCompound) {
        CompoundTag info = tagCompound.getCompound("Info");
        if (info.contains("charges")) {
            charges = info.getInt("charges");
        }
        super.load(tagCompound);
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound) {
        getOrCreateInfo(tagCompound).putInt("charges", charges);
        super.saveAdditional(tagCompound);
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

    @Override
    public AABB getRenderBoundingBox() {
        return getBeamBox();
    }

    // @todo 1.15
//    @Override
//    public boolean shouldRenderInPass(int pass) {
//        return pass == 1;
//    }

    private void warp(Player player) {
        if (level.dimension() == Level.OVERWORLD) {
            if (!level.isClientSide) {
                if (Ariente.setup.arienteWorld) {
                    // @todo for future usage
                    BlockPos nearest = ArienteWorldCompat.getArienteWorld().getNearestTeleportationSpot(player.blockPosition());
                    TeleportationTools.teleportToDimension(player, ArienteWorldCompat.getArienteWorld().getDimension(), nearest.getX(), nearest.getY(), nearest.getZ());
                }
            }
        } else {
            if (!level.isClientSide) {
                BlockPos bedLocation = player.getSleepingPos().get();
                if (bedLocation == null) {
                    LevelData worldInfo = level.getLevelData();
                    bedLocation = new BlockPos(worldInfo.getXSpawn(),worldInfo.getYSpawn(), worldInfo.getZSpawn());
                }
                while (!level.isEmptyBlock(bedLocation) && !level.isEmptyBlock(bedLocation.above()) && bedLocation.getY() < level.getMaxBuildHeight()-2) {
                    bedLocation = bedLocation.above();
                }
                TeleportationTools.teleportToDimension(player, Level.OVERWORLD, bedLocation.getX(), bedLocation.getY(), bedLocation.getZ());
            }
        }
    }

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        if (getChargePercentage() < 100) {
            return registry.panel(0, 0, 8, 8)
                    .add(registry.text(0, 2, 1, 1).text("Teleportation").color(registry.color(StyledColor.LABEL)))
                    .add(registry.text(0, 4, 1, 1).text("Charged to " + getChargePercentage() + "%"))
                    .add(registry.text(0, 5, 1, 1).text("Insufficient!").color(registry.color(StyledColor.ERROR)));
        } else {
            return registry.panel(0, 0, 8, 8)
                    .add(registry.text(0, 2, 1, 1).text("Teleportation").color(registry.color(StyledColor.LABEL)))
                    .add(registry.button(0, 4, 3, 1).text("Warp").hitEvent((component, player, entity, x, y) -> {
                        warp(player);
                    }));
        }
    }

    @Override
    public void syncToClient() {

    }
}
