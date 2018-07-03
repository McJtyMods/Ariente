package mcjty.ariente.cables;

import mcjty.ariente.facade.IFacadeSupport;
import mcjty.ariente.facade.MimicBlockSupport;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;

public class GenericCableTileEntity extends GenericTileEntity implements IFacadeSupport {

    private MimicBlockSupport mimicBlockSupport = new MimicBlockSupport();

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        IBlockState oldMimicBlock = mimicBlockSupport.getMimicBlock();

        super.onDataPacket(net, packet);

        if (world.isRemote) {
            // If needed send a render update.
            if (mimicBlockSupport.getMimicBlock() != oldMimicBlock) {
                world.markBlockRangeForRenderUpdate(getPos(), getPos());
            }
        }
    }

    @Override
    public IBlockState getMimicBlock() {
        return mimicBlockSupport.getMimicBlock();
    }

    @Override
    public void setMimicBlock(IBlockState mimicBlock) {
        mimicBlockSupport.setMimicBlock(mimicBlock);
        markDirtyClient();
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        mimicBlockSupport.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        mimicBlockSupport.writeToNBT(tagCompound);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
    }
}
