package mcjty.ariente.blocks.storagecube;

import mcjty.ariente.blocks.storagecube.client.StorageCubeClient;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record PacketStorageCubeData(BlockPos pos, int entityId, int size, long revision, boolean reply, List<Entry> changes) {

    public PacketStorageCubeData(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarLong(), buffer.readBoolean(), readEntries(buffer));
    }

    private static List<Entry> readEntries(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        if (count < 0 || count > StorageCubeTile.MAX_SLOTS + 36) {
            throw new IllegalArgumentException("Invalid storage update size");
        }
        List<Entry> changes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            changes.add(new Entry(buffer.readVarInt(), buffer.readItem()));
        }
        return changes;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeVarInt(entityId);
        buffer.writeVarInt(size);
        buffer.writeVarLong(revision);
        buffer.writeBoolean(reply);
        buffer.writeVarInt(changes.size());
        for (Entry entry : changes) {
            buffer.writeVarInt(entry.slot());
            buffer.writeItem(entry.stack());
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> StorageCubeClient.receive(this)));
        context.setPacketHandled(true);
    }

    public record Entry(int slot, ItemStack stack) {
    }
}
