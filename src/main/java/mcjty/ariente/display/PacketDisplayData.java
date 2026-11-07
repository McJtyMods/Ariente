package mcjty.ariente.display;

import mcjty.ariente.display.client.DisplayDataReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** One bounded fragment of one module snapshot. Layouts travel separately in block entity updates. */
public record PacketDisplayData(BlockPos pos, long layoutVersion, int index, int snapshotVersion,
                                int totalBytes, int offset, byte[] data) {

    public PacketDisplayData {
        if (index < 0 || index >= DisplayBaseTile.MAX_MODULES || snapshotVersion < 0
                || totalBytes < 1 || totalBytes > DisplayDataCodec.MAX_SNAPSHOT_BYTES
                || offset < 0 || offset % DisplayDataCodec.FRAGMENT_BYTES != 0 || offset >= totalBytes
                || data.length != Math.min(DisplayDataCodec.FRAGMENT_BYTES, totalBytes - offset)) {
            throw new IllegalArgumentException("Invalid display snapshot fragment");
        }
    }

    public PacketDisplayData(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readLong(), buffer.readUnsignedByte(), buffer.readVarInt(),
                buffer.readVarInt(), buffer.readVarInt(), buffer.readByteArray(DisplayDataCodec.FRAGMENT_BYTES));
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeLong(layoutVersion);
        buffer.writeByte(index);
        buffer.writeVarInt(snapshotVersion);
        buffer.writeVarInt(totalBytes);
        buffer.writeVarInt(offset);
        buffer.writeByteArray(data);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> DisplayDataReceiver.receive(this)));
        context.setPacketHandled(true);
    }
}
