package mcjty.ariente.cables;

import net.minecraft.tileentity.TileEntityType;

public class NetCableTileEntity extends GenericCableTileEntity {

    @Override
    public boolean canSendPower() {
        return false;
    }

    public NetCableTileEntity(TileEntityType<?> type) {
        super(type);
    }
}
