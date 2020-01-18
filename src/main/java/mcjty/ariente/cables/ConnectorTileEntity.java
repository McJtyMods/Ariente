package mcjty.ariente.cables;

import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

public class ConnectorTileEntity extends GenericCableTileEntity {

    private int inputFromSide[] = new int[] { 0, 0, 0, 0, 0, 0 };
    private int powerOut[] = new int[] { 0, 0, 0, 0, 0, 0 };
    private Block[] cachedNeighbours = new Block[Direction.VALUES.length];

    public int getPowerOut(Direction side) {
        return powerOut[side.ordinal()];
    }

    public void setPowerOut(Direction side, int powerOut) {
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
    public boolean canSendPower() {
        return true;
    }

    public boolean supportsCableColor(CableColor color) {
        CableColor thisColor = getCableColor();
        if (thisColor.equals(color)) {
            return true;
        }
        return false;
    }

    @Override
    public void readFromNBT(CompoundNBT tagCompound) {
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
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        super.readRestorableFromNBT(tagCompound);
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setIntArray("inputs", inputFromSide);
        for (int i = 0 ; i < 6 ; i++) {
            tagCompound.setByte("p" + i, (byte) powerOut[i]);
        }
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        super.writeRestorableToNBT(tagCompound);
    }
}
