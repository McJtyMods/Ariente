package mcjty.ariente.cables;

import mcjty.ariente.blocks.generators.PowerCombinerTile;
import mcjty.ariente.power.IPowerReceiver;
import mcjty.ariente.power.IPowerSender;
import mcjty.ariente.setup.Registration;
import mcjty.lib.compat.theoneprobe.TOPDriver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConnectorBlock extends GenericCableBlock {

    public static final String CONNECTOR = "connector";

    public ConnectorBlock() {
        super(MapColor.METAL);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConnectorTileEntity(pos, state);
    }

    @Override
    public TOPDriver getProbeDriver() {
        // @todo 1.14
        return null;
    }

    @Override
    protected Item getItem(CableColor color) {
        switch (color) {
            case NEGARITE:
                return Registration.CONNECTOR_NEGARITE.get();
            case POSIRITE:
                return Registration.CONNECTOR_POSIRITE.get();
            case COMBINED:
                return Registration.CONNECTOR_COMBINED.get();
            case DATA:
                return Registration.CONNECTOR_DATA.get();
        };
        return Items.AIR;
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        checkRedstone(world, pos);
    }

    // @todo 1.18 @Override
    public boolean shouldCheckWeakPower(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        return false;
    }

    private void checkRedstone(Level world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ConnectorTileEntity) {
            int powered = world.getBestNeighborSignal(pos);
            ConnectorTileEntity genericTileEntity = (ConnectorTileEntity) te;
            genericTileEntity.setPowerInput(powered);
        }
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        return getRedstoneOutput(state, world, pos, side);
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        return getRedstoneOutput(state, world, pos, side);
    }

    protected int getRedstoneOutput(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        BlockEntity te = world.getBlockEntity(pos);
        if (state.getBlock() instanceof ConnectorBlock && te instanceof ConnectorTileEntity) {
            ConnectorTileEntity connector = (ConnectorTileEntity) te;
            return connector.getPowerOut(side.getOpposite());
        }
        return 0;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context);
    }

    @Override
    protected ConnectorType getConnectorType(@Nonnull CableColor color, BlockGetter world, BlockPos connectorPos, Direction facing) {
        BlockPos pos = connectorPos.relative(facing);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if ((block instanceof NetCableBlock || block instanceof ConnectorBlock) && state.getValue(COLOR) == color) {
            return ConnectorType.CABLE;
        } else if (isConnectable(world, connectorPos, facing)) {
            return ConnectorType.BLOCK;
        } else {
            return ConnectorType.NONE;
        }
    }

    public static boolean isConnectable(BlockGetter world, BlockPos connectorPos, Direction facing) {

        BlockPos pos = connectorPos.relative(facing);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (state.isAir()) {
            return false;
        }

        ConnectorTileEntity connectorTE = (ConnectorTileEntity) world.getBlockEntity(connectorPos);
        if (connectorTE == null) {
            return false;
        }

        BlockEntity te = world.getBlockEntity(pos);

        if (block instanceof ConnectorBlock) {
            return false;
        }
        if (te == null) {
            return false;
        }
        if (te instanceof IPowerSender) {
            return connectorTE.supportsCableColor(((IPowerSender) te).getSupportedColor());
        }
        if (te instanceof PowerCombinerTile) {
            return true;        // All connectors connect to the power combiner
        }
        if (te instanceof IPowerReceiver) {
            return true;
        }
        return false;
    }

}
