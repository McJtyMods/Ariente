package mcjty.ariente.blocks.utility.autofield;

import mcjty.lib.multipart.PartPos;
import mcjty.lib.multipart.PartSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class AutoFieldRenderInfo {

    private final Map<TransferPath, TreeSet<Transfer>> transfers = new HashMap<>();

    private final Random random = new Random();

    /// Register a transfer so that it can possibly be rendered by the TESR if needed
    public void registerTransfer(@Nonnull PartPos sourcePos, @Nonnull PartPos destPos, @Nonnull ItemStack stack) {
        if (random.nextInt(10) != 1) {
            return;
        }
        long time = System.currentTimeMillis();
        Transfer transfer = new Transfer(stack.getItem(), 0 /* @todo 1.14 stack.getMetadata()*/, time);
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
        for (TransferPath path : transfers.keySet()) {
            Iterable<Transfer> treeSet = this.transfers.get(path);

            TreeSet<Transfer> newTreeSet = new TreeSet<>();
            for (Transfer tr : treeSet) {
                if (tr.time < time-3000) {
                    // All movements after this one will be too old
                    break;
                }
                newTreeSet.add(tr);
            }

            this.transfers.put(path, newTreeSet);
        }
    }

//    private void cleanupTransfers(long time) {
//        List<Transfer> toRemove = transfers.entrySet().stream()
//                .sorted(Comparator.comparingLong(Map.Entry::getValue))
//                .filter(e -> e.getValue() < time - 5000)
//                .map(Map.Entry::getKey)
//                .collect(Collectors.toList());
//        for (Transfer transfer : toRemove) {
//            transfers.remove(transfer);
//        }
//        System.out.println("transfers.size() = " + transfers.size());
//    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(transfers.size());
        for (Map.Entry<TransferPath, TreeSet<Transfer>> entry : transfers.entrySet()) {
            TransferPath path = entry.getKey();
            buf.writeBlockPos(path.sourcePos.getPos());
            buf.writeByte(path.sourcePos.getSlot().ordinal());
            buf.writeBlockPos(path.destPos.getPos());
            buf.writeByte(path.destPos.getSlot().ordinal());
            Set<Transfer> values = entry.getValue();
            buf.writeInt(values.size());
            for (Transfer transfer : entry.getValue()) {
                int id = Item.getIdFromItem(transfer.item);
                buf.writeInt(id);
                buf.writeInt(transfer.meta);
            }
        }
    }

    public void fromBytes(PacketBuffer buf) {
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
                int id = buf.readInt();
                Item item = Item.getItemById(id);
                int meta = buf.readInt();
                // On the client the time doesn't matter so we put 0 there
                values.add(new Transfer(item, meta, 0));
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
        @Nonnull private final Item item;
        private final int meta;
        private final long time;

        public Transfer(@Nonnull Item item, int meta, long time) {
            this.item = item;
            this.meta = meta;
            this.time = time;
        }

        @Override
        public int compareTo(Transfer transfer) {
            return Long.compare(time, transfer.time);
        }

        @Nonnull
        public Item getItem() {
            return item;
        }

        public int getMeta() {
            return meta;
        }

        public long getTime() {
            return time;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Transfer transfer = (Transfer) o;
            return meta == transfer.meta &&
                    time == transfer.time &&
                    Objects.equals(item, transfer.item);
        }

        @Override
        public int hashCode() {
            return Objects.hash(item, meta, time);
        }
    }

}
