package mcjty.ariente.blocks.utility.autofield;

import java.util.List;
import java.util.ArrayList;
import mcjty.lib.varia.ItemStackList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class SimpleItemHandler implements ResourceHandler<ItemResource> {

    private final ItemStackList list;

    public SimpleItemHandler(ItemStackList list) {
        this.list = list;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public ItemResource getResource(int index) {
        return ItemResource.of(list.get(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return list.get(index).getCount();
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return resource.isEmpty() ? 64 : resource.getMaxStackSize();
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return true;
    }

    private final SnapshotJournal<List<ItemStack>> journal = new SnapshotJournal<>() {

        @Override
        protected List<ItemStack> createSnapshot() {
            var result = new ArrayList<ItemStack>();
            for (int index = 0; index < list.size(); index++) result.add(list.get(index).copy());
            return result;
        }

        @Override
        protected void revertToSnapshot(List<ItemStack> snapshot) {
            for (int index = 0; index < snapshot.size(); index++) list.set(index, snapshot.get(index));
        }
    };

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        var current = list.get(index);
        if (!current.isEmpty() && !resource.matches(current)) return 0;
        int inserted = (int) Math.min(amount, Math.max(0, getCapacityAsLong(index, resource) - current.getCount()));
        if (inserted > 0) {
            journal.updateSnapshots(transaction);
            list.set(index, resource.toStack(current.getCount() + inserted));
        }
        return inserted;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        var current = list.get(index);
        int extracted = resource.matches(current) ? Math.min(amount, current.getCount()) : 0;
        if (extracted > 0) {
            journal.updateSnapshots(transaction);
            list.set(index, current.copyWithCount(current.getCount() - extracted));
        }
        return extracted;
    }

    public void setItemStack(int slot, ItemStack stack) {
        list.set(slot, stack);
    }

}
