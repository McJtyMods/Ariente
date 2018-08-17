package mcjty.ariente.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketHitForcefield implements IMessage {

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public PacketHitForcefield() {
    }

    public static class Handler implements IMessageHandler<PacketHitForcefield, IMessage> {
        @Override
        public IMessage onMessage(PacketHitForcefield message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketHitForcefield message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            // @todo
        }
    }
}
