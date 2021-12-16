package mcjty.ariente.blocks.defense;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketDamageForcefield {
    private BlockPos pos;
    private int index;
    private Vec3 intersection;

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(index);
        buf.writeDouble(intersection.x);
        buf.writeDouble(intersection.y);
        buf.writeDouble(intersection.z);
    }

    public PacketDamageForcefield() {
    }

    public PacketDamageForcefield(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        index = buf.readInt();
        intersection = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public PacketDamageForcefield(BlockPos pos, int index, Vec3 intersection) {
        this.pos = pos;
        this.index = index;
        this.intersection = intersection;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ForceFieldRenderer.damageField(pos, index, intersection);
        });
        ctx.setPacketHandled(true);
    }
}
