package mcjty.ariente.blocks.utility.autofield;

import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.ariente.items.ItemData;
import mcjty.lib.multipart.PartPos;
import mcjty.lib.multipart.PartSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class AutoFieldRenderInfo {

    private final Map<TransferPath, TreeSet<Transfer>> transfers = new HashMap<>();

    private final Random random = new Random();

    /// Register a transfer so that it can possibly be rendered by the TESR if needed
    public void registerTransfer(@Nonnull PartPos sourcePos, @Nonnull PartPos destPos, @Nonnull ItemStack stack) {
        if (random.nextInt(10) != 1) {
            return;
        }
        long time = System.currentTimeMillis();
        Transfer transfer = new Transfer(stack.copyWithCount(1), time);
        TransferPath path = new TransferPath(sourcePos, destPos);
        transfers.computeIfAbsent(path, p -> new TreeSet<>());
        Set<Transfer> treeSet = this.transfers.get(path);
        treeSet.add(transfer);
    }

    @Nullable
    public TransferPath getRandomPath() {
        if (this.transfers.isEmpty()) {
            return null;
        }
        if (transfers.size() == 1) {
            return transfers.keySet().iterator().next();
        }
        List<TransferPath> paths = new ArrayList<>(this.transfers.keySet());
        return paths.get(random.nextInt(paths.size()));
    }

    @Nullable
    public Transfer getRandomTransfer(TransferPath path) {
        TreeSet<Transfer> values = this.transfers.get(path);
        if (values.isEmpty()) {
            return null;
        }
        if (values.size() == 1) {
            return values.first();
        }
        // We iterate over the values and with a 50% chance return every one. That means
        // the first one will have most chance to be returned
        while (true) {
            for (Transfer value : values) {
                if (random.nextFloat() < .5) {
                    return value;
                }
            }
        }
    }

    public void cleanOldTransfers() {
        long time = System.currentTimeMillis();
        transfers.values().forEach(values -> values.removeIf(transfer -> transfer.time < time - 3000));
        transfers.values().removeIf(Set::isEmpty);
    }

    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeInt(transfers.size());
        for (Map.Entry<TransferPath, TreeSet<Transfer>> entry : transfers.entrySet()) {
            TransferPath path = entry.getKey();
            buf.writeBlockPos(path.sourcePos.pos());
            buf.writeByte(path.sourcePos.slot().ordinal());
            buf.writeBlockPos(path.destPos.pos());
            buf.writeByte(path.destPos.slot().ordinal());
            Set<Transfer> values = entry.getValue();
            buf.writeInt(values.size());
            for (Transfer transfer : entry.getValue()) {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, transfer.stack);
            }
        }
    }

    public void fromBytes(RegistryFriendlyByteBuf buf) {
        transfers.clear();
        int size = buf.readInt();
        for (int i = 0 ; i < size ; i++) {
            BlockPos sourcePos = buf.readBlockPos();
            PartSlot sourceSlot = PartSlot.VALUES[buf.readByte()];
            BlockPos destPos = buf.readBlockPos();
            PartSlot destSlot = PartSlot.VALUES[buf.readByte()];
            TransferPath path = new TransferPath(PartPos.create(sourcePos, sourceSlot), PartPos.create(destPos, destSlot));
            TreeSet<Transfer> values = new TreeSet<>();
            int s = buf.readInt();
            for (int j = 0 ; j < s ; j++) {
                values.add(new Transfer(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf), j));
            }
            transfers.put(path, values);
        }
    }

    public static class TransferPath {
        @Nonnull private final PartPos sourcePos;
        @Nonnull private final PartPos destPos;

        public TransferPath(@Nonnull PartPos sourcePos, @Nonnull PartPos destPos) {
            this.sourcePos = sourcePos;
            this.destPos = destPos;
        }

        @Nonnull
        public PartPos getSourcePos() {
            return sourcePos;
        }

        @Nonnull
        public PartPos getDestPos() {
            return destPos;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TransferPath that = (TransferPath) o;
            return Objects.equals(sourcePos, that.sourcePos) &&
                    Objects.equals(destPos, that.destPos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourcePos, destPos);
        }
    }

    public static class Transfer implements Comparable<Transfer> {
        @Nonnull private final ItemStack stack;
        private final long time;

        public Transfer(@Nonnull ItemStack stack, long time) {
            this.stack = stack;
            this.time = time;
        }

        @Override
        public int compareTo(Transfer transfer) {
            return Long.compare(time, transfer.time);
        }

        @Nonnull
        public ItemStack getStack() {
            return stack;
        }

        public long getTime() {
            return time;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Transfer transfer = (Transfer) o;
            return time == transfer.time && ItemStack.matches(stack, transfer.stack);
        }

        @Override
        public int hashCode() {
            return Objects.hash(stack.getItem(), ItemData.get(stack), time);
        }
    }

}
