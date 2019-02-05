package mcjty.ariente.blocks.utility.autofield;

import mcjty.lib.varia.ItemStackList;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

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

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
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
