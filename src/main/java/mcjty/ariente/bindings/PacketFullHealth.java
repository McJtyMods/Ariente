package mcjty.ariente.bindings;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketFullHealth implements IMessage {

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public PacketFullHealth() {
    }

    public static class Handler implements IMessageHandler<PacketFullHealth, IMessage> {
        @Override
        public IMessage onMessage(PacketFullHealth message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketFullHealth message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            playerEntity.heal(100);
        }
    }
}
