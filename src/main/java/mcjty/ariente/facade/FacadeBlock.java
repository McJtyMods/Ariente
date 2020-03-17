package mcjty.ariente.facade;

import mcjty.ariente.cables.CableColor;
import mcjty.ariente.cables.GenericCableTileEntity;
import mcjty.ariente.cables.NetCableBlock;
import mcjty.ariente.setup.Registration;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class FacadeBlock extends NetCableBlock {

    public static final String FACADE = "facade";

    public FacadeBlock() {
        super(Material.IRON);
        // @todo 1.14
//        setHardness(0.8f);
    }

    @Override
    public void harvestBlock(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
        ItemStack item = new ItemStack(Registration.FACADE.get());
        BlockState mimicBlock;
        if (te instanceof GenericCableTileEntity) {
            mimicBlock = ((GenericCableTileEntity) te).getMimicBlock();
        } else {
            mimicBlock = Blocks.COBBLESTONE.getDefaultState();
        }
        FacadeItemBlock.setMimicBlock(item, mimicBlock);

        spawnAsEntity(worldIn, pos, item);
    }


    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, IFluidState fluid) {
        CableColor color = state.get(COLOR);
        this.onBlockHarvested(world, pos, state, player);
        return world.setBlockState(pos, Registration.NETCABLE.get().getDefaultState().with(COLOR, color), world.isRemote ? 11 : 3);
    }


    // @todo 1.14
//    @Override
//    protected ItemBlock createItemBlock() {
//        return new FacadeItemBlock(this);
//    }

    // @todo 1.14
//    @Override
//    public void initModel() {
//        // To make sure that our ISBM model is chosen for all states we use this custom state mapper:
//        McJtyLib.proxy.initStateMapper(this, MODEL_RESOURCE_LOCATION);
//    }

// @todo 1.14
//    @Override
//    public void initItemModel() {
//        // For our item model we want to use a normal json model. This has to be called in
//        // ClientProxy.init (not preInit) so that's why it is a separate method.
//        Item itemBlock = ForgeRegistries.ITEMS.getValue(new ResourceLocation(Ariente.MODID, FACADE));
//        ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation(getRegistryName(), "inventory");
//        final int DEFAULT_ITEM_SUBTYPE = 0;
//        McJtyLib.proxy.initItemModelMesher(itemBlock, DEFAULT_ITEM_SUBTYPE, itemModelResourceLocation);
//    }
}
