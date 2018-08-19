package mcjty.ariente.items.armor;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketConfigureArmor implements IMessage {

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public PacketConfigureArmor() {
    }

    public static class Handler implements IMessageHandler<PacketConfigureArmor, IMessage> {
        @Override
        public IMessage onMessage(PacketConfigureArmor message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketConfigureArmor message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            PowerArmorConfiguration.openConfigurationGui(playerEntity);
        }
    }
}
