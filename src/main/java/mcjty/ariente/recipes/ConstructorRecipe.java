package mcjty.ariente.recipes;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConstructorRecipe {

    private final ItemStack destination;
    private final List<ItemStack> ingredients;
    private final float chance;

    public ConstructorRecipe(ItemStack destination, float chance, ItemStack... ingredients) {
        this.destination = destination;
        this.ingredients = new ArrayList<>(ingredients.length);
        this.chance = chance;
        Collections.addAll(this.ingredients, ingredients);
    }

    public float getChance() {
        return chance;
    }

    public ItemStack getDestination() {
        return destination;
    }

    public List<ItemStack> getIngredients() {
        return ingredients;
    }
}
