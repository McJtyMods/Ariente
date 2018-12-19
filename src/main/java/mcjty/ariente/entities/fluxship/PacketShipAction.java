package mcjty.ariente.entities.fluxship;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketShipAction implements IMessage {

    private FlyAction action;

    @Override
    public void fromBytes(ByteBuf buf) {
        action = FlyAction.values()[buf.readInt()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(action.ordinal());
    }

    public PacketShipAction() {
    }

    public PacketShipAction(FlyAction action) {
        this.action = action;
    }

    public static class Handler implements IMessageHandler<PacketShipAction, IMessage> {
        @Override
        public IMessage onMessage(PacketShipAction message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketShipAction message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            Entity ridingEntity = player.getRidingEntity();
            if (ridingEntity instanceof FluxShipEntity) {
                ((FluxShipEntity) ridingEntity).handleAction(message.action);
            }
        }
    }
}
