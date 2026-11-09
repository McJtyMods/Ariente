package mcjty.ariente.blocks.utility.autofield;

import javax.annotation.Nonnull;
import mcjty.lib.varia.ItemStackList;
import net.minecraft.world.item.ItemStack;
import mcjty.lib.container.IItemHandler;

public class SimpleItemHandler implements IItemHandler {

    private final ItemStackList list;

    public SimpleItemHandler(ItemStackList list) {
        this.list = list;
    }

    @Override
    public int getSlots() {
        return list.size();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return list.get(slot);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        ItemStack originalStack = list.get(slot);
        if (!originalStack.isEmpty() && !ItemStack.isSameItemSameComponents(stack, originalStack)) {
            return stack;
        }

        if (!simulate) {
            list.set(slot, stack.copy());
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return list.get(slot);
    }

    public void setItemStack(int slot, ItemStack stack) {
        list.set(slot, stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }
}
