package mcjty.ariente.blocks.utility;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.IWarper;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.api.StyledColor;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.TeleportationTools;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class WarperTile extends GenericTileEntity implements IGuiTile, IWarper {

    private AxisAlignedBB renderBox = null;
    private AxisAlignedBB detectionBox = null;

    private int charges = 0;

    @Override
    public void setWorld(World worldIn) {
        super.setWorld(worldIn);
        if (Ariente.setup.arienteWorld) {
            int dim = ArienteWorldCompat.getArienteWorld().getDimension();
            if (worldIn != null && worldIn.provider.getDimension() == dim) {
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
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        return super.writeToNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        charges = tagCompound.getInteger("charges");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("charges", charges);
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        int pct = getChargePercentage();
        probeInfo.text(TextStyleClass.LABEL + "Charged: " + TextStyleClass.INFO + pct + "%");
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

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.addWailaBody(itemStack, currenttip, accessor, config);
//        if (isWorking()) {
//            currenttip.add(TextFormatting.GREEN + "Producing " + getRfPerTick() + " RF/t");
//        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return getBeamBox();
    }


    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

    private void warp(PlayerEntity player) {
        if (world.provider.getDimension() == 0) {
            if (!world.isRemote) {
                if (Ariente.setup.arienteWorld) {
                    // @todo for future usage
                    BlockPos nearest = ArienteWorldCompat.getArienteWorld().getNearestTeleportationSpot(player.getPosition());
                    TeleportationTools.teleportToDimension(player, ArienteWorldCompat.getArienteWorld().getDimension(), nearest.getX(), nearest.getY(), nearest.getZ());
                }
            }
        } else {
            if (!world.isRemote) {
                BlockPos bedLocation = player.getBedLocation(0);
                if (bedLocation == null) {
                    bedLocation = world.getSpawnPoint();
                }
                while (!world.isAirBlock(bedLocation) && !world.isAirBlock(bedLocation.up()) && bedLocation.getY() < world.getHeight()-2) {
                    bedLocation = bedLocation.up();
                }
                TeleportationTools.teleportToDimension(player, 0, bedLocation.getX(), bedLocation.getY(), bedLocation.getZ());
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
