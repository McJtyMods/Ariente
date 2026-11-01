package mcjty.ariente.display.client;

import mcjty.ariente.display.DisplayBaseTile;
import mcjty.ariente.display.PacketDisplayData;
import net.minecraft.client.Minecraft;

public class DisplayDataReceiver {

    public static void receive(PacketDisplayData packet) {
        var level = Minecraft.getInstance().level;
        if (level != null && level.hasChunkAt(packet.pos())
                && level.getBlockEntity(packet.pos()) instanceof DisplayBaseTile base) {
            base.applyReadings(packet);
        }
    }
}
