package mcjty.ariente.blocks.utility.autofield;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.multipart.PartSlot;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

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
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(AbstractNodeTile.ORIENTATION);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return AbstractNodeTile.getVoxelShape(state, worldIn, pos);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        TileEntity te = context.getWorld().getTileEntity(context.getPos());
        if (te instanceof SensorItemNodeTile) {
            Vec3d hit = context.getHitVec();
            return AbstractNodeTile.getStateForPlacement(this, context.getFace(), hit.x, hit.y, hit.z);
        }
        return super.getStateForPlacement(context);
    }

    @Nonnull
    @Override
    public PartSlot getSlotFromState(World world, BlockPos pos, BlockState newState) {
        return newState.get(AbstractNodeTile.ORIENTATION).getSlot();
    }

}
