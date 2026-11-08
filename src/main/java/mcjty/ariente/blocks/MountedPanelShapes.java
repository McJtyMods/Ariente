package mcjty.ariente.blocks;

import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Bounds of a mounted panel, rotated using the block model's facing convention. */
public class MountedPanelShapes {

    private final Map<Direction, VoxelShape> shapes;

    public MountedPanelShapes(double inset, double depth) {
        double max = 16 - inset;
        shapes = Map.of(
                Direction.NORTH, Block.box(inset, inset, 16 - depth, max, max, 16),
                Direction.SOUTH, Block.box(inset, inset, 0, max, max, depth),
                Direction.WEST, Block.box(16 - depth, inset, inset, 16, max, max),
                Direction.EAST, Block.box(0, inset, inset, depth, max, max),
                Direction.DOWN, Block.box(inset, 16 - depth, inset, max, 16, max),
                Direction.UP, Block.box(inset, 0, inset, max, depth, max));
    }

    public VoxelShape get(Direction facing) {
        return shapes.get(facing);
    }
}
