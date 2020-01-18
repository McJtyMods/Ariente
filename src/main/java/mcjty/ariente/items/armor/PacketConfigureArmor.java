package mcjty.ariente.items.armor;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketConfigureArmor {

    public void toBytes(PacketBuffer buf) {
    }

    public PacketConfigureArmor() {
    }

    public PacketConfigureArmor(PacketBuffer buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            PlayerEntity playerEntity = ctx.getSender();
            PowerArmorConfiguration.openConfigurationGui(playerEntity);
        });
        ctx.setPacketHandled(true);
    }
}
