package mcjty.ariente.blocks.decorative;

import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.lib.McJtyRegister;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

import static mcjty.ariente.blocks.decorative.MarbleBlock.COLOR;

public class MarbleSlabBlock extends BlockSlab {

    public MarbleSlabBlock(String name) {
        super(Material.ROCK);
        IBlockState iblockstate = this.blockState.getBaseState();
        this.setDefaultState(iblockstate.withProperty(COLOR, MarbleColor.BLACK));
        this.setCreativeTab(Ariente.setup.getTab());
        setUnlocalizedName(Ariente.MODID + "." + name);
        setRegistryName(name);
        McJtyRegister.registerLater(this, Ariente.instance, block -> new ItemSlab(block, ModBlocks.marbleSlabBlock, ModBlocks.doubleMarbleSlabBlock));
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        for (MarbleColor type : MarbleColor.VALUES) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), type.ordinal(), new ModelResourceLocation(getRegistryName(),
                    "half=bottom,type=" + type.getName()));
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(ModBlocks.marbleSlabBlock);
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(ModBlocks.marbleSlabBlock, 1, state.getValue(COLOR).ordinal());
    }


    @Override
    public String getUnlocalizedName(int meta) {
        return super.getUnlocalizedName() + "." + MarbleColor.VALUES[meta].getUnlocalizedName();
    }

    @Override
    public boolean isDouble() {
        return false;
    }

    @Override
    public IProperty<?> getVariantProperty() {
        return COLOR;
    }

    @Override
    public Comparable<?> getTypeForItem(ItemStack stack) {
        return MarbleColor.VALUES[stack.getMetadata() & 7];
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (MarbleColor color : MarbleColor.VALUES) {
            items.add(new ItemStack(this, 1, color.ordinal()));
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState iblockstate = this.getDefaultState().withProperty(COLOR, MarbleColor.VALUES[meta & 7]);

        if (!this.isDouble()) {
            iblockstate = iblockstate.withProperty(HALF, (meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
        }

        return iblockstate;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;
        i = i | state.getValue(COLOR).ordinal();

        if (!this.isDouble() && state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP) {
            i |= 8;
        }

        return i;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return this.isDouble() ? new BlockStateContainer(this, COLOR) : new BlockStateContainer(this, HALF, COLOR);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(COLOR).ordinal();
    }
}
