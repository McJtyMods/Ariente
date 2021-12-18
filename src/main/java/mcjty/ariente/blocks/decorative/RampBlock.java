package mcjty.ariente.blocks.decorative;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

public class RampBlock extends BaseBlock {

    public static final VoxelShape AABB_NORTH1 = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.2D, 0.2D);
    public static final VoxelShape AABB_NORTH2 = Shapes.box(0.0D, 0.2D, 0.2D, 1.0D, 0.4D, 0.4D);
    public static final VoxelShape AABB_NORTH3 = Shapes.box(0.0D, 0.4D, 0.4D, 1.0D, 0.6D, 0.6D);
    public static final VoxelShape AABB_NORTH4 = Shapes.box(0.0D, 0.6D, 0.6D, 1.0D, 0.8D, 0.8D);
    public static final VoxelShape AABB_NORTH5 = Shapes.box(0.0D, 0.8D, 0.8D, 1.0D, 1.0D, 1.0D);
    public static final VoxelShape SHAPE_NORTH = Shapes.or(AABB_NORTH1, AABB_NORTH2, AABB_NORTH3, AABB_NORTH4, AABB_NORTH5);

    public static final VoxelShape AABB_SOUTH1 = Shapes.box(0.0D, 0.0D, 0.8D, 1.0D, 0.2D, 1.0D);
    public static final VoxelShape AABB_SOUTH2 = Shapes.box(0.0D, 0.2D, 0.6D, 1.0D, 0.4D, 0.8D);
    public static final VoxelShape AABB_SOUTH3 = Shapes.box(0.0D, 0.4D, 0.4D, 1.0D, 0.6D, 0.6D);
    public static final VoxelShape AABB_SOUTH4 = Shapes.box(0.0D, 0.6D, 0.2D, 1.0D, 0.8D, 0.4D);
    public static final VoxelShape AABB_SOUTH5 = Shapes.box(0.0D, 0.8D, 0.0D, 1.0D, 1.0D, 0.2D);
    public static final VoxelShape SHAPE_SOUTH = Shapes.or(AABB_SOUTH1, AABB_SOUTH2, AABB_SOUTH3, AABB_SOUTH4, AABB_SOUTH5);

    public static final VoxelShape AABB_WEST1 = Shapes.box(0.0D, 0.0D, 0.0D, 0.2D, 0.2D, 1.0D);
    public static final VoxelShape AABB_WEST2 = Shapes.box(0.2D, 0.2D, 0.0D, 0.4D, 0.4D, 1.0D);
    public static final VoxelShape AABB_WEST3 = Shapes.box(0.4D, 0.4D, 0.0D, 0.6D, 0.6D, 1.0D);
    public static final VoxelShape AABB_WEST4 = Shapes.box(0.6D, 0.6D, 0.0D, 0.8D, 0.8D, 1.0D);
    public static final VoxelShape AABB_WEST5 = Shapes.box(0.8D, 0.8D, 0.0D, 1.0D, 1.0D, 1.0D);
    public static final VoxelShape SHAPE_WEST = Shapes.or(AABB_WEST1, AABB_WEST2, AABB_WEST3, AABB_WEST4, AABB_WEST5);

    public static final VoxelShape AABB_EAST1 = Shapes.box(0.8D, 0.0D, 0.0D, 1.0D, 0.2D, 1.0D);
    public static final VoxelShape AABB_EAST2 = Shapes.box(0.6D, 0.2D, 0.0D, 0.8D, 0.4D, 1.0D);
    public static final VoxelShape AABB_EAST3 = Shapes.box(0.4D, 0.4D, 0.0D, 0.6D, 0.6D, 1.0D);
    public static final VoxelShape AABB_EAST4 = Shapes.box(0.2D, 0.6D, 0.0D, 0.4D, 0.8D, 1.0D);
    public static final VoxelShape AABB_EAST5 = Shapes.box(0.0D, 0.8D, 0.0D, 0.2D, 1.0D, 1.0D);
    public static final VoxelShape SHAPE_EAST = Shapes.or(AABB_EAST1, AABB_EAST2, AABB_EAST3, AABB_EAST4, AABB_EAST5);

    public RampBlock() {
        super(new BlockBuilder()
                .properties(Properties.of(Material.STONE).strength(2.0f, 4.0f))
                // @todo 1.18 .harvestLevel(ToolType.PICKAXE, 1)
        );
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.HORIZROTATION;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
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
        return Shapes.empty();
    }
}
