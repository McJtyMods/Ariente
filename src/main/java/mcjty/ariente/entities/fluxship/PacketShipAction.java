package mcjty.ariente.entities.fluxship;

import io.netty.buffer.ByteBuf;
import mcjty.lib.thirteen.Context;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.function.Supplier;

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

    public PacketShipAction(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketShipAction(FlyAction action) {
        this.action = action;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            EntityPlayerMP player = ctx.getSender();
            Entity ridingEntity = player.getRidingEntity();
            if (ridingEntity instanceof FluxShipEntity) {
                ((FluxShipEntity) ridingEntity).handleAction(action);
            }
        });
        ctx.setPacketHandled(true);
    }
}
