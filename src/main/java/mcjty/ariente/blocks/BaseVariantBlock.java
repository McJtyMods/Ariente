package mcjty.ariente.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.BlockGetter;

public abstract class BaseVariantBlock<T extends Enum<T> & StringRepresentable> extends BaseBlock {

    public BaseVariantBlock() {
        super(new BlockBuilder()
                .properties(Properties.of(Material.STONE)
                    .strength(2.0f, 4.0f))
                // @todo 1.18 .harvestLevel(ToolType.PICKAXE, 1)
        );
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
    public ItemStack getCloneItemStack(BlockGetter worldIn, BlockPos pos, BlockState state) {
        return new ItemStack(this, 1); // @todo 1.14 META?, state.get(getProperty()).ordinal());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(getProperty());
    }

}
