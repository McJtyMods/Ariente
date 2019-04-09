package mcjty.ariente.blocks.utility.autofield;

import io.netty.buffer.ByteBuf;
import mcjty.lib.multipart.PartPos;
import mcjty.lib.multipart.PartSlot;
import mcjty.lib.network.NetworkTools;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class AutoFieldRenderInfo {

    private final Map<TransferPath, List<Transfer>> transfers = new HashMap<>();

    private final Random random = new Random();

    /// Register a transfer so that it can possibly be rendered by the TESR if needed
    public void registerTransfer(@Nonnull PartPos sourcePos, @Nonnull PartPos destPos, @Nonnull ItemStack stack) {
        if (random.nextInt(10) != 1) {
            return;
        }
        long time = System.currentTimeMillis();
        Transfer transfer = new Transfer(stack.getItem(), stack.getMetadata(), time);
        TransferPath path = new TransferPath(sourcePos, destPos);
        transfers.computeIfAbsent(path, p -> new ArrayList<>());
        transfers.get(path).add(transfer);
        // @todo
//        if (transfers.size() > 100) {
//            cleanupTransfers(time);
//        }
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
        List<Transfer> values = this.transfers.get(path);
        if (values.isEmpty()) {
            return null;
        }
        if (values.size() == 1) {
            return values.get(0);
        }
        return values.get(random.nextInt(values.size()));
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

    public void toBytes(ByteBuf buf) {
        buf.writeInt(transfers.size());
        for (Map.Entry<TransferPath, List<Transfer>> entry : transfers.entrySet()) {
            TransferPath path = entry.getKey();
            NetworkTools.writePos(buf, path.sourcePos.getPos());
            buf.writeByte(path.sourcePos.getSlot().ordinal());
            NetworkTools.writePos(buf, path.destPos.getPos());
            buf.writeByte(path.destPos.getSlot().ordinal());
            List<Transfer> values = entry.getValue();
            buf.writeInt(values.size());
            for (Transfer transfer : entry.getValue()) {
                int id = Item.getIdFromItem(transfer.item);
                buf.writeInt(id);
                buf.writeInt(transfer.meta);
            }
        }
    }

    public void fromBytes(ByteBuf buf) {
        transfers.clear();
        int size = buf.readInt();
        for (int i = 0 ; i < size ; i++) {
            BlockPos sourcePos = NetworkTools.readPos(buf);
            PartSlot sourceSlot = PartSlot.VALUES[buf.readByte()];
            BlockPos destPos = NetworkTools.readPos(buf);
            PartSlot destSlot = PartSlot.VALUES[buf.readByte()];
            TransferPath path = new TransferPath(PartPos.create(sourcePos, sourceSlot), PartPos.create(destPos, destSlot));
            List<Transfer> values = new ArrayList<>();
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

    public static class Transfer {
        @Nonnull private final Item item;
        private final int meta;
        private final long time;

        public Transfer(@Nonnull Item item, int meta, long time) {
            this.item = item;
            this.meta = meta;
            this.time = time;
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
