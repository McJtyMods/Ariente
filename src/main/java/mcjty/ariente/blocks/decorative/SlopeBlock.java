package mcjty.ariente.blocks.decorative;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.EnumFacingUpDown;
import mcjty.lib.blocks.BaseBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SlopeBlock extends BaseBlock {

    private static final IProperty[] PROPERTIES = {EnumFacingUpDown.FACING};

    public SlopeBlock(String name) {
        super(Ariente.instance, Material.ROCK, name, ItemBlock::new);
        setHardness(2.0f);
        setResistance(4.0f);
        setHarvestLevel("pickaxe", 1);
        setCreativeTab(Ariente.setup.getTab());
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }

    @Override
    public boolean isTopSolid(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        EnumFacingUpDown facing = state.getValue(EnumFacingUpDown.FACING);
        switch (rot) {
            case NONE:
                return state;
            case CLOCKWISE_90:
                return state.withProperty(EnumFacingUpDown.FACING, facing.rotateY());
            case CLOCKWISE_180:
                return state.withProperty(EnumFacingUpDown.FACING, facing.rotateY().rotateY());
            case COUNTERCLOCKWISE_90:
                return state.withProperty(EnumFacingUpDown.FACING, facing.rotateY().rotateY().rotateY());
        }
        return state;
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        IBlockState state = super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
        EnumFacing dir = placer.getHorizontalFacing().getOpposite();
        EnumFacingUpDown updown = EnumFacingUpDown.VALUES[dir.ordinal()-2 + (facing == EnumFacing.DOWN ? 4 : 0)];
        return state.withProperty(EnumFacingUpDown.FACING, updown);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(EnumFacingUpDown.FACING, EnumFacingUpDown.VALUES[meta]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(EnumFacingUpDown.FACING).ordinal();
    }

    @Override
    protected IProperty<?>[] getProperties() {
        return PROPERTIES;
    }
}
