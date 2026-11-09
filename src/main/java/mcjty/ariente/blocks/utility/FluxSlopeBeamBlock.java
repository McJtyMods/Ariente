package mcjty.ariente.blocks.utility;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Facing points toward the higher end, one block above the lower end. */
public class FluxSlopeBeamBlock extends BaseBeamBlock {

    private final VoxelShape[] slopes = new VoxelShape[4];

    public FluxSlopeBeamBlock(BlockBehaviour.Properties properties) {
        super(new BlockBuilder().properties(properties.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL)), false);
        VoxelShape shape = Shapes.empty();
        for (int step = 0; step < 16; step++) {
            shape = Shapes.or(shape, box(5, 15 - step, step, 11, 19 - step, step + 1));
        }
        for (int turn = 0; turn < slopes.length; turn++) {
            slopes[turn] = shape;
            VoxelShape[] rotated = {Shapes.empty()};
            shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    rotated[0] = Shapes.or(rotated[0], Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            shape = rotated[0];
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return slopes[switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> throw new IllegalArgumentException("Flux slopes must face horizontally");
        }];
    }
}
