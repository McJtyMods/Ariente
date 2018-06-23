package mcjty.ariente.blocks;

import mcjty.ariente.Ariente;
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
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MarbleBlock extends BaseBlock {

    public static final PropertyEnum<MarbleColor> COLOR = PropertyEnum.create("color", MarbleColor.class);

    public MarbleBlock(String name) {
        super(Ariente.instance, Material.ROCK, name, DamageMetadataItemBlock::new);
        setHardness(3.0f);
        setResistance(5.0f);
        setHarvestLevel("pickaxe", 2);
        setCreativeTab(Ariente.creativeTab);
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        for (MarbleColor color : MarbleColor.VALUES) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), color.ordinal(), new ModelResourceLocation(getRegistryName(), "color=" + color.getName()));
        }
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> tab) {
        for (MarbleColor color : MarbleColor.VALUES) {
            tab.add(new ItemStack(this, 1, color.ordinal()));
        }
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(this, 1, state.getValue(COLOR).ordinal());
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(COLOR).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(COLOR, MarbleColor.VALUES[meta]);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, COLOR);
    }

}
