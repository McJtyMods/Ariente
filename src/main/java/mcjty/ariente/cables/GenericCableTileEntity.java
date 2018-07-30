package mcjty.ariente.cables;

import mcjty.ariente.facade.IFacadeSupport;
import mcjty.ariente.facade.MimicBlockSupport;
import mcjty.ariente.power.PowerSystem;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class GenericCableTileEntity extends GenericTileEntity implements IFacadeSupport {

    private MimicBlockSupport mimicBlockSupport = new MimicBlockSupport();

    private int cableId = -1;

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

    public CableColor getCableColor() {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof GenericCableBlock) {
            return state.getValue(GenericCableBlock.COLOR);
        }
        return CableColor.COMBINED;
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

    public void setCableId(int id) {
        if (cableId != id) {
            cableId = id;
            markDirtyQuick();
        }
    }

    public int getCableId() {
        return cableId;
    }

    private void fillCableId(int id) {
        if (cableId == id) {
            return; // Already ok
        }
        int toReplace = cableId;
        CableColor thisColor = getCableColor();
        setCableId(id);
        for (EnumFacing facing : EnumFacing.VALUES) {
            BlockPos p = pos.offset(facing);
            TileEntity te = world.getTileEntity(p);
            if (te instanceof GenericCableTileEntity && ((GenericCableTileEntity) te).getCableColor() == thisColor && ((GenericCableTileEntity) te).getCableId() == toReplace) {
                ((GenericCableTileEntity) te).fillCableId(id);
            }
        }
    }

    public static void fixNetworks(World world, BlockPos pos) {
        PowerSystem powerSystem = PowerSystem.getPowerSystem(world);

        // Fix all networks in a 3x3x3 centered around this block
        // First invalidate all of them
        Set<Integer> toRemove = new HashSet<>();
        for (int dx = -1 ; dx <= 1 ; dx++) {
            for (int dy = -1 ; dy <= 1 ; dy++) {
                for (int dz = -1 ; dz <= 1 ; dz++) {
                    BlockPos p = pos.add(dx, dy, dz);
                    TileEntity te = world.getTileEntity(p);
                    if (te instanceof GenericCableTileEntity) {
                        GenericCableTileEntity cableTileEntity = (GenericCableTileEntity) te;
                        int cableId = cableTileEntity.getCableId();
                        if (cableId != -1) {
                            cableTileEntity.fillCableId(-1);
                            toRemove.add(cableId);
                        }
                    }
                }
            }
        }

        // From the ids are about to remove we see which ones we can reuse for new networks
        for (int dx = -1 ; dx <= 1 ; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos p = pos.add(dx, dy, dz);
                    TileEntity te = world.getTileEntity(p);
                    if (te instanceof GenericCableTileEntity) {
                        GenericCableTileEntity cableTileEntity = (GenericCableTileEntity) te;
                        int cableId = cableTileEntity.getCableId();
                        if (cableId == -1) {
                            if (toRemove.isEmpty()) {
                                cableId = powerSystem.allocateId();
                            } else {
                                cableId = toRemove.iterator().next();
                                toRemove.remove(cableId);
                            }
                            cableTileEntity.fillCableId(cableId);
                        }
                    }
                }
            }
        }

        // Remove all remaining ids
        for (Integer id : toRemove) {
            powerSystem.removeId(id);
        }

        powerSystem.save();
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        mimicBlockSupport.readFromNBT(tagCompound);
        cableId = tagCompound.getInteger("cableId");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        mimicBlockSupport.writeToNBT(tagCompound);
        tagCompound.setInteger("cableId", cableId);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
    }
}
