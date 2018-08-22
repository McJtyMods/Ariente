package mcjty.ariente.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class HoloGuiTools {

    public static Integer countItem(EntityPlayer player, Item item) {
        InventoryPlayer inventory = player.inventory;
        int size = inventory.getSizeInventory();
        int cnt = 0;
        for (int i = 0 ; i < size ; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                cnt += stack.getCount();
            }
        }
        return cnt;
    }

}
