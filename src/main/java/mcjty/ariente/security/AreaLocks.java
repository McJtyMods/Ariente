package mcjty.ariente.security;

import net.minecraft.world.level.Level;

import mcjty.lib.varia.PersistentData;
import mcjty.ariente.varia.SavedDataMigration;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.resources.Identifier;
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

/** Keeps controller authority available without loading the controller's chunk. */
public class AreaLocks extends SavedData {

    private static final String TARGET_STATE = "ArienteAreaLock";
    private final Map<BlockPos, Controller> controllers = new HashMap<>();
    private final Set<BlockPos> pending = new HashSet<>();
    private final Set<ChunkPos> pendingChunks = new HashSet<>();

    public static AreaLocks get(ServerLevel level) {
        Identifier id = Identifier.withDefaultNamespace("ariente_area_locks");
        SavedDataMigration.migrate(level, id, "ariente_area_locks");
        return level.getDataStorage().computeIfAbsent(new SavedDataType<>(id, AreaLocks::new, CompoundTag.CODEC.xmap(AreaLocks::load, data -> data.save(new CompoundTag(), level.registryAccess())), null));
    }

    public static void loaded(BlockEntity tile) {
        if (tile.getLevel() instanceof ServerLevel level) {
            // onLoad can run before a chunk is promoted to FULL. Reconcile after promotion.
            level.getServer().schedule(new TickTask(level.getServer().getTickCount(),
                    () -> get(level).pending.add(tile.getBlockPos().immutable())));
        }
    }

    public static void tick(Level world) {
        if (world instanceof ServerLevel level) {
            AreaLocks locks = get(level);
            for (ChunkPos pos : List.copyOf(locks.pendingChunks)) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x(), pos.z());
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

    public static void chunkLoaded(Level world, LevelChunk chunk) {
        if (world instanceof ServerLevel level) {
            ChunkPos pos = chunk.getPos();
            level.getServer().schedule(new TickTask(level.getServer().getTickCount(),
                    () -> get(level).pendingChunks.add(pos)));
        }
    }

    public static void saveTarget(BlockEntity tile, CompoundTag tag) {
        if (PersistentData.get(tile).contains(TARGET_STATE)) {
            tag.put(TARGET_STATE, PersistentData.get(tile).getCompoundOrEmpty(TARGET_STATE).copy());
        }
    }

    public static void loadTarget(BlockEntity tile, CompoundTag tag) {
        if (tag.contains(TARGET_STATE)) {
            PersistentData.get(tile).put(TARGET_STATE, tag.getCompoundOrEmpty(TARGET_STATE).copy());
        } else {
            PersistentData.get(tile).remove(TARGET_STATE);
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
        CompoundTag data = PersistentData.get(tile);
        if (authority != null) {
            CompoundTag state = new CompoundTag();
            if (authority.key() != null) {
                state.putString("key", authority.key());
            }
            if (!data.contains(TARGET_STATE) || !state.equals(data.getCompoundOrEmpty(TARGET_STATE))) {
                data.put(TARGET_STATE, state);
                tile.setChanged();
            }
            // An overlapping unlocked controller cannot override another authorized locked one.
            target.setLocked(locked);
        } else if (data.contains(TARGET_STATE)) {
            CompoundTag previous = data.getCompoundOrEmpty(TARGET_STATE);
            String key = previous.contains("key") ? previous.getStringOr("key", "") : null;
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
        for (Tag entry : tag.getListOrEmpty("controllers")) {
            CompoundTag data = (CompoundTag) entry;
            BlockPos pos = BlockPos.of(data.getLongOr("pos", 0));
            locks.controllers.put(pos, new Controller(pos, data.getIntOr("horizontal", 0), data.getIntOr("vertical", 0),
                    data.getBooleanOr("locked", false), data.contains("key") ? data.getStringOr("key", "") : null));
        }
        return locks;
    }

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
