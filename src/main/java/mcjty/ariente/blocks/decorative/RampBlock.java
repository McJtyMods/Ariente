package mcjty.ariente.blocks.decorative;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;

public class RampBlock extends BaseBlock {

    public static final VoxelShape AABB_NORTH1 = VoxelShapes.create(0.0D, 0.0D, 0.0D, 1.0D, 0.2D, 0.2D);
    public static final VoxelShape AABB_NORTH2 = VoxelShapes.create(0.0D, 0.2D, 0.2D, 1.0D, 0.4D, 0.4D);
    public static final VoxelShape AABB_NORTH3 = VoxelShapes.create(0.0D, 0.4D, 0.4D, 1.0D, 0.6D, 0.6D);
    public static final VoxelShape AABB_NORTH4 = VoxelShapes.create(0.0D, 0.6D, 0.6D, 1.0D, 0.8D, 0.8D);
    public static final VoxelShape AABB_NORTH5 = VoxelShapes.create(0.0D, 0.8D, 0.8D, 1.0D, 1.0D, 1.0D);
    public static final VoxelShape SHAPE_NORTH = VoxelShapes.or(AABB_NORTH1, AABB_NORTH2, AABB_NORTH3, AABB_NORTH4, AABB_NORTH5);

    public static final VoxelShape AABB_SOUTH1 = VoxelShapes.create(0.0D, 0.0D, 0.8D, 1.0D, 0.2D, 1.0D);
    public static final VoxelShape AABB_SOUTH2 = VoxelShapes.create(0.0D, 0.2D, 0.6D, 1.0D, 0.4D, 0.8D);
    public static final VoxelShape AABB_SOUTH3 = VoxelShapes.create(0.0D, 0.4D, 0.4D, 1.0D, 0.6D, 0.6D);
    public static final VoxelShape AABB_SOUTH4 = VoxelShapes.create(0.0D, 0.6D, 0.2D, 1.0D, 0.8D, 0.4D);
    public static final VoxelShape AABB_SOUTH5 = VoxelShapes.create(0.0D, 0.8D, 0.0D, 1.0D, 1.0D, 0.2D);
    public static final VoxelShape SHAPE_SOUTH = VoxelShapes.or(AABB_SOUTH1, AABB_SOUTH2, AABB_SOUTH3, AABB_SOUTH4, AABB_SOUTH5);

    public static final VoxelShape AABB_WEST1 = VoxelShapes.create(0.0D, 0.0D, 0.0D, 0.2D, 0.2D, 1.0D);
    public static final VoxelShape AABB_WEST2 = VoxelShapes.create(0.2D, 0.2D, 0.0D, 0.4D, 0.4D, 1.0D);
    public static final VoxelShape AABB_WEST3 = VoxelShapes.create(0.4D, 0.4D, 0.0D, 0.6D, 0.6D, 1.0D);
    public static final VoxelShape AABB_WEST4 = VoxelShapes.create(0.6D, 0.6D, 0.0D, 0.8D, 0.8D, 1.0D);
    public static final VoxelShape AABB_WEST5 = VoxelShapes.create(0.8D, 0.8D, 0.0D, 1.0D, 1.0D, 1.0D);
    public static final VoxelShape SHAPE_WEST = VoxelShapes.or(AABB_WEST1, AABB_WEST2, AABB_WEST3, AABB_WEST4, AABB_WEST5);

    public static final VoxelShape AABB_EAST1 = VoxelShapes.create(0.8D, 0.0D, 0.0D, 1.0D, 0.2D, 1.0D);
    public static final VoxelShape AABB_EAST2 = VoxelShapes.create(0.6D, 0.2D, 0.0D, 0.8D, 0.4D, 1.0D);
    public static final VoxelShape AABB_EAST3 = VoxelShapes.create(0.4D, 0.4D, 0.0D, 0.6D, 0.6D, 1.0D);
    public static final VoxelShape AABB_EAST4 = VoxelShapes.create(0.2D, 0.6D, 0.0D, 0.4D, 0.8D, 1.0D);
    public static final VoxelShape AABB_EAST5 = VoxelShapes.create(0.0D, 0.8D, 0.0D, 0.2D, 1.0D, 1.0D);
    public static final VoxelShape SHAPE_EAST = VoxelShapes.or(AABB_EAST1, AABB_EAST2, AABB_EAST3, AABB_EAST4, AABB_EAST5);

    public RampBlock() {
        super(new BlockBuilder()
                .properties(Properties.create(Material.ROCK).hardnessAndResistance(2.0f, 4.0f))
                .harvestLevel(ToolType.PICKAXE, 1)
        );
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.HORIZROTATION;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Direction direction = getFrontDirection(state);
        switch (direction) {
            case NORTH:
                return SHAPE_NORTH;
            case SOUTH:
                return SHAPE_SOUTH;
            case WEST:
                return SHAPE_WEST;
            case EAST:
                return SHAPE_EAST;
        }
        return VoxelShapes.empty();
    }
}
