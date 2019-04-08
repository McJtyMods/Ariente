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
import java.util.stream.Collectors;

public class AutoFieldRenderInfo {

    private final Map<Transfer, Long> transfers = new HashMap<>();

    private final Random random = new Random();

    /// Register a transfer so that it can possibly be rendered by the TESR if needed
    public void registerTransfer(@Nonnull PartPos sourcePos, @Nonnull PartPos destPos, @Nonnull ItemStack stack) {
        if (random.nextInt(10) != 1) {
            return;
        }
        Transfer transfer = new Transfer(sourcePos, destPos, stack.getItem(), stack.getMetadata());
        long time = System.currentTimeMillis();
        transfers.put(transfer, time);
        if (transfers.size() > 100) {
            cleanupTransfers(time);
        }
    }

    @Nullable
    public Transfer getRandomTransfer() {
        if (this.transfers.isEmpty()) {
            return null;
        }
        if (this.transfers.size() == 1) {
            return this.transfers.entrySet().iterator().next().getKey();
        }
        List<Transfer> transfers = new ArrayList<>(this.transfers.keySet());
        return transfers.get(random.nextInt(transfers.size()));
    }

    private void cleanupTransfers(long time) {
        List<Transfer> toRemove = transfers.entrySet().stream()
                .sorted(Comparator.comparingLong(Map.Entry::getValue))
                .filter(e -> e.getValue() < time - 5000)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        for (Transfer transfer : toRemove) {
            transfers.remove(transfer);
        }
        System.out.println("transfers.size() = " + transfers.size());
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(transfers.size());
        for (Map.Entry<Transfer, Long> entry : transfers.entrySet()) {
            Transfer transfer = entry.getKey();
            NetworkTools.writePos(buf, transfer.sourcePos.getPos());
            buf.writeByte(transfer.sourcePos.getSlot().ordinal());
            NetworkTools.writePos(buf, transfer.destPos.getPos());
            buf.writeByte(transfer.destPos.getSlot().ordinal());
            int id = Item.getIdFromItem(transfer.item);
//            System.out.println("to: id = " + id);
            buf.writeInt(id);
            buf.writeInt(transfer.meta);
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
            int id = buf.readInt();
//            System.out.println("from: id = " + id);
            Item item = Item.getItemById(id);
            int meta = buf.readInt();
            // On the client the time doesn't matter so we put long there
            transfers.put(new Transfer(PartPos.create(sourcePos, sourceSlot), PartPos.create(destPos, destSlot), item, meta), 0L);
        }
    }

    public static class Transfer {
        @Nonnull private final PartPos sourcePos;
        @Nonnull private final PartPos destPos;
        @Nonnull private final Item item;
        private final int meta;

        public Transfer(@Nonnull PartPos sourcePos, @Nonnull PartPos destPos, @Nonnull Item item, int meta) {
            this.sourcePos = sourcePos;
            this.destPos = destPos;
            this.item = item;
            this.meta = meta;
        }

        @Nonnull
        public PartPos getSourcePos() {
            return sourcePos;
        }

        @Nonnull
        public PartPos getDestPos() {
            return destPos;
        }

        @Nonnull
        public Item getItem() {
            return item;
        }

        public int getMeta() {
            return meta;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Transfer transfer = (Transfer) o;
            return meta == transfer.meta &&
                    Objects.equals(sourcePos, transfer.sourcePos) &&
                    Objects.equals(destPos, transfer.destPos) &&
                    Objects.equals(item, transfer.item);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourcePos, destPos, item, meta);
        }
    }

}
