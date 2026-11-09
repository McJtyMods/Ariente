package mcjty.ariente.blocks.storagecube;

import net.neoforged.neoforge.transfer.item.ItemUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import mcjty.lib.container.GenericItemHandler;


/** Reorder complete stacks without changing their counts, tags or item capabilities. */
public class StorageCubeSort {

    public static void apply(GenericItemHandler inventory, boolean byCount) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = ItemUtil.getStack(inventory, slot);
            if (!stack.isEmpty()) {
                stacks.add(stack.copy());
            }
        }
        Comparator<ItemStack> names = Comparator.comparing(stack -> stack.getHoverName().getString(), String.CASE_INSENSITIVE_ORDER);
        names = names.thenComparing(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        Comparator<ItemStack> order = byCount
                ? Comparator.comparingInt(ItemStack::getCount).reversed().thenComparing(names)
                : names.thenComparing(Comparator.comparingInt(ItemStack::getCount).reversed());
        stacks.sort(order);
        for (int slot = 0; slot < inventory.size(); slot++) {
            inventory.setStackInSlot(slot, slot < stacks.size() ? stacks.get(slot) : ItemStack.EMPTY);
        }
    }
}
