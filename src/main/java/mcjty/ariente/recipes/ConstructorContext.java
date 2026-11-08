package mcjty.ariente.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public class ConstructorContext implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public int size() {
        return 0;
    }
}
