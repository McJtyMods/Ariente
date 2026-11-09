package mcjty.ariente.cables;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class NetCableTileEntity extends GenericCableTileEntity {

    @Override
    public boolean canSendPower() {
        return false;
    }

    public NetCableTileEntity(BlockPos pos, BlockState state) {
        super(mcjty.ariente.setup.Registration.NETCABLE_TILE.get(), pos, state);
    }
}
