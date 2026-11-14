package mcjty.ariente.teleport.client;

import mcjty.ariente.Ariente;
import mcjty.ariente.teleport.PacketWarperPreview;
import mcjty.ariente.teleport.WarperDestination;
import net.minecraft.nbt.CompoundTag;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class WarperPreviewCache {

    private static final Map<UUID, PacketWarperPreview> PREVIEWS = new LinkedHashMap<>();

    public static void receive(PacketWarperPreview packet) {
        PREVIEWS.remove(packet.card());
        PREVIEWS.put(packet.card(), packet);
        while (PREVIEWS.size() > 8) {
            PREVIEWS.remove(PREVIEWS.keySet().iterator().next());
        }
    }

    public static CompoundTag get(UUID card, WarperDestination destination) {
        PacketWarperPreview packet = PREVIEWS.get(card);
        return packet != null && destination != null && destination.equals(WarperDestination.load(packet.destination())) ? packet.scene() : null;
    }

    public static void clear() {
        PREVIEWS.clear();
    }
}
