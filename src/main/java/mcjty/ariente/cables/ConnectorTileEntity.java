package mcjty.ariente.cables;

import mcjty.ariente.setup.Registration;
import mcjty.lib.varia.OrientationTools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.Direction;

public class ConnectorTileEntity extends GenericCableTileEntity {

    private int inputFromSide[] = new int[] { 0, 0, 0, 0, 0, 0 };
    private int powerOut[] = new int[] { 0, 0, 0, 0, 0, 0 };
    private Block[] cachedNeighbours = new Block[OrientationTools.DIRECTION_VALUES.length];

    public ConnectorTileEntity() {
        super(Registration.CONNECTOR_TILE.get());
    }

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
        setChanged();
        level.neighborChanged(worldPosition.relative(side), this.getBlockState().getBlock(), this.worldPosition); // @todo 1.14 is this right?
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
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        inputFromSide = tagCompound.getIntArray("inputs");
        if (inputFromSide.length != 6) {
            inputFromSide = new int[] { 0, 0, 0, 0, 0, 0 };
        }
        for (int i = 0 ; i < 6 ; i++) {
            powerOut[i] = tagCompound.getByte("p" + i);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound) {
        super.saveAdditional(tagCompound);
        tagCompound.putIntArray("inputs", inputFromSide);
        for (int i = 0 ; i < 6 ; i++) {
            tagCompound.putByte("p" + i, (byte) powerOut[i]);
        }
    }

}
