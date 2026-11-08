package mcjty.ariente.blocks.utility.camera;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Capture surfaces bordering the camera's connected space, without exposing sealed cavities. */
public class CameraCapture {

    public static final int MIN_RANGE = 8;
    public static final int DEFAULT_RANGE = 30;
    public static final int MAX_RANGE = 100;
    public static final int MAX_BLOCKS = 16384;
    public static final int MAX_ENTITIES = 16;
    public static final int ORIGIN_DISTANCE = 2;
    // A radius-100 hemisphere contains fewer than 2,200,000 block centers.
    private static final int MAX_SPACE_CELLS = 2_200_000;
    private static final int FACE_MASK = 63;
    private static final int REACHED = 64;
    private static final int KNOWN = 128;
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int ORIGIN = 128 | 128 << 8 | 128 << 16;
    // Reuse primitive buffers on the server thread. No chunks, levels or captures are retained here.
    private static final ThreadLocal<ScanWorkspace> WORKSPACE = ThreadLocal.withInitial(ScanWorkspace::new);

    public static boolean inFront(Vec3 relative, Direction normal) {
        return relative.dot(Vec3.atLowerCornerOf(normal.getNormal())) >= 0;
    }

    public static CompoundTag scan(Level level, BlockPos camera, Direction normal) {
        return scan(level, camera, normal, DEFAULT_RANGE);
    }

