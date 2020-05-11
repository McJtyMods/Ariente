package mcjty.ariente.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;

public abstract class BaseVariantBlock<T extends Enum<T> & IStringSerializable> extends BaseBlock {

    public BaseVariantBlock() {
        super(new BlockBuilder()
                .properties(Properties.create(Material.ROCK)
                    .hardnessAndResistance(2.0f, 4.0f))
                .harvestLevel(ToolType.PICKAXE, 1));
        // @todo 1.14
//        super(Ariente.instance, Material.ROCK, name, BaseVariantItemBlock::new);
    }

    public abstract EnumProperty<T> getProperty();
    public abstract T[] getValues();

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }

    // @todo 1.14
//    public void initModel() {
//        for (T type : getValues()) {
//            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), type.ordinal(), new ModelResourceLocation(getRegistryName(), "type=" + type.getName()));
//        }
//    }

    // @todo 1.14
//    public String getUnlocalizedName(int meta) {
//        return super.getUnlocalizedName() + "." + getValues()[meta].getName();
//    }

    // @todo 1.14
//    @Override
//    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> tab) {
//        for (T type : getValues()) {
//            tab.add(new ItemStack(this, 1, type.ordinal()));
//        }
//    }


    @SuppressWarnings({"NullableProblems", "deprecation"})
    @Override
    public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
        return new ItemStack(this, 1); // @todo 1.14 META?, state.get(getProperty()).ordinal());
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(getProperty());
    }

}
