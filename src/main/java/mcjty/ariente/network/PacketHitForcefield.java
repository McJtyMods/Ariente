package mcjty.ariente.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketHitForcefield {

    public void toBytes(PacketBuffer buf) {
    }

    public PacketHitForcefield() {
    }

    public PacketHitForcefield(PacketBuffer buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            PlayerEntity playerEntity = ctx.getSender();
            // @todo

        });
        ctx.setPacketHandled(true);
    }
}
