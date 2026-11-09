package mcjty.ariente.recipes;

import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import mcjty.lib.container.IItemHandler;

public class ConstructorCrafting {

    /** Reserve each input only once, including recipes with repeated ingredients. */
    @Nullable
    public static int[] planIngredients(ConstructorRecipe recipe, IItemHandler inventory, int first, int count) {
        int[] reserved = new int[inventory.getSlots()];
        for (ItemStack ingredient : recipe.getIngredientList()) {
            int needed = ingredient.getCount();
            for (int slot = first; slot < first + count && needed > 0; slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (ItemStack.isSameItem(ingredient, stack)) {
                    int take = Math.min(needed, stack.getCount() - reserved[slot]);
                    reserved[slot] += take;
                    needed -= take;
                }
            }
            if (needed > 0) {
                return null;
            }
        }
        return reserved;
    }
}
