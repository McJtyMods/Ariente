package mcjty.ariente.blocks;

import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.decorative.MarbleColor;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.DamageMetadataItemBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BaseVariantBlock<T extends Enum<T> & IStringSerializable> extends BaseBlock {

    public BaseVariantBlock(String name) {
        super(Ariente.instance, Material.ROCK, name, BaseVariantItemBlock::new);
        setHardness(2.0f);
        setResistance(4.0f);
        setHarvestLevel("pickaxe", 1);
        setCreativeTab(Ariente.setup.getTab());
    }

    abstract public PropertyEnum<T> getProperty();
    abstract public T[] getValues();

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }

    public void initModel() {
        for (T type : getValues()) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), type.ordinal(), new ModelResourceLocation(getRegistryName(), "type=" + type.getName()));
        }
    }

    public String getUnlocalizedName(int meta) {
        return super.getUnlocalizedName() + "." + getValues()[meta].getName();
    }


    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> tab) {
        for (T type : getValues()) {
            tab.add(new ItemStack(this, 1, type.ordinal()));
        }
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(this, 1, state.getValue(getProperty()).ordinal());
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(getProperty()).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(getProperty(), getValues()[meta]);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, getProperty());
    }


}
