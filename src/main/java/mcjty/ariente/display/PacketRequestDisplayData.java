package mcjty.ariente.display;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Requested after the client has applied a layout, including layouts received with a chunk. */
public record PacketRequestDisplayData(BlockPos pos, long layoutVersion, int modules) {

    public PacketRequestDisplayData(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readLong(), buffer.readInt());
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeLong(layoutVersion);
        buffer.writeInt(modules);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                DisplayDataSender.request(context.getSender(), this);
            }
        });
        context.setPacketHandled(true);
    }
}
