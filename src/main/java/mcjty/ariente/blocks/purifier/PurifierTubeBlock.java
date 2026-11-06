package mcjty.ariente.blocks.purifier;

import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PurifierTubeBlock extends BaseBlock {

    private static final VoxelShape NORTH_SOUTH = Shapes.or(
            Block.box(6, 3, 0, 10, 13, 16),
            Block.box(4, 4, 0, 12, 12, 16),
            Block.box(3, 6, 0, 13, 10, 16));
    private static final VoxelShape EAST_WEST = Shapes.or(
            Block.box(0, 3, 6, 16, 13, 10),
            Block.box(0, 4, 4, 16, 12, 12),
            Block.box(0, 6, 3, 16, 10, 13));

    public PurifierTubeBlock() {
        super(new BlockBuilder().properties(Properties.of().strength(3).noOcclusion()).tileEntitySupplier(PurifierTubeTile::new));
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.Z));
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.HORIZONTAL_AXIS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clicked = context.getClickedFace();
        if (clicked.getAxis().isHorizontal() && connectsTo(context, clicked.getOpposite())) {
            return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_AXIS, clicked.getAxis());
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (connectsTo(context, direction)) {
                return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_AXIS, direction.getAxis());
            }
        }
        return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_AXIS, context.getHorizontalDirection().getAxis());
    }

    private boolean connectsTo(BlockPlaceContext context, Direction direction) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos().relative(direction));
        return state.is(Registration.PURIFIER_START.get()) || state.is(Registration.PURIFIER_FINISH.get())
                || PurifierStructure.isTube(state, direction);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90
                ? state.setValue(BlockStateProperties.HORIZONTAL_AXIS,
                state.getValue(BlockStateProperties.HORIZONTAL_AXIS) == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X) : state;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(BlockStateProperties.HORIZONTAL_AXIS) == Direction.Axis.X ? EAST_WEST : NORTH_SOUTH;
    }
}
