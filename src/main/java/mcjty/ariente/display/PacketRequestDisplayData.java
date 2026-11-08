package mcjty.ariente.display;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Requested after the client has applied a layout, including layouts received with a chunk. */
public record PacketRequestDisplayData(BlockPos pos, long layoutVersion, int modules) implements CustomPacketPayload {

    public static final Type<PacketRequestDisplayData> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ariente", "packet_request_display_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketRequestDisplayData> CODEC = StreamCodec.ofMember(PacketRequestDisplayData::toBytes, PacketRequestDisplayData::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public PacketRequestDisplayData(RegistryFriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readLong(), buffer.readInt());
    }

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeLong(layoutVersion);
        buffer.writeInt(modules);
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if ((ServerPlayer) context.player() != null) {
                DisplayDataSender.request((ServerPlayer) context.player(), this);
            }
        });
    }
}
