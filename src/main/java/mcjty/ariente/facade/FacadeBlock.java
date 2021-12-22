package mcjty.ariente.facade;

import mcjty.ariente.cables.CableColor;
import mcjty.ariente.cables.GenericCableTileEntity;
import mcjty.ariente.cables.NetCableBlock;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;

import javax.annotation.Nullable;

public class FacadeBlock extends NetCableBlock {

    public static final String FACADE = "facade";

    public FacadeBlock() {
        super(Material.METAL);
        // @todo 1.14
//        setHardness(0.8f);
    }

    @Override
    public void playerDestroy(Level worldIn, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        ItemStack item = new ItemStack(Registration.FACADE.get());
        BlockState mimicBlock;
        if (te instanceof GenericCableTileEntity) {
            mimicBlock = ((GenericCableTileEntity) te).getMimicBlock();
        } else {
            mimicBlock = Blocks.COBBLESTONE.defaultBlockState();
        }
        FacadeItemBlock.setMimicBlock(item, mimicBlock);

        popResource(worldIn, pos, item);
    }


    @Override
    public boolean removedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        CableColor color = state.getValue(COLOR);
        this.playerWillDestroy(world, pos, state, player);
        return world.setBlock(pos, Registration.NETCABLE.get().defaultBlockState().setValue(COLOR, color), world.isClientSide ? 11 : 3);
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
