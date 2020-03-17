package mcjty.ariente.cables;

import mcjty.ariente.setup.Registration;

public class NetCableTileEntity extends GenericCableTileEntity {

    @Override
    public boolean canSendPower() {
        return false;
    }

    public NetCableTileEntity() {
        super(Registration.NETCABLE_TILE.get());
    }
}
