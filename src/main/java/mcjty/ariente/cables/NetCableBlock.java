package mcjty.ariente.cables;

import net.minecraft.world.level.block.state.BlockBehaviour;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.compat.theoneprobe.TOPDriver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class NetCableBlock extends GenericCableBlock {

    public static final String NETCABLE = "netcable";

    public NetCableBlock(BlockBehaviour.Properties properties) {
        this(properties, MapColor.WOOL);
    }

    public NetCableBlock(BlockBehaviour.Properties properties, MapColor material) {
        super(properties, material);
    }

    @Override
    public TOPDriver getProbeDriver() {
        return null;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return getPlacementState(context);
    }

    public BlockState getPlacementState(BlockPlaceContext context) {
        // When our block is placed down we force a re-render of adjacent blocks to make sure their baked model is updated
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        // @todo 1.14
        world.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        return super.getStateForPlacement(context);
    }

    @Override
    protected ConnectorType getConnectorType(@Nonnull CableColor color, BlockGetter world, BlockPos connectorPos, Direction facing) {
        BlockPos pos = connectorPos.relative(facing);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if ((block instanceof NetCableBlock || block instanceof ConnectorBlock) && state.getValue(COLOR) == color) {
            return ConnectorType.CABLE;
        } else {
            return ConnectorType.NONE;
        }
    }

}
