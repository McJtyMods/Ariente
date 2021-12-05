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
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.storage.IWorldInfo;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class WarperTile extends GenericTileEntity implements IGuiTile, IWarper {

    private AxisAlignedBB renderBox = null;
    private AxisAlignedBB detectionBox = null;

    private int charges = 0;

    public WarperTile() {
        super(Registration.WARPER_TILE.get());
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
    public ActionResultType onBlockActivated(BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        Ariente.guiHandler.openHoloGui(level, worldPosition, player);
        return ActionResultType.SUCCESS;
    }


    @Override
    public void setLevelAndPosition(World worldIn, BlockPos pos) {
        super.setLevelAndPosition(worldIn, pos);
        if (Ariente.setup.arienteWorld) {
            RegistryKey<World> dim = ArienteWorldCompat.getArienteWorld().getDimension();
            if (worldIn != null && dim == worldIn.dimension()) {
                charges = UtilityConfiguration.WARPER_MAX_CHARGES.get();
            }
        }
    }

    private AxisAlignedBB getBeamBox() {
        if (renderBox == null) {
            renderBox = new AxisAlignedBB(getBlockPos()).minmax(new AxisAlignedBB(new BlockPos(worldPosition.getX(), worldPosition.getY() + 10, worldPosition.getZ())));
        }
        return renderBox;
    }

    private AxisAlignedBB getDetectionBox() {
        if (detectionBox == null) {
            detectionBox = new AxisAlignedBB(worldPosition.getX()-3, worldPosition.getY()-2, worldPosition.getZ()-3, worldPosition.getX()+4, worldPosition.getY()+6, worldPosition.getZ()+4);
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
    public void read(CompoundNBT tagCompound) {
        CompoundNBT info = tagCompound.getCompound("Info");
        if (info.contains("charges")) {
            charges = info.getInt("charges");
        }
        super.read(tagCompound);
    }

    @Override
    public CompoundNBT save(CompoundNBT tagCompound) {
        getOrCreateInfo(tagCompound).putInt("charges", charges);
        return super.save(tagCompound);
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
    public AxisAlignedBB getRenderBoundingBox() {
        return getBeamBox();
    }

    // @todo 1.15
//    @Override
//    public boolean shouldRenderInPass(int pass) {
//        return pass == 1;
//    }

    private void warp(PlayerEntity player) {
        if (level.dimension() == World.OVERWORLD) {
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
                    IWorldInfo worldInfo = level.getLevelData();
                    bedLocation = new BlockPos(worldInfo.getXSpawn(),worldInfo.getYSpawn(), worldInfo.getZSpawn());
                }
                while (!level.isEmptyBlock(bedLocation) && !level.isEmptyBlock(bedLocation.above()) && bedLocation.getY() < level.getMaxBuildHeight()-2) {
                    bedLocation = bedLocation.above();
                }
                TeleportationTools.teleportToDimension(player, World.OVERWORLD, bedLocation.getX(), bedLocation.getY(), bedLocation.getZ());
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
