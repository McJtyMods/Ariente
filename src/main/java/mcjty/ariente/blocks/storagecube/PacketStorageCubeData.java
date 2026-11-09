package mcjty.ariente.blocks.storagecube;

import java.util.ArrayList;
import java.util.List;
import mcjty.ariente.blocks.storagecube.client.StorageCubeClient;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketStorageCubeData(BlockPos pos, int entityId, int size, long revision, boolean reply, List<Entry> changes) implements CustomPacketPayload {

    public static final Type<PacketStorageCubeData> TYPE = new Type<>(Identifier.fromNamespaceAndPath("ariente", "packet_storage_cube_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketStorageCubeData> CODEC = StreamCodec.ofMember(PacketStorageCubeData::toBytes, PacketStorageCubeData::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public PacketStorageCubeData(RegistryFriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarLong(), buffer.readBoolean(), readEntries(buffer));
    }

    private static List<Entry> readEntries(RegistryFriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        if (count < 0 || count > StorageCubeTile.MAX_SLOTS + 36) {
            throw new IllegalArgumentException("Invalid storage update size");
        }
        List<Entry> changes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            changes.add(new Entry(buffer.readVarInt(), ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer)));
        }
        return changes;
    }

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeVarInt(entityId);
        buffer.writeVarInt(size);
        buffer.writeVarLong(revision);
        buffer.writeBoolean(reply);
        buffer.writeVarInt(changes.size());
        for (Entry entry : changes) {
            buffer.writeVarInt(entry.slot());
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, entry.stack());
        }
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> StorageCubeClient.receive(this));
    }

    public record Entry(int slot, ItemStack stack) {
    }
}
