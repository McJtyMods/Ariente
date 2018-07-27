package mcjty.ariente.blocks.defense;

import io.netty.buffer.ByteBuf;
import mcjty.ariente.Ariente;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketDamageForcefield implements IMessage {
    private BlockPos pos;
    private int index;
    private Vec3d intersection;

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        index = buf.readInt();
        intersection = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(index);
        buf.writeDouble(intersection.x);
        buf.writeDouble(intersection.y);
        buf.writeDouble(intersection.z);
    }

    public PacketDamageForcefield() {
    }

    public PacketDamageForcefield(BlockPos pos, int index, Vec3d intersection) {
        this.pos = pos;
        this.index = index;
        this.intersection = intersection;
    }

    public static class Handler implements IMessageHandler<PacketDamageForcefield, IMessage> {
        @Override
        public IMessage onMessage(PacketDamageForcefield message, MessageContext ctx) {
            Ariente.proxy.addScheduledTaskClient(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketDamageForcefield message, MessageContext ctx) {
            ForceFieldRenderer.damageField(message.pos, message.index, message.intersection);
        }
    }
}
