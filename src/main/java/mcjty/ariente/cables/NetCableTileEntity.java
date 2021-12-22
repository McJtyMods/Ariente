package mcjty.ariente.cables;

import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class NetCableTileEntity extends GenericCableTileEntity {

    @Override
    public boolean canSendPower() {
        return false;
    }

    public NetCableTileEntity(BlockPos pos, BlockState state) {
        super(Registration.NETCABLE_TILE.get(), pos, state);
    }
}
