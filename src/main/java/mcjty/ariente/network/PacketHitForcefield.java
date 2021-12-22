package mcjty.ariente.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketHitForcefield {

    public void toBytes(FriendlyByteBuf buf) {
    }

    public PacketHitForcefield() {
    }

    public PacketHitForcefield(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            Player playerEntity = ctx.getSender();
            // @todo

        });
        ctx.setPacketHandled(true);
    }
}
