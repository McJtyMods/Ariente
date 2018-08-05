package mcjty.ariente.blocks.utility;

import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class DoorMarkerTile extends GenericTileEntity {

    public static final AxisAlignedBB BLOCK_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.1D, 1.0D);
    private AxisAlignedBB cachedBox = null;

    // Client side only
    private int opening;  // 0 is closed, 1000 is open
    private long lastTime = -1;  // For rendering

    // @todo: door should block entities

    private AxisAlignedBB getBeamBox() {
        if (cachedBox == null) {
            cachedBox = new AxisAlignedBB(getPos()).union(new AxisAlignedBB(new BlockPos(pos.getX(), pos.getY() + 10, pos.getZ())));
        }
        return cachedBox;
    }

    // Client side only
    public int getOpening() {
        return opening;
    }

    // Client side only
    public void setOpening(int opening) {
        this.opening = opening;
    }

    // Client side only
    public long getLastTime() {
        return lastTime;
    }

    // Client side only
    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
//        height = tagCompound.getInteger("height");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
//        tagCompound.setInteger("height", height);
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        Boolean working = isWorking();
//        if (working) {
//            probeInfo.text(TextFormatting.GREEN + "Producing " + getRfPerTick() + " RF/t");
//        }
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

}
