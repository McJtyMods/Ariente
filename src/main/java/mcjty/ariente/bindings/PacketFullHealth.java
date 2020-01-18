package mcjty.ariente.bindings;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketFullHealth {

    public void toBytes(PacketBuffer buf) {
    }

    public PacketFullHealth() {
    }

    public PacketFullHealth(PacketBuffer buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            PlayerEntity playerEntity = ctx.getSender();
            playerEntity.heal(100);
        });
        ctx.setPacketHandled(true);
    }
}
