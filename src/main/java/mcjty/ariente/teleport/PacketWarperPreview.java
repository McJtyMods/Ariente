package mcjty.ariente.teleport;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import mcjty.ariente.display.DisplayDataCodec;
import mcjty.ariente.teleport.client.WarperPreviewCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.UUID;

/** Selected preview only; large terrain data never enters an item stack's share tag. */
public record PacketWarperPreview(UUID card, CompoundTag destination, CompoundTag scene) implements CustomPacketPayload {

    public static final Type<PacketWarperPreview> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ariente", "warper_preview"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketWarperPreview> CODEC = StreamCodec.ofMember(PacketWarperPreview::toBytes, PacketWarperPreview::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public PacketWarperPreview(RegistryFriendlyByteBuf buffer) {
        this(buffer.readUUID(), buffer.readNbt(), DisplayDataCodec.decode(buffer.readByteArray(DisplayDataCodec.MAX_SNAPSHOT_BYTES)));
    }

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeUUID(card);
        buffer.writeNbt(destination);
        buffer.writeByteArray(DisplayDataCodec.encode(scene));
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> WarperPreviewCache.receive(this));
    }
}
