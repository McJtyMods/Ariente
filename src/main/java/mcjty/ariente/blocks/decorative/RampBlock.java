package mcjty.ariente.blocks.decorative;

import mcjty.ariente.Ariente;
import mcjty.lib.blocks.BaseBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class RampBlock extends BaseBlock {

    public static final AxisAlignedBB AABB_NORTH1 = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.2D, 0.2D);
    public static final AxisAlignedBB AABB_NORTH2 = new AxisAlignedBB(0.0D, 0.2D, 0.2D, 1.0D, 0.4D, 0.4D);
    public static final AxisAlignedBB AABB_NORTH3 = new AxisAlignedBB(0.0D, 0.4D, 0.4D, 1.0D, 0.6D, 0.6D);
    public static final AxisAlignedBB AABB_NORTH4 = new AxisAlignedBB(0.0D, 0.6D, 0.6D, 1.0D, 0.8D, 0.8D);
    public static final AxisAlignedBB AABB_NORTH5 = new AxisAlignedBB(0.0D, 0.8D, 0.8D, 1.0D, 1.0D, 1.0D);

    public static final AxisAlignedBB AABB_SOUTH1 = new AxisAlignedBB(0.0D, 0.0D, 0.8D, 1.0D, 0.2D, 1.0D);
    public static final AxisAlignedBB AABB_SOUTH2 = new AxisAlignedBB(0.0D, 0.2D, 0.6D, 1.0D, 0.4D, 0.8D);
    public static final AxisAlignedBB AABB_SOUTH3 = new AxisAlignedBB(0.0D, 0.4D, 0.4D, 1.0D, 0.6D, 0.6D);
    public static final AxisAlignedBB AABB_SOUTH4 = new AxisAlignedBB(0.0D, 0.6D, 0.2D, 1.0D, 0.8D, 0.4D);
    public static final AxisAlignedBB AABB_SOUTH5 = new AxisAlignedBB(0.0D, 0.8D, 0.0D, 1.0D, 1.0D, 0.2D);

    public static final AxisAlignedBB AABB_WEST1 = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.2D, 0.2D, 1.0D);
    public static final AxisAlignedBB AABB_WEST2 = new AxisAlignedBB(0.2D, 0.2D, 0.0D, 0.4D, 0.4D, 1.0D);
    public static final AxisAlignedBB AABB_WEST3 = new AxisAlignedBB(0.4D, 0.4D, 0.0D, 0.6D, 0.6D, 1.0D);
    public static final AxisAlignedBB AABB_WEST4 = new AxisAlignedBB(0.6D, 0.6D, 0.0D, 0.8D, 0.8D, 1.0D);
    public static final AxisAlignedBB AABB_WEST5 = new AxisAlignedBB(0.8D, 0.8D, 0.0D, 1.0D, 1.0D, 1.0D);

    public static final AxisAlignedBB AABB_EAST1 = new AxisAlignedBB(0.8D, 0.0D, 0.0D, 1.0D, 0.2D, 1.0D);
    public static final AxisAlignedBB AABB_EAST2 = new AxisAlignedBB(0.6D, 0.2D, 0.0D, 0.8D, 0.4D, 1.0D);
    public static final AxisAlignedBB AABB_EAST3 = new AxisAlignedBB(0.4D, 0.4D, 0.0D, 0.6D, 0.6D, 1.0D);
    public static final AxisAlignedBB AABB_EAST4 = new AxisAlignedBB(0.2D, 0.6D, 0.0D, 0.4D, 0.8D, 1.0D);
    public static final AxisAlignedBB AABB_EAST5 = new AxisAlignedBB(0.0D, 0.8D, 0.0D, 0.2D, 1.0D, 1.0D);


    public RampBlock(String name) {
        super(Ariente.instance, Material.ROCK, name, ItemBlock::new);
        setHardness(2.0f);
        setResistance(4.0f);
        setHarvestLevel("pickaxe", 1);
        setCreativeTab(Ariente.creativeTab);
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.HORIZROTATION;
    }

    public void handleAABB(IBlockState state, Consumer<AxisAlignedBB> consumer) {
        EnumFacing direction = getFrontDirection(state);
        switch (direction) {
            case NORTH:
                consumer.accept(AABB_NORTH1);
                consumer.accept(AABB_NORTH2);
                consumer.accept(AABB_NORTH3);
                consumer.accept(AABB_NORTH4);
                consumer.accept(AABB_NORTH5);
                break;
            case SOUTH:
                consumer.accept(AABB_SOUTH1);
                consumer.accept(AABB_SOUTH2);
                consumer.accept(AABB_SOUTH3);
                consumer.accept(AABB_SOUTH4);
                consumer.accept(AABB_SOUTH5);
                break;
            case WEST:
                consumer.accept(AABB_WEST1);
                consumer.accept(AABB_WEST2);
                consumer.accept(AABB_WEST3);
                consumer.accept(AABB_WEST4);
                consumer.accept(AABB_WEST5);
                break;
            case EAST:
                consumer.accept(AABB_EAST1);
                consumer.accept(AABB_EAST2);
                consumer.accept(AABB_EAST3);
                consumer.accept(AABB_EAST4);
                consumer.accept(AABB_EAST5);
                break;
            case DOWN:
            case UP:
                break;
        }
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        handleAABB(state, aabb -> addCollisionBoxToList(pos, entityBox, collidingBoxes, aabb));
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
        return super.getSelectedBoundingBox(state, worldIn, pos);
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
}
