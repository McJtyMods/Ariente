package mcjty.ariente.cables;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class ConnectorTileEntity extends GenericCableTileEntity {

    private int inputFromSide[] = new int[] { 0, 0, 0, 0, 0, 0 };
    private int powerOut[] = new int[] { 0, 0, 0, 0, 0, 0 };
    private Block[] cachedNeighbours = new Block[EnumFacing.VALUES.length];

    public int getPowerOut(EnumFacing side) {
        return powerOut[side.ordinal()];
    }

    public void setPowerOut(EnumFacing side, int powerOut) {
        if (powerOut > 15) {
            powerOut = 15;
        }
        if (this.powerOut[side.ordinal()] == powerOut) {
            return;
        }
        this.powerOut[side.ordinal()] = powerOut;
        markDirty();
        world.neighborChanged(pos.offset(side), this.getBlockType(), this.pos);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        inputFromSide = tagCompound.getIntArray("inputs");
        if (inputFromSide.length != 6) {
            inputFromSide = new int[] { 0, 0, 0, 0, 0, 0 };
        }
        for (int i = 0 ; i < 6 ; i++) {
            powerOut[i] = tagCompound.getByte("p" + i);
        }
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setIntArray("inputs", inputFromSide);
        for (int i = 0 ; i < 6 ; i++) {
            tagCompound.setByte("p" + i, (byte) powerOut[i]);
        }
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
    }
}
