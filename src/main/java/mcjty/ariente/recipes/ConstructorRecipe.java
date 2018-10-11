package mcjty.ariente.recipes;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConstructorRecipe {

    private final ItemStack destination;
    private final List<ItemStack> ingredients;

    public ConstructorRecipe(ItemStack destination, ItemStack... ingredients) {
        this.destination = destination;
        this.ingredients = new ArrayList<>(ingredients.length);
        Collections.addAll(this.ingredients, ingredients);
    }

    public ItemStack getDestination() {
        return destination;
    }

    public List<ItemStack> getIngredients() {
        return ingredients;
    }
}
