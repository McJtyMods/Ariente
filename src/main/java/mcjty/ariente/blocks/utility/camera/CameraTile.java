package mcjty.ariente.blocks.utility.camera;

import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;

public class CameraTile extends GenericTileEntity {

    private BlockState capturedState;
    private final Map<Integer, Capture> captures = new HashMap<>();

    public CameraTile(BlockPos pos, BlockState state) {
        super(Registration.CAMERA_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .properties(Block.Properties.of().mapColor(MapColor.METAL).strength(2).noOcclusion())
                .tileEntitySupplier(CameraTile::new)) {

            @Override
            public BlockState getStateForPlacement(BlockPlaceContext context) {
                return defaultBlockState().setValue(BlockStateProperties.FACING, context.getClickedFace());
            }

            @Override
            public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                return shape(state.getValue(BlockStateProperties.FACING));
            }
        };
    }

    public static VoxelShape shape(Direction facing) {
        return switch (facing) {
            case UP -> Shapes.or(Block.box(0, 0, 0, 16, 1, 16), Block.box(4, 1, 4, 12, 9, 12));
            case DOWN -> Shapes.or(Block.box(0, 15, 0, 16, 16, 16), Block.box(4, 7, 4, 12, 15, 12));
            case NORTH -> Shapes.or(Block.box(0, 0, 15, 16, 16, 16), Block.box(4, 4, 7, 12, 12, 15));
            case SOUTH -> Shapes.or(Block.box(0, 0, 0, 16, 16, 1), Block.box(4, 4, 1, 12, 12, 9));
            case WEST -> Shapes.or(Block.box(15, 0, 0, 16, 16, 16), Block.box(7, 4, 4, 15, 12, 12));
            case EAST -> Shapes.or(Block.box(0, 0, 0, 1, 16, 16), Block.box(1, 4, 4, 9, 12, 12));
        };
    }

    /** Demand driven: linked displays share one capture per second; the camera never ticks or loads chunks. */
    public CompoundTag capture() {
        return capture(CameraCapture.DEFAULT_RANGE);
    }

    public CompoundTag capture(int range) {
        long now = level.getGameTime();
        if (capturedState != getBlockState()) {
            captures.clear();
            capturedState = getBlockState();
        }
        captures.entrySet().removeIf(entry -> now < entry.getValue().tick() || now - entry.getValue().tick() >= 20);
        Capture saved = captures.get(range);
        if (saved == null) {
            saved = new Capture(now, CameraCapture.scan(level, worldPosition, getBlockState().getValue(BlockStateProperties.FACING), range));
            captures.put(range, saved);
        }
        return saved.data();
    }

    private record Capture(long tick, CompoundTag data) {
    }
}
