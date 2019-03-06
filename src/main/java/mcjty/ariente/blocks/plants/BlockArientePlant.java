package mcjty.ariente.blocks.plants;

import mcjty.ariente.Ariente;
import mcjty.lib.McJtyLib;
import mcjty.lib.McJtyRegister;
import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BlockArientePlant extends BlockBush implements net.minecraftforge.common.IShearable {

    protected static final AxisAlignedBB BLACK_BUSH_AABB = new AxisAlignedBB(0.1, 0.0D, 0.1, 0.9, 0.8, 0.9);

    public BlockArientePlant(String name) {
        super(Material.VINE);
        setHardness(0.0F);
        setSoundType(SoundType.PLANT);
        setCreativeTab(Ariente.setup.getTab());

        setUnlocalizedName(Ariente.MODID + "." + name);
        setRegistryName(name);
        McJtyRegister.registerLater(this, Ariente.instance, ItemBlock::new);
    }

    public void initModel() {
        McJtyLib.proxy.initStandardItemModel(this);
    }


    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BLACK_BUSH_AABB;
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return MapColor.WOOD;
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        return state.getBlock() == Blocks.DIRT || state.getBlock() == Blocks.GRASS;
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public int quantityDropped(Random random) {
        return random.nextInt(3);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.STICK;
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
        if (!worldIn.isRemote && stack.getItem() == Items.SHEARS) {
            spawnAsEntity(worldIn, pos, new ItemStack(this, 1, 0));
        } else {
            super.harvestBlock(worldIn, player, pos, state, te, stack);
        }
    }

    @Override
    public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
        return Arrays.asList(new ItemStack(this));
    }
}