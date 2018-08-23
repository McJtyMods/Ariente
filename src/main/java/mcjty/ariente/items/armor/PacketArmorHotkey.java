package mcjty.ariente.items.armor;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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

    public PacketArmorHotkey(int index) {
        this.index = index;
    }

    public static class Handler implements IMessageHandler<PacketArmorHotkey, IMessage> {
        @Override
        public IMessage onMessage(PacketArmorHotkey message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketArmorHotkey message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            PowerSuit.receivedHotkey(playerEntity, message.index);
        }
    }
}
