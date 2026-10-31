package mcjty.ariente.cables;

import mcjty.lib.compat.theoneprobe.TOPDriver;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NetCableBlock extends GenericCableBlock {

    public static final String NETCABLE = "netcable";

    public NetCableBlock() {
        this(MapColor.WOOL);
    }

    public NetCableBlock(MapColor material) {
        super(material);
    }

    @Override
    public TOPDriver getProbeDriver() {
        return null;
    }

// @todo 1.14
//    @Override
//    public BlockState getExtendedState(BlockState state, IBlockReader world, BlockPos pos) {
//        IExtendedBlockState extendedBlockState = (IExtendedBlockState) super.getExtendedState(state, world, pos);
//        BlockState mimicBlock = getMimicBlock(world, pos);
//        if (mimicBlock != null) {
//            return extendedBlockState.with(FACADEID, new FacadeBlockId(mimicBlock));
//        } else {
//            return extendedBlockState;
//        }
//    }


    // @todo 1.14
//    @Override
//    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
//        for (CableColor value : CableColor.VALUES) {
//            items.add(updateColorInStack(new ItemStack(this, 1, value.ordinal()), value));
//        }
//    }

    // @todo 1.14
//    @Override
//    @SideOnly(Side.CLIENT)
//    public boolean shouldSideBeRendered(BlockState blockState, IBlockAccess blockAccess, BlockPos pos, Direction side) {
//        BlockState mimicBlock = getMimicBlock(blockAccess, pos);
//        if (mimicBlock == null) {
//            return false;
//        } else {
//            return mimicBlock.shouldSideBeRendered(blockAccess, pos, side);
//        }
//    }
//
//
//    @Override
//    @SideOnly(Side.CLIENT)
//    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
//        return true; // delegated to GenericCableBakedModel#getQuads
//    }


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
//        world.markBlockRangeForRenderUpdate(pos.add(-1, -1, -1), pos.add(1, 1, 1));
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
