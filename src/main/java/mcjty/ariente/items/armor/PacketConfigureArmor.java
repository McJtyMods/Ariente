package mcjty.ariente.items.armor;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketConfigureArmor {

    public void toBytes(FriendlyByteBuf buf) {
    }

    public PacketConfigureArmor() {
    }

    public PacketConfigureArmor(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            Player playerEntity = ctx.getSender();
            PowerArmorConfiguration.openConfigurationGui(playerEntity);
        });
        ctx.setPacketHandled(true);
    }
}
