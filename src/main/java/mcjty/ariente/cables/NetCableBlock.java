package mcjty.ariente.cables;

import mcjty.ariente.facade.FacadeItemBlock;
import mcjty.ariente.setup.Registration;
import mcjty.lib.compat.theoneprobe.TOPDriver;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NetCableBlock extends GenericCableBlock {

    public static final String NETCABLE = "netcable";

    public NetCableBlock() {
        this(Material.CLOTH_DECORATION);
    }

    public NetCableBlock(Material material) {
        super(material);
    }

    @Override
    public TOPDriver getProbeDriver() {
        return null;
    }

    @Override
    public void playerDestroy(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
        if (te instanceof NetCableTileEntity) {
            // If we are in mimic mode then the drop will be the facade as the connector will remain there
            NetCableTileEntity cableTileEntity = (NetCableTileEntity) te;
            if (cableTileEntity.getMimicBlock() != null) {
                ItemStack item = new ItemStack(Registration.FACADE.get());
                FacadeItemBlock.setMimicBlock(item, cableTileEntity.getMimicBlock());
                cableTileEntity.setMimicBlock(null);
                popResource(worldIn, pos, item);
                return;
            }
        }
        super.playerDestroy(worldIn, player, pos, state, te, stack);
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof NetCableTileEntity) {
            NetCableTileEntity cableTileEntity = (NetCableTileEntity) te;
            if (cableTileEntity.getMimicBlock() == null) {
                this.playerWillDestroy(world, pos, state, player);
                return world.setBlock(pos, Blocks.AIR.defaultBlockState(), world.isClientSide ? 11 : 3);
            } else {
                // We are in mimic mode. Don't remove the connector
                this.playerWillDestroy(world, pos, state, player);
                if (player.abilities.instabuild) {
                    cableTileEntity.setMimicBlock(null);
                }
            }
        } else {
            return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
        }
        return true;
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
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getPlacementState(context);

    }

    public BlockState getPlacementState(BlockItemUseContext context) {
        // When our block is placed down we force a re-render of adjacent blocks to make sure their baked model is updated
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        // @todo 1.14
        world.sendBlockUpdated(pos, state, state, Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
//        world.markBlockRangeForRenderUpdate(pos.add(-1, -1, -1), pos.add(1, 1, 1));
        return super.getStateForPlacement(context);
    }

    @Override
    protected ConnectorType getConnectorType(@Nonnull CableColor color, IBlockReader world, BlockPos connectorPos, Direction facing) {
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
