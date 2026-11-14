package mcjty.ariente.teleport;

import mcjty.ariente.display.DisplayDataCodec;
import mcjty.ariente.teleport.client.WarperPreviewCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Selected preview only; large terrain data never enters an item stack's share tag. */
public record PacketWarperPreview(UUID card, CompoundTag destination, CompoundTag scene) {

    public PacketWarperPreview(FriendlyByteBuf buffer) {
        this(buffer.readUUID(), buffer.readNbt(), DisplayDataCodec.decode(buffer.readByteArray(DisplayDataCodec.MAX_SNAPSHOT_BYTES)));
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUUID(card);
        buffer.writeNbt(destination);
        buffer.writeByteArray(DisplayDataCodec.encode(scene));
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> WarperPreviewCache.receive(this)));
        context.setPacketHandled(true);
    }
}
