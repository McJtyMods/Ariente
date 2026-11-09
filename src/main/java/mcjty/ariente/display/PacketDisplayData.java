package mcjty.ariente.display;

import mcjty.ariente.display.client.DisplayDataReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import mcjty.lib.network.PayloadContext;

/** One bounded fragment of one module snapshot. Layouts travel separately in block entity updates. */
public record PacketDisplayData(BlockPos pos, long layoutVersion, int index, int snapshotVersion,
                                int totalBytes, int offset, byte[] data) implements CustomPacketPayload {

    public static final Type<PacketDisplayData> TYPE = new Type<>(Identifier.fromNamespaceAndPath("ariente", "packet_display_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketDisplayData> CODEC = StreamCodec.ofMember(PacketDisplayData::toBytes, PacketDisplayData::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public PacketDisplayData {
        if (index < 0 || index >= DisplayBaseTile.MAX_MODULES || snapshotVersion < 0
                || totalBytes < 1 || totalBytes > DisplayDataCodec.MAX_SNAPSHOT_BYTES
                || offset < 0 || offset % DisplayDataCodec.FRAGMENT_BYTES != 0 || offset >= totalBytes
                || data.length != Math.min(DisplayDataCodec.FRAGMENT_BYTES, totalBytes - offset)) {
            throw new IllegalArgumentException("Invalid display snapshot fragment");
        }
    }

    public PacketDisplayData(RegistryFriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readLong(), buffer.readUnsignedByte(), buffer.readVarInt(),
                buffer.readVarInt(), buffer.readVarInt(), buffer.readByteArray(DisplayDataCodec.FRAGMENT_BYTES));
    }

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeLong(layoutVersion);
        buffer.writeByte(index);
        buffer.writeVarInt(snapshotVersion);
        buffer.writeVarInt(totalBytes);
        buffer.writeVarInt(offset);
        buffer.writeByteArray(data);
    }

    public void handle(PayloadContext context) {
        context.enqueueWork(() -> DisplayDataReceiver.receive(this));
    }
}
