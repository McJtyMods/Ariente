package mcjty.ariente.blocks.utility;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.IWarper;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.blocks.defense.ForceFieldTile;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.config.UtilityConfiguration;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class WarperTile extends GenericTileEntity implements IGuiTile, IWarper {

    private AxisAlignedBB renderBox = null;
    private AxisAlignedBB detectionBox = null;

    private int charges = 0;

    public WarperTile() {
        super(ModBlocks.WARPER_TILE.get());
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .properties(BlockBuilder.STANDARD_IRON.lightValue(8))
//                .flags(REDSTONE_CHECK, RENDER_SOLID, RENDER_CUTOUT)
                .info("message.ariente.shiftmessage")
                .infoExtended("message.ariente.warper")
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
        Ariente.guiHandler.openHoloGui(world, pos, player);
        return ActionResultType.SUCCESS;
    }


    @Override
    public void setWorldAndPos(World worldIn, BlockPos pos) {
        super.setWorldAndPos(worldIn, pos);
        if (Ariente.setup.arienteWorld) {
            DimensionType dim = ArienteWorldCompat.getArienteWorld().getDimension();
            if (worldIn != null && worldIn.getDimension().getType() == dim) {
                charges = UtilityConfiguration.WARPER_MAX_CHARGES.get();
            }
        }
    }

    private AxisAlignedBB getBeamBox() {
        if (renderBox == null) {
            renderBox = new AxisAlignedBB(getPos()).union(new AxisAlignedBB(new BlockPos(pos.getX(), pos.getY() + 10, pos.getZ())));
        }
        return renderBox;
    }

    private AxisAlignedBB getDetectionBox() {
        if (detectionBox == null) {
            detectionBox = new AxisAlignedBB(pos.getX()-3, pos.getY()-2, pos.getZ()-3, pos.getX()+4, pos.getY()+6, pos.getZ()+4);
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
        readRestorableFromNBT(tagCompound);
        super.read(tagCompound);
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        writeRestorableToNBT(tagCompound);
        return super.write(tagCompound);
    }

    // @todo 1.14 loot
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        charges = tagCompound.getInt("charges");
    }

    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        tagCompound.putInt("charges", charges);
    }

    // @todo 1.14
//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        int pct = getChargePercentage();
//        probeInfo.text(TextStyleClass.LABEL + "Charged: " + TextStyleClass.INFO + pct + "%");
//    }
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    @Optional.Method(modid = "waila")
//    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        super.addWailaBody(itemStack, currenttip, accessor, config);
////        if (isWorking()) {
////            currenttip.add(TextFormatting.GREEN + "Producing " + getRfPerTick() + " RF/t");
////        }
//    }

    public int getChargePercentage() {
        int pct;
        if (UtilityConfiguration.WARPER_MAX_CHARGES.get() <= 0) {
            pct = 100;
        } else {
            pct = charges * 100 / UtilityConfiguration.WARPER_MAX_CHARGES.get();
        }
        return pct;
    }

    // @todo 1.14
//    @SideOnly(Side.CLIENT)
//    @Override
//    public AxisAlignedBB getRenderBoundingBox() {
//        return getBeamBox();
//    }
//
//    @Override
//    public boolean shouldRenderInPass(int pass) {
//        return pass == 1;
//    }

    private void warp(PlayerEntity player) {
        if (world.getDimension().getType() == DimensionType.OVERWORLD) {
            if (!world.isRemote) {
                if (Ariente.setup.arienteWorld) {
                    // @todo for future usage
                    BlockPos nearest = ArienteWorldCompat.getArienteWorld().getNearestTeleportationSpot(player.getPosition());
                    TeleportationTools.teleportToDimension(player, ArienteWorldCompat.getArienteWorld().getDimension(), nearest.getX(), nearest.getY(), nearest.getZ());
                }
            }
        } else {
            if (!world.isRemote) {
                BlockPos bedLocation = player.getBedLocation(DimensionType.OVERWORLD);
                if (bedLocation == null) {
                    bedLocation = world.getSpawnPoint();
                }
                while (!world.isAirBlock(bedLocation) && !world.isAirBlock(bedLocation.up()) && bedLocation.getY() < world.getHeight()-2) {
                    bedLocation = bedLocation.up();
                }
                TeleportationTools.teleportToDimension(player, DimensionType.OVERWORLD, bedLocation.getX(), bedLocation.getY(), bedLocation.getZ());
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
