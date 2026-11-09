package mcjty.ariente.blocks.storagecube;

import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import mcjty.lib.varia.InventoryTools;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.RangedResourceHandler;

public class StorageCubeTransfer {

    private static void set(StorageCubeTile tile, Player player, int slot, ItemStack stack) {
        int size = tile.getItems().size();
        if (slot < size) {
            tile.getItems().setStackInSlot(slot, stack);
        } else {
            player.getInventory().setItem(slot - size, stack);
        }
    }

    public static boolean apply(StorageCubeTile tile, Player player, int source, int destination, int amount,
                                boolean quick, ItemStack expectedSource, ItemStack expectedDestination) {
        if (tile.isLocked()) {
            return false;
        }
        int size = tile.getItems().size();
        ResourceHandler<ItemResource> inventory = RangedResourceHandler.of(VanillaContainerWrapper.of(player.getInventory()), 0, 36);
        var all = new CombinedResourceHandler<>(tile.getItems(), inventory);
        if (source < 0 || source >= all.size() || amount <= 0 || amount > 64) {
            return false;
        }
        ItemStack from = ItemUtil.getStack(all, source);
        // No item leaves its source until the drop is accepted. Closing, disconnecting and dying
        // during a drag therefore need no cursor recovery, and automation cannot duplicate a drag.
        if (from.isEmpty() || !ItemStack.matches(from, expectedSource)) {
            return false;
        }
        if (quick) {
            ResourceHandler<ItemResource> target = source < size ? inventory : tile.getItems();
            ItemStack remaining = InventoryTools.insertItem(target, from.copy(), false);
            set(tile, player, source, remaining);
        } else {
            if (destination < 0 || destination >= all.size() || destination == source) {
                return false;
            }
            ItemStack to = ItemUtil.getStack(all, destination);
            if (!ItemStack.matches(to, expectedDestination)) {
                return false;
            }
            int moved = Math.min(amount, from.getCount());
            if (to.isEmpty() || ItemStack.isSameItemSameComponents(from, to)) {
                moved = Math.min(moved, Math.min(64, from.getMaxStackSize()) - to.getCount());
                if (moved <= 0) {
                    return false;
                }
                set(tile, player, destination, from.copyWithCount(to.getCount() + moved));
                set(tile, player, source, from.copyWithCount(from.getCount() - moved));
            } else if (moved == from.getCount() && to.getCount() <= Math.min(64, to.getMaxStackSize())) {
                set(tile, player, source, to.copy());
                set(tile, player, destination, from.copy());
            } else {
                return false;
            }
        }
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        return true;
    }
}
