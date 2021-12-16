package mcjty.ariente.blocks.utility.autofield;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.multipart.PartSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BaseNodeBlock extends BaseBlock {

    public BaseNodeBlock(BlockBuilder builder) {
        super(builder);
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AbstractNodeTile.ORIENTATION);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AbstractNodeTile.getVoxelShape(state, worldIn, pos);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockPos pos = context.getClickedPos();
        BlockEntity te = context.getLevel().getBlockEntity(pos);
//        if (te instanceof MultipartTE) {
            Vec3 hit = context.getClickLocation();
        return AbstractNodeTile.getStateForPlacement(this, context.getClickedFace(),
                hit.x - pos.getX(), hit.y - pos.getY(), hit.z - pos.getZ());
//        }
//        return super.getStateForPlacement(context);
    }

    @Nonnull
    @Override
    public PartSlot getSlotFromState(Level world, BlockPos pos, BlockState newState) {
        return newState.getValue(AbstractNodeTile.ORIENTATION).getSlot();
    }

}
