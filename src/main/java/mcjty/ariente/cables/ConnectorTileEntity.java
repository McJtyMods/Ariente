package mcjty.ariente.cables;

import mcjty.ariente.facade.IFacadeSupport;
import mcjty.ariente.facade.MimicBlockSupport;
import mcjty.lib.bindings.DefaultValue;
import mcjty.lib.bindings.IValue;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;

public class ConnectorTileEntity extends GenericTileEntity implements IFacadeSupport {

    private MimicBlockSupport mimicBlockSupport = new MimicBlockSupport();

    private int inputFromSide[] = new int[] { 0, 0, 0, 0, 0, 0 };
    private String name = "";

    // Count the number of redstone pulses we got
    private int pulseCounter;
    private int powerOut[] = new int[] { 0, 0, 0, 0, 0, 0 };

    private byte enabled = 0x3f;

    private Block[] cachedNeighbours = new Block[EnumFacing.VALUES.length];

    public static final Key<String> VALUE_NAME = new Key<>("name", Type.STRING);

    @Override
    public IValue<?>[] getValues() {
        return new IValue[] {
                new DefaultValue<>(VALUE_NAME, this::getConnectorName, this::setConnectorName),
        };
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        IBlockState oldMimicBlock = mimicBlockSupport.getMimicBlock();
        byte oldEnabled = enabled;

        super.onDataPacket(net, packet);

        if (world.isRemote) {
            // If needed send a render update.
            if (enabled != oldEnabled || mimicBlockSupport.getMimicBlock() != oldMimicBlock) {
                world.markBlockRangeForRenderUpdate(getPos(), getPos());
            }
        }
    }

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

    public void setEnabled(EnumFacing direction, boolean e) {
        if (e) {
            enabled |= 1 << direction.ordinal();
        } else {
            enabled &= ~(1 << direction.ordinal());
        }
        markDirtyClient();
    }

    public boolean isEnabled(EnumFacing direction) {
        return (enabled & (1 << direction.ordinal())) != 0;
    }

    @Override
    public IBlockState getMimicBlock() {
        return mimicBlockSupport.getMimicBlock();
    }

    public void setMimicBlock(IBlockState mimicBlock) {
        mimicBlockSupport.setMimicBlock(mimicBlock);
        markDirtyClient();
    }

    @Override
    public void setPowerInput(int powered) {
        if (powerLevel == 0 && powered > 0) {
            pulseCounter++;
        }
        super.setPowerInput(powered);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        inputFromSide = tagCompound.getIntArray("inputs");
        if (inputFromSide.length != 6) {
            inputFromSide = new int[] { 0, 0, 0, 0, 0, 0 };
        }
        mimicBlockSupport.readFromNBT(tagCompound);
        pulseCounter = tagCompound.getInteger("pulse");
        for (int i = 0 ; i < 6 ; i++) {
            powerOut[i] = tagCompound.getByte("p" + i);
        }
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        name = tagCompound.getString("name");
        if (tagCompound.hasKey("enabled")) {
            enabled = tagCompound.getByte("enabled");
        } else {
            enabled = 0x3f;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setIntArray("inputs", inputFromSide);
        mimicBlockSupport.writeToNBT(tagCompound);
        tagCompound.setInteger("pulse", pulseCounter);
        for (int i = 0 ; i < 6 ; i++) {
            tagCompound.setByte("p" + i, (byte) powerOut[i]);
        }
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setString("name", name);
        tagCompound.setByte("enabled", enabled);
    }

    public void setConnectorName(String n) {
        this.name = n;
        markDirtyClient();
    }


    public String getConnectorName() {
        return name;
    }
}
