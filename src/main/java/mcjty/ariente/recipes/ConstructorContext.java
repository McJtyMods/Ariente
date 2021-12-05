package mcjty.ariente.recipes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class ConstructorContext implements IInventory {

    @Override
    public int getContainerSize() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getItem(int index) {
        return null;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return null;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return null;
    }

    @Override
    public void setItem(int index, ItemStack stack) {

    }

    @Override
    public void setChanged() {

    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return false;
    }

    @Override
    public void clearContent() {

    }
}
