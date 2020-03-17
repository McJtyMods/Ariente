package mcjty.ariente.cables;

import mcjty.ariente.blocks.generators.PowerCombinerTile;
import mcjty.ariente.facade.FacadeItemBlock;
import mcjty.ariente.power.IPowerReceiver;
import mcjty.ariente.power.IPowerSender;
import mcjty.ariente.setup.Registration;
import mcjty.lib.compat.theoneprobe.TOPDriver;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConnectorBlock extends GenericCableBlock {

    public static final String CONNECTOR = "connector";

    public ConnectorBlock() {
        super(Material.IRON);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ConnectorTileEntity();
    }

    @Override
    public TOPDriver getProbeDriver() {
        // @todo 1.14
        return null;
    }

    // @todo 1.14
//    @Override
//    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
//        for (CableColor value : CableColor.VALUES) {
//            items.add(updateColorInStack(new ItemStack(this, 1, value.ordinal()), value));
//        }
//    }

//    @Override
//    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
//        if (!world.isRemote) {
//            player.openGui(XNet.instance, GuiProxy.GUI_CONNECTOR, world, pos.getX(), pos.getY(), pos.getZ());
//        }
//        return true;
//    }
//


    @Override
    public void harvestBlock(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
        if (te instanceof ConnectorTileEntity) {
            // If we are in mimic mode then the drop will be the facade as the connector will remain there
            ConnectorTileEntity connectorTileEntity = (ConnectorTileEntity) te;
            if (connectorTileEntity.getMimicBlock() != null) {
                ItemStack item = new ItemStack(Registration.FACADE.get());
                FacadeItemBlock.setMimicBlock(item, connectorTileEntity.getMimicBlock());
                connectorTileEntity.setMimicBlock(null);
                spawnAsEntity(worldIn, pos, item);
                return;
            }
        }
        super.harvestBlock(worldIn, player, pos, state, te, stack);
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, IFluidState fluid) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ConnectorTileEntity) {
            ConnectorTileEntity connectorTileEntity = (ConnectorTileEntity) te;
            if (connectorTileEntity.getMimicBlock() == null) {
                this.onBlockHarvested(world, pos, state, player);
                return world.setBlockState(pos, Blocks.AIR.getDefaultState(), world.isRemote ? 11 : 3);
            } else {
                // We are in mimic mode. Don't remove the connector
                this.onBlockHarvested(world, pos, state, player);
                if (player.abilities.isCreativeMode) {
                    connectorTileEntity.setMimicBlock(null);
                }
            }
        } else {
            return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
        }
        return true;
    }

// @todo 1.14
//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//    }
//
//
//    @Override
//    public BlockState getExtendedState(BlockState state, IBlockAccess world, BlockPos pos) {
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
//    public void initModel() {
//        super.initModel();
//        // To make sure that our ISBM model is chosen for all states we use this custom state mapper:
//        McJtyLib.proxy.initStateMapper(this, GenericCableBakedModel.modelConnector);
//        CableRenderer.register(ConnectorTileEntity.class);
//    }


    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        checkRedstone(world, pos);
    }

//    @Override
//    public void onNeighborChange(IBlockAccess blockAccess, BlockPos pos, BlockPos neighbor) {
//        if (blockAccess instanceof World) {
//            World world = (World) blockAccess;
//            if (!world.isRemote) {
//                TileEntity te = world.getTileEntity(pos);
//                if (te instanceof ConnectorTileEntity) {
//                    ConnectorTileEntity connector = (ConnectorTileEntity) te;
//                    connector.possiblyMarkNetworkDirty(neighbor);
//                }
//            }
//        }
//    }
//


    @Override
    public boolean shouldCheckWeakPower(BlockState state, IWorldReader world, BlockPos pos, Direction side) {
        return false;
    }

    private void checkRedstone(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ConnectorTileEntity) {
            int powered = world.getRedstonePowerFromNeighbors(pos);
            ConnectorTileEntity genericTileEntity = (ConnectorTileEntity) te;
            genericTileEntity.setPowerInput(powered);
        }
    }

    @Override
    public boolean canProvidePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        return getRedstoneOutput(state, world, pos, side);
    }

    @Override
    public int getStrongPower(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        return getRedstoneOutput(state, world, pos, side);
    }

    protected int getRedstoneOutput(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        TileEntity te = world.getTileEntity(pos);
        if (state.getBlock() instanceof ConnectorBlock && te instanceof ConnectorTileEntity) {
            ConnectorTileEntity connector = (ConnectorTileEntity) te;
            return connector.getPowerOut(side.getOpposite());
        }
        return 0;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        // When our block is placed down we force a re-render of adjacent blocks to make sure their ISBM model is updated
// @todo 1.14
//        world.markBlockRangeForRenderUpdate(pos.add(-1, -1, -1), pos.add(1, 1, 1));
        return super.getStateForPlacement(context);
    }

    @Override
    protected ConnectorType getConnectorType(@Nonnull CableColor color, IBlockReader world, BlockPos connectorPos, Direction facing) {
        BlockPos pos = connectorPos.offset(facing);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if ((block instanceof NetCableBlock || block instanceof ConnectorBlock) && state.get(COLOR) == color) {
            return ConnectorType.CABLE;
        } else if (isConnectable(world, connectorPos, facing)) {
            return ConnectorType.BLOCK;
        } else {
            return ConnectorType.NONE;
        }
    }

    public static boolean isConnectable(IBlockReader world, BlockPos connectorPos, Direction facing) {

        BlockPos pos = connectorPos.offset(facing);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block.isAir(state, world, pos)) {
            return false;
        }

        ConnectorTileEntity connectorTE = (ConnectorTileEntity) world.getTileEntity(connectorPos);
        if (connectorTE == null) {
            return false;
        }

        TileEntity te = world.getTileEntity(pos);

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

    // @todo 1.14
//    @Override
//    @SideOnly(Side.CLIENT)
//    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
//        return true; // delegated to GenericCableBakedModel#getQuads
//    }

    // @todo 1.14
//    @Override
//    public void getDrops(NonNullList<ItemStack> drops, IBlockReader blockAccess, BlockPos pos, BlockState state, int fortune) {
//        super.getDrops(drops, blockAccess, pos, state, fortune);
//        if (blockAccess instanceof World) {
//            World world = (World) blockAccess;
//            for (ItemStack drop : drops) {
//                if (!drop.hasTagCompound()) {
//                    drop.setTagCompound(new CompoundNBT());
//                }
//            }
//        }
//    }
}
