package mcjty.ariente.display;

import net.minecraft.world.phys.Vec3;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import mcjty.ariente.Ariente;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** A fair, bounded per-player stream. The normal channel is sufficient because every message is small. */
@EventBusSubscriber(modid = Ariente.MODID)
public class DisplayDataSender {

    public static final int PACKETS_PER_TICK = 4;
    private static final int MAX_PENDING = 128;
    private static final int MAX_QUEUED_BYTES = 8 * 1024 * 1024;
    private static final Map<ServerPlayer, PlayerQueue> QUEUES = new HashMap<>();

    public static void broadcast(DisplayBaseTile base, int index) {
        ServerLevel level = (ServerLevel) base.getLevel();
        for (ServerPlayer player : level.getChunkSource().chunkMap.getPlayers(ChunkPos.containing(base.getBlockPos()), false)) {
            enqueue(player, base, index, false);
        }
    }

    public static void request(ServerPlayer player, PacketRequestDisplayData request) {
        ServerLevel level = player.level();
        if (player.distanceToSqr(Vec3.atCenterOf(request.pos())) > 64 * 64 || !level.hasChunkAt(request.pos())
                || !(level.getBlockEntity(request.pos()) instanceof DisplayBaseTile base)
                || base.layoutVersion() != request.layoutVersion()
                || !level.getChunkSource().chunkMap.getPlayers(ChunkPos.containing(request.pos()), false).contains(player)) {
            return;
        }
        PlayerQueue queue = queue(player);
        long tick = level.getGameTime();
        if (queue.requestTick != tick) {
            queue.requestTick = tick;
            queue.requestsThisTick = 0;
        }
        if (queue.requestsThisTick >= 4 || tick < queue.requests.getOrDefault(request.pos(), Long.MIN_VALUE)) {
            return;
        }
        queue.requestsThisTick++;
        queue.requests.put(request.pos(), tick + 40);
        if (queue.requests.size() > MAX_PENDING) {
            queue.requests.remove(queue.requests.keySet().iterator().next());
        }
        for (int index = 0; index < base.modules().size(); index++) {
            if ((request.modules() & 1 << index) == 0) {
                continue;
            }
            InstalledModule module = base.modules().get(index);
            if (observed(player, base, module)) {
                if (module.snapshot().isEmpty()) {
                    module.refresh(level);
                }
                enqueue(player, base, index, true);
            }
        }
    }

    private static PlayerQueue queue(ServerPlayer player) {
        PlayerQueue queue = QUEUES.get(player);
        if (queue == null || queue.level != player.level()) {
            queue = new PlayerQueue(player.level());
            QUEUES.put(player, queue);
        }
        return queue;
    }

    private static boolean observed(ServerPlayer player, DisplayBaseTile base, InstalledModule module) {
        int distance = module.type().observationDistance();
        return player.distanceToSqr(Vec3.atCenterOf(base.getBlockPos())) <= distance * distance;
    }

    private static void enqueue(ServerPlayer player, DisplayBaseTile base, int index, boolean requested) {
        InstalledModule module = base.modules().get(index);
        if (!observed(player, base, module)) {
            return;
        }
        PlayerQueue queue = queue(player);
        Key key = new Key(base.getBlockPos(), index);
        Sent delivered = queue.delivered.get(key);
        if (!requested && delivered != null && delivered.layout() == base.layoutVersion() && delivered.version() == module.snapshotVersion()) {
            return;
        }
        Pending previous = queue.pending.get(key);
        if (previous != null && previous.layout == base.layoutVersion() && previous.latestVersion() == module.snapshotVersion()) {
            return;
        }
        byte[] bytes = module.encodedSnapshot();
        boolean inProgress = previous != null && previous.layout == base.layoutVersion() && previous.offset > 0;
        int retained = inProgress ? previous.bytes.length : 0;
        int replacing = previous == null ? 0 : previous.size();
        if (queue.pending.size() >= MAX_PENDING && previous == null
                || queue.queuedBytes - replacing + retained + bytes.length > MAX_QUEUED_BYTES) {
            return;
        }
        queue.queuedBytes += retained + bytes.length - replacing;
        if (inProgress) {
            // Finish the image already in flight, then send only the most recent waiting image.
            previous.nextBytes = bytes;
            previous.nextVersion = module.snapshotVersion();
        } else {
            queue.pending.put(key, new Pending(base, index, bytes, module.snapshotVersion()));
        }
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        QUEUES.entrySet().removeIf(entry -> entry.getKey().hasDisconnected() || entry.getKey().level() != entry.getValue().level);
        QUEUES.forEach((player, queue) -> {
            for (int sent = 0; sent < PACKETS_PER_TICK && !queue.pending.isEmpty(); sent++) {
                Key key = queue.pending.keySet().iterator().next();
                Pending pending = queue.pending.remove(key);
                queue.queuedBytes -= pending.size();
                if (pending.base.isRemoved() || pending.base.layoutVersion() != pending.layout
                        || pending.base.getLevel() != player.level() || pending.index >= pending.base.modules().size()
                        || !observed(player, pending.base, pending.base.modules().get(pending.index))) {
                    continue;
                }
                int end = Math.min(pending.bytes.length, pending.offset + DisplayDataCodec.FRAGMENT_BYTES);
                PacketDistributor.sendToPlayer(player,
                        new PacketDisplayData(key.pos(), pending.layout, pending.index, pending.version,
                                pending.bytes.length, pending.offset, Arrays.copyOfRange(pending.bytes, pending.offset, end)));
                pending.offset = end;
                if (end == pending.bytes.length) {
                    queue.delivered.put(key, new Sent(pending.layout, pending.version));
                    if (queue.delivered.size() > MAX_PENDING) {
                        queue.delivered.remove(queue.delivered.keySet().iterator().next());
                    }
                }
                if (end == pending.bytes.length && pending.nextBytes != null) {
                    pending.bytes = pending.nextBytes;
                    pending.version = pending.nextVersion;
                    pending.nextBytes = null;
                    pending.offset = 0;
                }
                if (pending.offset < pending.bytes.length) {
                    queue.pending.put(key, pending);
                    queue.queuedBytes += pending.size();
                }
            }
        });
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        QUEUES.remove(event.getEntity());
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        QUEUES.clear();
    }

    private record Key(BlockPos pos, int index) {
    }

    private record Sent(long layout, int version) {
    }

    private static class PlayerQueue {

        private final ServerLevel level;
        private final LinkedHashMap<Key, Pending> pending = new LinkedHashMap<>();
        private final LinkedHashMap<BlockPos, Long> requests = new LinkedHashMap<>();
        private final LinkedHashMap<Key, Sent> delivered = new LinkedHashMap<>();
        private int queuedBytes;
        private long requestTick = -1;
        private int requestsThisTick;

        private PlayerQueue(ServerLevel level) {
            this.level = level;
        }
    }

    private static class Pending {

        private final DisplayBaseTile base;
        private final int index;
        private final long layout;
        private byte[] bytes;
        private int version;
        private int offset;
        private byte[] nextBytes;
        private int nextVersion;

        private Pending(DisplayBaseTile base, int index, byte[] bytes, int version) {
            this.base = base;
            this.index = index;
            layout = base.layoutVersion();
            this.bytes = bytes;
            this.version = version;
        }

        private int latestVersion() {
            return nextBytes == null ? version : nextVersion;
        }

        private int size() {
            return bytes.length + (nextBytes == null ? 0 : nextBytes.length);
        }
    }
}
