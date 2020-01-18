package mcjty.ariente.items.armor;

import mcjty.ariente.items.modules.ModuleSupport;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketArmorHotkey {

    private int index;

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(index);
    }

    public PacketArmorHotkey() {
    }

    public PacketArmorHotkey(PacketBuffer buf) {
        index = buf.readInt();
    }

    public PacketArmorHotkey(int index) {
        this.index = index;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            PlayerEntity playerEntity = ctx.getSender();
            ModuleSupport.receivedHotkey(playerEntity, index);
        });
        ctx.setPacketHandled(true);
    }
}
