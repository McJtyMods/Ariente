package mcjty.ariente.blocks.utility.autofield;

import mcjty.lib.multipart.PartPos;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AutoFieldRenderInfo {

    private final Map<Transfer, Long> transfers = new HashMap<>();

    /// Register a transfer so that it can possibly be rendered by the TESR if needed
    public void registerTransfer(@Nonnull PartPos sourcePos, @Nonnull PartPos destPos, @Nonnull ItemStack stack) {
        Transfer transfer = new Transfer(sourcePos, destPos, stack.getItem());
        long time = System.currentTimeMillis();
        transfers.put(transfer, time);
    }

    public static class Transfer {
        @Nonnull private final PartPos sourcePos;
        @Nonnull private final PartPos destPos;
        @Nonnull private final Item item;

        public Transfer(@Nonnull PartPos sourcePos, @Nonnull PartPos destPos, @Nonnull Item item) {
            this.sourcePos = sourcePos;
            this.destPos = destPos;
            this.item = item;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Transfer transfer = (Transfer) o;
            return Objects.equals(sourcePos, transfer.sourcePos) &&
                    Objects.equals(destPos, transfer.destPos) &&
                    Objects.equals(item, transfer.item);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourcePos, destPos, item);
        }
    }

}
