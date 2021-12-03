package mcjty.ariente.blocks.defense;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketDamageForcefield {
    private BlockPos pos;
    private int index;
    private Vector3d intersection;

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(index);
        buf.writeDouble(intersection.x);
        buf.writeDouble(intersection.y);
        buf.writeDouble(intersection.z);
    }

    public PacketDamageForcefield() {
    }

    public PacketDamageForcefield(PacketBuffer buf) {
        pos = buf.readBlockPos();
        index = buf.readInt();
        intersection = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public PacketDamageForcefield(BlockPos pos, int index, Vector3d intersection) {
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
