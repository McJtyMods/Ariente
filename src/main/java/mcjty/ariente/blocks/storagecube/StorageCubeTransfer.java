package mcjty.ariente.blocks.storagecube;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

public class StorageCubeTransfer {

    public static boolean apply(StorageCubeTile tile, Player player, int source, int destination, int amount,
                                boolean quick, ItemStack expectedSource, ItemStack expectedDestination) {
        if (tile.isLocked()) {
            return false;
        }
        int size = tile.getItems().getSlots();
        IItemHandlerModifiable inventory = new RangedWrapper(new InvWrapper(player.getInventory()), 0, 36);
        CombinedInvWrapper all = new CombinedInvWrapper(tile.getItems(), inventory);
        if (source < 0 || source >= all.getSlots() || amount <= 0 || amount > 64) {
            return false;
        }
        ItemStack from = all.getStackInSlot(source);
        // No item leaves its source until the drop is accepted. Closing, disconnecting and dying
        // during a drag therefore need no cursor recovery, and automation cannot duplicate a drag.
        if (from.isEmpty() || !ItemStack.matches(from, expectedSource)) {
            return false;
        }
        if (quick) {
            IItemHandler target = source < size ? inventory : tile.getItems();
            ItemStack remaining = ItemHandlerHelper.insertItemStacked(target, from.copy(), false);
            all.setStackInSlot(source, remaining);
        } else {
            if (destination < 0 || destination >= all.getSlots() || destination == source) {
                return false;
            }
            ItemStack to = all.getStackInSlot(destination);
            if (!ItemStack.matches(to, expectedDestination)) {
                return false;
            }
            int moved = Math.min(amount, from.getCount());
            if (to.isEmpty() || ItemStack.isSameItemSameComponents(from, to)) {
                moved = Math.min(moved, Math.min(all.getSlotLimit(destination), from.getMaxStackSize()) - to.getCount());
                if (moved <= 0) {
                    return false;
                }
                all.setStackInSlot(destination, from.copyWithCount(to.getCount() + moved));
                all.setStackInSlot(source, from.copyWithCount(from.getCount() - moved));
            } else if (moved == from.getCount() && to.getCount() <= Math.min(all.getSlotLimit(source), to.getMaxStackSize())) {
                all.setStackInSlot(source, to.copy());
                all.setStackInSlot(destination, from.copy());
            } else {
                return false;
            }
        }
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        return true;
    }
}
