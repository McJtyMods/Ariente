package mcjty.ariente.bindings;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketFullHealth {

    public void toBytes(FriendlyByteBuf buf) {
    }

    public PacketFullHealth() {
    }

    public PacketFullHealth(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            Player playerEntity = ctx.getSender();
            playerEntity.heal(100);
        });
        ctx.setPacketHandled(true);
    }
}
