package mcjty.ariente.blocks.generators;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;

import static mcjty.ariente.blocks.BlockProperties.LOWER;
import static mcjty.ariente.blocks.BlockProperties.UPPER;

public class TankBlock extends BaseBlock {

    public TankBlock(BlockBuilder builder) {
        super(builder.properties(Properties.of().mapColor(MapColor.METAL).strength(2.0f)
                .requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion()
                .hasPostProcess((state, level, pos) -> true)));
        registerDefaultState(defaultBlockState().setValue(UPPER, false).setValue(LOWER, false));
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(UPPER, LOWER);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        return super.getStateForPlacement(context)
                .setValue(UPPER, context.getLevel().getBlockState(pos.above()).is(this))
                .setValue(LOWER, context.getLevel().getBlockState(pos.below()).is(this));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState adjacent, LevelAccessor level, BlockPos pos, BlockPos adjacentPos) {
        if (direction == Direction.UP) {
            return state.setValue(UPPER, adjacent.is(this));
        } else if (direction == Direction.DOWN) {
            return state.setValue(LOWER, adjacent.is(this));
        }
        return super.updateShape(state, direction, adjacent, level, pos, adjacentPos);
    }
}
