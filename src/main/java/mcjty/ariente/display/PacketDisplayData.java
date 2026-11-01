package mcjty.ariente.display;

import mcjty.ariente.display.client.DisplayDataReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Bounded reading deltas. Static module items and text are sent only with layout updates. */
public record PacketDisplayData(BlockPos pos, long layoutVersion, List<Entry> changes) {

    public PacketDisplayData(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readLong(), readChanges(buffer));
    }

    private static List<Entry> readChanges(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        if (count < 0 || count > DisplayBaseTile.MAX_MODULES) {
            throw new IllegalArgumentException("Invalid display update count");
        }
        List<Entry> changes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int index = buffer.readUnsignedByte();
            CompoundTag data = buffer.readNbt();
            if (index >= DisplayBaseTile.MAX_MODULES || data == null) {
                throw new IllegalArgumentException("Invalid display update");
            }
            changes.add(new Entry(index, data));
        }
        return List.copyOf(changes);
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeLong(layoutVersion);
        buffer.writeVarInt(changes.size());
        for (Entry entry : changes) {
            buffer.writeByte(entry.index());
            buffer.writeNbt(entry.data());
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> DisplayDataReceiver.receive(this)));
        context.setPacketHandled(true);
    }

    public record Entry(int index, CompoundTag data) {
    }
}
