package mcjty.ariente.blocks.purifier;

import mcjty.ariente.config.PowerConfiguration;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public record PurifierStructure(Direction direction, List<BlockPos> tubes, PurifierTile finish) {

    /** Endpoints have exactly one horizontal tube connection; parallel runs do not connect sideways. */
    private static boolean hasSingleConnection(Level level, BlockPos endpoint) {
        int connections = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos pos = endpoint.relative(direction);
            if (!level.hasChunkAt(pos)) {
                return false;
            }
            if (isTube(level.getBlockState(pos), direction)) {
                connections++;
            }
        }
        return connections == 1;
    }

    public static boolean isTube(BlockState state, Direction direction) {
        return state.is(Registration.PURIFIER_TUBE.get())
                && state.getValue(BlockStateProperties.HORIZONTAL_AXIS) == direction.getAxis();
    }

    @Nullable
    public static PurifierStructure find(Level level, BlockPos start) {
        if (!hasSingleConnection(level, start)) {
            return null;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            List<BlockPos> tubes = new ArrayList<>();
            for (int distance = 1; distance <= PowerConfiguration.PURIFIER_MAX_TUBES.get() + 1; distance++) {
                BlockPos pos = start.relative(direction, distance);
                if (!level.hasChunkAt(pos)) {
                    break;
                }
                BlockState state = level.getBlockState(pos);
                if (isTube(state, direction) && tubes.size() < PowerConfiguration.PURIFIER_MAX_TUBES.get()) {
                    tubes.add(pos);
                } else {
                    if (!tubes.isEmpty() && state.is(Registration.PURIFIER_FINISH.get())
                            && hasSingleConnection(level, pos) && level.getBlockEntity(pos) instanceof PurifierTile finish) {
                        return new PurifierStructure(direction, List.copyOf(tubes), finish);
                    }
                    break;
                }
            }
        }
        return null;
    }
}
