package mcjty.ariente.security;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.utility.ILockTarget;
import mcjty.ariente.blocks.utility.LockTile;
import mcjty.ariente.blocks.utility.wireless.WirelessLockTile;
import mcjty.ariente.varia.LoadedChunks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Keeps controller authority available without loading the controller's chunk. */
@EventBusSubscriber(modid = Ariente.MODID)
public class AreaLocks extends SavedData {

    private static final String TARGET_STATE = "ArienteAreaLock";
    private final Map<BlockPos, Controller> controllers = new HashMap<>();
    private final Set<BlockPos> pending = new HashSet<>();
    private final Set<ChunkPos> pendingChunks = new HashSet<>();

    public static AreaLocks get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(AreaLocks::new, (tag, provider) -> load(tag)), "ariente_area_locks");
    }

    public static void loaded(BlockEntity tile) {
        if (tile.getLevel() instanceof ServerLevel level) {
            // onLoad can run before a chunk is promoted to FULL. Reconcile after promotion.
            level.getServer().tell(new TickTask(level.getServer().getTickCount(),
                    () -> get(level).pending.add(tile.getBlockPos().immutable())));
        }
    }

    @SubscribeEvent
    public static void tick(LevelTickEvent.Pre event) {
        if (event.getLevel() instanceof ServerLevel level) {
            AreaLocks locks = get(level);
            for (ChunkPos pos : List.copyOf(locks.pendingChunks)) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x, pos.z);
                if (chunk != null) {
                    locks.pending.addAll(chunk.getBlockEntitiesPos());
                    locks.pendingChunks.remove(pos);
                }
            }
            for (BlockPos pos : List.copyOf(locks.pending)) {
                if (level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) == null) {
                    continue;
                }
                locks.pending.remove(pos);
                BlockEntity tile = LoadedChunks.getBlockEntity(level, pos);
                if (tile instanceof LockTile lock) {
                    update(lock, lock.getHorizontalRange(), lock.getVerticalRange(), lock.isLocked(), lock.getKeyId());
                } else if (tile instanceof WirelessLockTile lock) {
                    update(lock, lock.getHorizontalRange(), lock.getVerticalRange(), lock.isLocked(), null);
                } else if (tile != null) {
                    locks.reconcile(tile);
                }
            }
        }
    }

    @SubscribeEvent
    public static void chunkLoaded(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            ChunkPos pos = event.getChunk().getPos();
            level.getServer().tell(new TickTask(level.getServer().getTickCount(),
                    () -> get(level).pendingChunks.add(pos)));
        }
    }

    public static void saveTarget(BlockEntity tile, CompoundTag tag) {
        if (tile.getPersistentData().contains(TARGET_STATE, Tag.TAG_COMPOUND)) {
            tag.put(TARGET_STATE, tile.getPersistentData().getCompound(TARGET_STATE).copy());
        }
    }

    public static void loadTarget(BlockEntity tile, CompoundTag tag) {
        if (tag.contains(TARGET_STATE, Tag.TAG_COMPOUND)) {
            tile.getPersistentData().put(TARGET_STATE, tag.getCompound(TARGET_STATE).copy());
        } else {
            tile.getPersistentData().remove(TARGET_STATE);
        }
    }

    public static void update(BlockEntity tile, int horizontal, int vertical, boolean locked, @Nullable String key) {
        if (tile.getLevel() instanceof ServerLevel level) {
            AreaLocks locks = get(level);
            Controller current = new Controller(tile.getBlockPos().immutable(), horizontal, vertical, locked, key);
            Controller previous = locks.controllers.put(current.pos(), current);
            if (!current.equals(previous)) {
                locks.setDirty();
                if (previous != null) {
                    locks.reconcileLoaded(level, previous);
                }
                locks.reconcileLoaded(level, current);
            }
        }
    }

    public static void remove(BlockEntity tile) {
        if (tile.getLevel() instanceof ServerLevel level) {
            AreaLocks locks = get(level);
            Controller previous = locks.controllers.remove(tile.getBlockPos());
            if (previous != null) {
                locks.setDirty();
                locks.reconcileLoaded(level, previous);
            }
        }
    }

    private void reconcileLoaded(ServerLevel level, Controller area) {
        int minX = (area.pos().getX() - area.horizontal()) >> 4;
        int maxX = (area.pos().getX() + area.horizontal()) >> 4;
        int minZ = (area.pos().getZ() - area.horizontal()) >> 4;
        int maxZ = (area.pos().getZ() + area.horizontal()) >> 4;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);
                if (chunk == null) {
                    // A chunk can lose FULL status and return without firing an unload/load pair.
                    // Keep the missed update queued even when no block entity onLoad will run.
                    pendingChunks.add(new ChunkPos(x, z));
                } else {
                    for (BlockEntity target : List.copyOf(chunk.getBlockEntities().values())) {
                        if (area.contains(target.getBlockPos())) {
                            reconcile(target);
                        }
                    }
                }
            }
        }
    }

    public void reconcile(BlockEntity tile) {
        if (!(tile instanceof ILockTarget target)) {
            return;
        }
        Controller authority = null;
        boolean locked = false;
        for (Controller controller : controllers.values()) {
            if (controller.contains(tile.getBlockPos()) && target.canBeControlledBy(controller.key())) {
                authority = controller;
                locked |= controller.locked();
            }
        }
        CompoundTag data = tile.getPersistentData();
        if (authority != null) {
            CompoundTag state = new CompoundTag();
            if (authority.key() != null) {
                state.putString("key", authority.key());
            }
            if (!data.contains(TARGET_STATE, Tag.TAG_COMPOUND) || !state.equals(data.getCompound(TARGET_STATE))) {
                data.put(TARGET_STATE, state);
                tile.setChanged();
            }
            // An overlapping unlocked controller cannot override another authorized locked one.
            target.setLocked(locked);
        } else if (data.contains(TARGET_STATE, Tag.TAG_COMPOUND)) {
            CompoundTag previous = data.getCompound(TARGET_STATE);
            String key = previous.contains("key") ? previous.getString("key") : null;
            data.remove(TARGET_STATE);
            tile.setChanged();
            // Rekeying a target must not let a former controller release its new lock.
            if (target.canBeControlledBy(key)) {
                target.setLocked(false);
            }
        }
    }

    private static AreaLocks load(CompoundTag tag) {
        AreaLocks locks = new AreaLocks();
        for (Tag entry : tag.getList("controllers", Tag.TAG_COMPOUND)) {
            CompoundTag data = (CompoundTag) entry;
            BlockPos pos = BlockPos.of(data.getLong("pos"));
            locks.controllers.put(pos, new Controller(pos, data.getInt("horizontal"), data.getInt("vertical"),
                    data.getBoolean("locked"), data.contains("key") ? data.getString("key") : null));
        }
        return locks;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Controller controller : controllers.values()) {
            CompoundTag data = new CompoundTag();
            data.putLong("pos", controller.pos().asLong());
            data.putInt("horizontal", controller.horizontal());
            data.putInt("vertical", controller.vertical());
            data.putBoolean("locked", controller.locked());
            if (controller.key() != null) {
                data.putString("key", controller.key());
            }
            list.add(data);
        }
        tag.put("controllers", list);
        return tag;
    }

    private record Controller(BlockPos pos, int horizontal, int vertical, boolean locked, @Nullable String key) {

        private boolean contains(BlockPos target) {
            return Math.abs((long) target.getX() - pos.getX()) <= horizontal
                    && Math.abs((long) target.getY() - pos.getY()) <= vertical
                    && Math.abs((long) target.getZ() - pos.getZ()) <= horizontal;
        }
    }
}
