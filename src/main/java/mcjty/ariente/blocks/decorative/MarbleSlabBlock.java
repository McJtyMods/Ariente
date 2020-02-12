package mcjty.ariente.blocks.decorative;

import mcjty.ariente.api.MarbleColor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.material.Material;
import net.minecraft.state.StateContainer;

import static mcjty.ariente.api.MarbleColor.COLOR;
import static net.minecraft.state.properties.BlockStateProperties.HALF;

public class MarbleSlabBlock extends SlabBlock {

    public MarbleSlabBlock() {
        super(Properties.create(Material.ROCK));
        BlockState iblockstate = this.getDefaultState();
        this.setDefaultState(iblockstate.with(COLOR, MarbleColor.BLACK));
        // @todo 1.14
//        setRegistryName(name);
    }

    // @todo 1.14
//    @SideOnly(Side.CLIENT)
//    public void initModel() {
//        for (MarbleColor type : MarbleColor.VALUES) {
//            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), type.ordinal(), new ModelResourceLocation(getRegistryName(),
//                    "half=bottom,type=" + type.getName()));
//        }
//    }

    // @todo 1.14 loot
//    @Override
//    public Item getItemDropped(BlockState state, Random rand, int fortune) {
//        return Item.getItemFromBlock(ModBlocks.marbleSlabBlock);
//    }

    // @todo 1.14
//    @Override
//    public ItemStack getItem(World worldIn, BlockPos pos, BlockState state) {
//        return new ItemStack(ModBlocks.marbleSlabBlock, 1, state.getValue(COLOR).ordinal());
//    }


    // @todo 1.14
//    @Override
//    public String getUnlocalizedName(int meta) {
//        return super.getUnlocalizedName() + "." + MarbleColor.VALUES[meta].getUnlocalizedName();
//    }

    public boolean isDouble() {
        return false;
    }

//    @Override
//    public IProperty<?> getVariantProperty() {
//        return COLOR;
//    }

    // @todo 1.14
//    @Override
//    public Comparable<?> getTypeForItem(ItemStack stack) {
//        return MarbleColor.VALUES[stack.getMetadata() & 7];
//    }

    // @todo 1.14
//    @Override
//    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
//        for (MarbleColor color : MarbleColor.VALUES) {
//            items.add(new ItemStack(this, 1, color.ordinal()));
//        }
//    }

// @todo 1.14
//    @Nullable
//    @Override
//    public BlockState getStateForPlacement(BlockItemUseContext context) {
//        BlockState iblockstate = getDefaultState();
//
//        if (this.isDouble()) {
//            return iblockstate;
//        } else {
//            iblockstate = iblockstate.withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM);
//            return facing != Direction.DOWN && (facing == Direction.UP || (double)hitY <= 0.5D) ? iblockstate : iblockstate.withProperty(HALF, BlockSlab.EnumBlockHalf.TOP);
//        }
//    }

//    @Override
//    public BlockState getStateFromMeta(int meta) {
//        BlockState iblockstate = this.getDefaultState().withProperty(COLOR, MarbleColor.VALUES[meta & 7]);
//
//        if (!this.isDouble()) {
//            iblockstate = iblockstate.withProperty(HALF, (meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
//        }
//
//        return iblockstate;
//    }
//
//    @Override
//    public int getMetaFromState(BlockState state) {
//        int i = 0;
//        i = i | state.getValue(COLOR).ordinal();
//
//        if (!this.isDouble() && state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP) {
//            i |= 8;
//        }
//
//        return i;
//    }


    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        if (isDouble()) {
            builder.add(HALF).add(COLOR);
        } else {
            builder.add(COLOR);
        }
    }

    // @todo 1.14
//    @Override
//    public int damageDropped(BlockState state) {
//        return state.getValue(COLOR).ordinal();
//    }
}
