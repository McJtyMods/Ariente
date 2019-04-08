package mcjty.ariente.blocks.defense;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.function.Supplier;

public class PacketDamageForcefield implements IMessage {
    private BlockPos pos;
    private int index;
    private Vec3d intersection;

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
        index = buf.readInt();
        intersection = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
        buf.writeInt(index);
        buf.writeDouble(intersection.x);
        buf.writeDouble(intersection.y);
        buf.writeDouble(intersection.z);
    }

    public PacketDamageForcefield() {
    }

    public PacketDamageForcefield(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketDamageForcefield(BlockPos pos, int index, Vec3d intersection) {
        this.pos = pos;
        this.index = index;
        this.intersection = intersection;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ForceFieldRenderer.damageField(pos, index, intersection);
        });
        ctx.setPacketHandled(true);
    }
}
