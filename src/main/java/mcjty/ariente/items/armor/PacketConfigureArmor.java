package mcjty.ariente.items.armor;

import io.netty.buffer.ByteBuf;
import mcjty.lib.thirteen.Context;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.function.Supplier;

public class PacketConfigureArmor implements IMessage {

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public PacketConfigureArmor() {
    }

    public PacketConfigureArmor(ByteBuf buf) {
        fromBytes(buf);
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            EntityPlayerMP playerEntity = ctx.getSender();
            PowerArmorConfiguration.openConfigurationGui(playerEntity);
        });
        ctx.setPacketHandled(true);
    }
}