    /** The explicit range also permits comparing capture costs. */
    public static CompoundTag scan(Level level, BlockPos camera, Direction normal, int range) {
        if (range < MIN_RANGE || range > MAX_RANGE) {
            throw new IllegalArgumentException("Invalid camera range");
        }
        BlockPos origin = camera.relative(normal, -ORIGIN_DISTANCE);
        Vec3 center = Vec3.atCenterOf(origin);
        Vec3 cameraCenter = Vec3.atCenterOf(camera);
        ConnectedView view = new ConnectedView(level, camera);
        ScanWorkspace workspace = WORKSPACE.get();
        Arrays.fill(workspace.cells, (byte) 0);
        workspace.fluids.clear();
        // Seed at the lens, even when the shifted origin is buried in a wall or the ground.
        // Visibility still follows connected space and cannot start inside a sealed cave.
        workspace.cells[ORIGIN] = (byte) (KNOWN | REACHED);
        workspace.queue[0] = ORIGIN;
        int tail = 1;
        int rangeSquared = range * range;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Map<BlockState, Integer> shapeMasks = new IdentityHashMap<>();
        Map<BlockState, Integer> paletteIds = new HashMap<>();
        ListTag palette = new ListTag();
        LongArrayList blocks = new LongArrayList();
        for (int head = 0; head < tail; head++) {
            int current = workspace.queue[head];
            int x = (current & 255) - 128, y = (current >> 8 & 255) - 128, z = (current >> 16 & 255) - 128;
            int closedFaces = workspace.cells[current] & FACE_MASK;
            for (Direction direction : DIRECTIONS) {
                if ((closedFaces & 1 << direction.ordinal()) != 0) {
                    continue;
                }
                int nx = x + direction.getStepX(), ny = y + direction.getStepY(), nz = z + direction.getStepZ();
                int next = current + direction.getStepX() + (direction.getStepY() << 8) + (direction.getStepZ() << 16);
                int flags = workspace.cells[next] & 255;
                if ((flags & REACHED) != 0) {
                    continue;
                }
                if ((flags & KNOWN) == 0) {
                    int ox = nx + normal.getStepX() * ORIGIN_DISTANCE;
                    int oy = ny + normal.getStepY() * ORIGIN_DISTANCE;
                    int oz = nz + normal.getStepZ() * ORIGIN_DISTANCE;
                    if (ox * ox + oy * oy + oz * oz > rangeSquared
                            || ox * normal.getStepX() + oy * normal.getStepY() + oz * normal.getStepZ() < 0) {
                        workspace.cells[next] = (byte) (KNOWN | FACE_MASK);
                        continue;
                    }
                    pos.set(camera.getX() + nx, camera.getY() + ny, camera.getZ() + nz);
                    if (!view.isLoaded(pos)) {
                        workspace.cells[next] = (byte) (KNOWN | FACE_MASK);
                        continue;
                    }
                    BlockState state = view.getBlockState(pos);
                    flags = KNOWN | closedFaces(state, view, pos, shapeMasks);
                    workspace.cells[next] = (byte) flags;
                    // Record each exposed block once, including props reached by going around an obstacle.
                    if (!state.isAir()) {
                        if (state.getRenderShape() == RenderShape.INVISIBLE && !state.getFluidState().isEmpty()) {
                            workspace.fluids.set(next);
                        }
                        int id = paletteIds.computeIfAbsent(state, key -> {
                            palette.add(NbtUtils.writeBlockState(key));
                            return palette.size() - 1;
                        });
                        blocks.add((long) next | (long) id << 24);
                    }
                }
                if ((flags & 1 << direction.getOpposite().ordinal()) == 0 && tail < MAX_SPACE_CELLS) {
                    workspace.cells[next] = (byte) (flags | REACHED);
                    workspace.queue[tail++] = next;
                }
            }
        }
        // Internal water/lava cells consume budget without contributing a visible surface.
        // Keep only fluid cells adjoining reachable non-fluid space.
        LongArrayList surfaces = new LongArrayList(Math.min(MAX_BLOCKS, blocks.size()));
        int[] distances = new int[rangeSquared + 1];
        for (int i = 0; i < blocks.size(); i++) {
            long packed = blocks.getLong(i);
            int cell = (int) (packed & 0xffffff);
            if (workspace.fluids.get(cell) && !fluidSurface(workspace, cell)) {
                continue;
            }
            surfaces.add(packed);
            distances[distanceSquared(cell, normal)]++;
        }
        int cutoff = rangeSquared;
        int count = surfaces.size();
        if (count > MAX_BLOCKS) {
            count = 0;
            cutoff = -1;
            for (int distance = 0; distance <= rangeSquared && count + distances[distance] <= MAX_BLOCKS; distance++) {
                count += distances[distance];
                cutoff = distance;
            }
        }
        long[] captured = new long[count];
        int index = 0;
        for (int i = 0; i < surfaces.size(); i++) {
            long packed = surfaces.getLong(i);
            if (distanceSquared((int) (packed & 0xffffff), normal) <= cutoff) {
                captured[index++] = packed;
            }
        }
        ListTag entities = new ListTag();
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, new AABB(origin).inflate(range),
                entity -> entity.isAlive() && !entity.isInvisible() && !entity.isSpectator());
        nearby.sort(Comparator.comparingDouble(entity -> entity.distanceToSqr(center)));
        for (LivingEntity entity : nearby) {
            Vec3 target = entity.getBoundingBox().getCenter();
            Vec3 relative = target.subtract(center);
            BlockPos offset = BlockPos.containing(target).subtract(camera);
            if (relative.lengthSqr() > cutoff || !inFront(relative, normal)
                    || (workspace.cells[(int) pack(offset, 0)] & REACHED) == 0) {
                continue;
            }
            CompoundTag marker = new CompoundTag();
            relative = target.subtract(cameraCenter);
            // Quantize moving markers to limit network churn; never transmit entity NBT or inventories.
            marker.putFloat("X", (float) (Math.round(relative.x * 8) / 8.0));
            marker.putFloat("Y", (float) (Math.round(relative.y * 8) / 8.0));
            marker.putFloat("Z", (float) (Math.round(relative.z * 8) / 8.0));
            marker.putInt("Color", entity instanceof Player ? 0x78edff : 0xffbd70);
            entities.add(marker);
            if (entities.size() == MAX_ENTITIES) {
                break;
            }
        }
        CompoundTag result = new CompoundTag();
        result.putInt("Facing", normal.get3DDataValue());
        result.putInt("Range", range);
        result.putInt("SurfaceRadiusSquared", cutoff);
        result.put("Palette", palette);
        result.putLongArray("Blocks", captured);
        result.put("Entities", entities);
        return result;
    }

    public static long pack(BlockPos offset, int paletteId) {
        return offset.getX() + 128L | (offset.getY() + 128L) << 8 | (offset.getZ() + 128L) << 16 | (long) paletteId << 24;
    }

    public static BlockPos offset(long packed) {
        return new BlockPos((int) (packed & 255) - 128, (int) (packed >> 8 & 255) - 128, (int) (packed >> 16 & 255) - 128);
    }

    public static int paletteId(long packed) {
        return (int) (packed >>> 24);
    }

    private static int distanceSquared(int cell, Direction normal) {
        int x = (cell & 255) - 128 + normal.getStepX() * ORIGIN_DISTANCE;
        int y = (cell >> 8 & 255) - 128 + normal.getStepY() * ORIGIN_DISTANCE;
        int z = (cell >> 16 & 255) - 128 + normal.getStepZ() * ORIGIN_DISTANCE;
        return x * x + y * y + z * z;
    }

    private static boolean fluidSurface(ScanWorkspace workspace, int cell) {
        for (Direction direction : DIRECTIONS) {
            int next = cell + direction.getStepX() + (direction.getStepY() << 8) + (direction.getStepZ() << 16);
            if ((workspace.cells[next] & REACHED) != 0 && !workspace.fluids.get(next)) {
                return true;
            }
        }
        return false;
    }

    private static int closedFaces(BlockState state, BlockGetter view, BlockPos pos, Map<BlockState, Integer> cache) {
        if (state.isAir()) {
            return 0;
        }
        boolean dynamic = state.getBlock().hasDynamicShape();
        Integer saved = dynamic ? null : cache.get(state);
        if (saved != null) {
            return saved;
        }
        int mask = 0;
        if (state.isSolidRender(view, pos)) {
            mask = FACE_MASK;
        } else {
            VoxelShape shape = state.getVisualShape(view, pos, CollisionContext.empty());
            for (Direction direction : DIRECTIONS) {
                if (Block.isFaceFull(shape, direction)) {
                    mask |= 1 << direction.ordinal();
                }
            }
        }
        if (!dynamic) {
            cache.put(state, mask);
        }
        return mask;
    }

    private static class ScanWorkspace {

        private final byte[] cells = new byte[1 << 24];
        private final int[] queue = new int[MAX_SPACE_CELLS];
        private final BitSet fluids = new BitSet(1 << 24);
    }

    private static class ConnectedView implements BlockGetter {

        private final Level level;
        private final BlockPos camera;
        private int chunkX = Integer.MIN_VALUE;
        private int chunkZ = Integer.MIN_VALUE;
        private LevelChunk chunk;

        private ConnectedView(Level level, BlockPos camera) {
            this.level = level;
            this.camera = camera;
        }

        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            updateChunk(pos);
            return chunk == null ? null : chunk.getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);
        }

        private void updateChunk(BlockPos pos) {
            int x = pos.getX() >> 4, z = pos.getZ() >> 4;
            if (x != chunkX || z != chunkZ) {
                chunkX = x;
                chunkZ = z;
                // This lookup never schedules a chunk load.
                chunk = level.getChunkSource().getChunkNow(x, z);
            }
        }

        private boolean isLoaded(BlockPos pos) {
            updateChunk(pos);
            return chunk != null;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            updateChunk(pos);
            return chunk == null ? Blocks.BEDROCK.defaultBlockState() : pos.equals(camera) ? Blocks.AIR.defaultBlockState() : chunk.getBlockState(pos);
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return getBlockState(pos).getFluidState();
        }

        @Override
        public int getHeight() {
            return level.getHeight();
        }

        @Override
        public int getMinBuildHeight() {
            return level.getMinBuildHeight();
        }
    }
}
