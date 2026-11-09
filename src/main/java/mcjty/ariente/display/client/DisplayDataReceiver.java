package mcjty.ariente.display.client;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import java.util.HashMap;
import java.util.Map;
import mcjty.ariente.display.DisplayBaseTile;
import mcjty.ariente.display.DisplayDataAssembly;
import mcjty.ariente.display.DisplayDataCodec;
import mcjty.ariente.display.PacketDisplayData;
import mcjty.ariente.display.PacketRequestDisplayData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;

public class DisplayDataReceiver {

    private static final int MAX_PENDING_BYTES = 8 * 1024 * 1024;
    private static final Map<Key, DisplayDataAssembly> PENDING = new HashMap<>();
    private static ClientLevel currentLevel;

    private static void checkLevel() {
        ClientLevel level = Minecraft.getInstance().level;
        if (currentLevel != level) {
            PENDING.clear();
            currentLevel = level;
        }
    }

    public static void receive(PacketDisplayData packet) {
        checkLevel();
        if (currentLevel == null || !currentLevel.hasChunkAt(packet.pos())
                || !(currentLevel.getBlockEntity(packet.pos()) instanceof DisplayBaseTile base)
                || base.layoutVersion() != packet.layoutVersion() || packet.index() >= base.modules().size()
                || packet.snapshotVersion() <= base.modules().get(packet.index()).receivedSnapshotVersion()) {
            return;
        }
        Key key = new Key(packet.pos(), packet.layoutVersion(), packet.index());
        DisplayDataAssembly assembly = PENDING.get(key);
        if (assembly == null || packet.snapshotVersion() > assembly.version()) {
            if (packet.offset() != 0) {
                return;
            }
            PENDING.remove(key);
            int allocated = PENDING.values().stream().mapToInt(DisplayDataAssembly::size).sum();
            if (PENDING.size() >= 128 || allocated + packet.totalBytes() > MAX_PENDING_BYTES) {
                return;
            }
            assembly = new DisplayDataAssembly(packet, currentLevel.getGameTime());
            PENDING.put(key, assembly);
        }
        byte[] complete = assembly.accept(packet);
        if (complete != null) {
            PENDING.remove(key);
            try {
                base.applyReadings(packet.layoutVersion(), packet.index(), packet.snapshotVersion(), DisplayDataCodec.decode(complete));
            } catch (RuntimeException e) {
                // Retain the previous complete image. The bounded initial-data request will retry if needed.
            }
        }
    }

    public static void tick() {
        checkLevel();
        Minecraft mc = Minecraft.getInstance();
        if (currentLevel == null || mc.player == null) {
            return;
        }
        PENDING.entrySet().removeIf(entry -> entry.getValue().expired(currentLevel.getGameTime())
                || !currentLevel.hasChunkAt(entry.getKey().pos())
                || !(currentLevel.getBlockEntity(entry.getKey().pos()) instanceof DisplayBaseTile base)
                || base.layoutVersion() != entry.getKey().layout());
        int requested = 0;
        for (DisplayBaseTile base : DisplayBaseTile.LOADED) {
            if (base.isRemoved() || base.getLevel() != currentLevel) {
                continue;
            }
            int missing = base.requestReadings(mc.player);
            if (missing != 0) {
                ClientPacketDistributor.sendToServer(new PacketRequestDisplayData(base.getBlockPos(), base.layoutVersion(), missing));
                if (++requested == 4) {
                    break;
                }
            }
        }
    }

    private record Key(BlockPos pos, long layout, int index) {
    }
}
