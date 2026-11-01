package mcjty.ariente.display;

import mcjty.ariente.display.client.DisplayModuleDrag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record PacketDisplayGestureResult(UUID ticket, BlockPos target, int x, int y) {

    public PacketDisplayGestureResult(FriendlyByteBuf buffer) {
        this(buffer.readUUID(), buffer.readBoolean() ? buffer.readBlockPos() : null, buffer.readVarInt(), buffer.readVarInt());
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUUID(ticket);
        buffer.writeBoolean(target != null);
        if (target != null) {
            buffer.writeBlockPos(target);
        }
        buffer.writeVarInt(x);
        buffer.writeVarInt(y);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> DisplayModuleDrag.complete(ticket, target, x, y)));
        context.setPacketHandled(true);
    }
}
