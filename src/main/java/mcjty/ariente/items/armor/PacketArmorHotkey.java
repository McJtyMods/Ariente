package mcjty.ariente.items.armor;

import io.netty.buffer.ByteBuf;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.lib.thirteen.Context;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.function.Supplier;

public class PacketArmorHotkey implements IMessage {

    private int index;

    @Override
    public void fromBytes(ByteBuf buf) {
        index = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(index);
    }

    public PacketArmorHotkey() {
    }

    public PacketArmorHotkey(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketArmorHotkey(int index) {
        this.index = index;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            EntityPlayerMP playerEntity = ctx.getSender();
            ModuleSupport.receivedHotkey(playerEntity, index);
        });
        ctx.setPacketHandled(true);
    }
}
