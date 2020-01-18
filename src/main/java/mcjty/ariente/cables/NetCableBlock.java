package mcjty.ariente.cables;

import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.facade.FacadeBlockId;
import mcjty.ariente.facade.FacadeItemBlock;
import mcjty.lib.McJtyLib;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NetCableBlock extends GenericCableBlock implements ITileEntityProvider {

    public static final String NETCABLE = "netcable";

    public NetCableBlock() {
        this(Material.CLOTH, NETCABLE);
    }

    public NetCableBlock(Material material, String name) {
        super(material, name);
        initTileEntity();
    }

    protected void initTileEntity() {
        GameRegistry.registerTileEntity(NetCableTileEntity.class, new ResourceLocation(Ariente.MODID, "netcable"));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return null;
    }

    @Override
    public TileEntity createTileEntity(World world, BlockState metadata) {
        return new NetCableTileEntity();
    }


    @Override
    public void harvestBlock(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
        if (te instanceof NetCableTileEntity) {
            // If we are in mimic mode then the drop will be the facade as the connector will remain there
            NetCableTileEntity cableTileEntity = (NetCableTileEntity) te;
            if (cableTileEntity.getMimicBlock() != null) {
                ItemStack item = new ItemStack(ModBlocks.facadeBlock);
                FacadeItemBlock.setMimicBlock(item, cableTileEntity.getMimicBlock());
                cableTileEntity.setMimicBlock(null);
                spawnAsEntity(worldIn, pos, item);
                return;
            }
        }
        super.harvestBlock(worldIn, player, pos, state, te, stack);
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof NetCableTileEntity) {
            NetCableTileEntity cableTileEntity = (NetCableTileEntity) te;
            if (cableTileEntity.getMimicBlock() == null) {
                this.onBlockHarvested(world, pos, state, player);
                return world.setBlockState(pos, Blocks.AIR.getDefaultState(), world.isRemote ? 11 : 3);
            } else {
                // We are in mimic mode. Don't remove the connector
                this.onBlockHarvested(world, pos, state, player);
                if(player.capabilities.isCreativeMode) {
                    cableTileEntity.setMimicBlock(null);
                }
            }
        } else {
            return super.removedByPlayer(state, world, pos, player, willHarvest);
        }
        return true;
    }

    @Override
    public BlockState getExtendedState(BlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState extendedBlockState = (IExtendedBlockState) super.getExtendedState(state, world, pos);
        BlockState mimicBlock = getMimicBlock(world, pos);
        if (mimicBlock != null) {
            return extendedBlockState.withProperty(FACADEID, new FacadeBlockId(mimicBlock));
        } else {
            return extendedBlockState;
        }
    }


    @Override
    public void initModel() {
        super.initModel();
        // To make sure that our ISBM model is chosen for all states we use this custom state mapper:
        McJtyLib.proxy.initStateMapper(this, GenericCableBakedModel.modelCable);
        CableRenderer.register(NetCableTileEntity.class);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        for (CableColor value : CableColor.VALUES) {
            items.add(updateColorInStack(new ItemStack(this, 1, value.ordinal()), value));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(BlockState blockState, IBlockAccess blockAccess, BlockPos pos, Direction side) {
        BlockState mimicBlock = getMimicBlock(blockAccess, pos);
        if (mimicBlock == null) {
            return false;
        } else {
            return mimicBlock.shouldSideBeRendered(blockAccess, pos, side);
        }
    }


    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
        return true; // delegated to GenericCableBakedModel#getQuads
    }


    @Override
    public BlockState getStateForPlacement(World world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
        return getPlacementState(world, pos, facing, hitX, hitY, hitZ, meta, placer);

    }

    public BlockState getPlacementState(World world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
        // When our block is placed down we force a re-render of adjacent blocks to make sure their baked model is updated
        world.markBlockRangeForRenderUpdate(pos.add(-1, -1, -1), pos.add(1, 1, 1));
        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer);
    }

    @Override
    protected ConnectorType getConnectorType(@Nonnull CableColor color, IBlockAccess world, BlockPos connectorPos, Direction facing) {
        BlockPos pos = connectorPos.offset(facing);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if ((block instanceof NetCableBlock || block instanceof ConnectorBlock) && state.getValue(COLOR) == color) {
            return ConnectorType.CABLE;
        } else {
            return ConnectorType.NONE;
        }
    }

}
