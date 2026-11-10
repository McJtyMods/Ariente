package mcjty.ariente.client;

import java.util.List;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.recipes.PurifierRecipes;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

public final class PurifierRecipeSync {

    public static void received(RecipeManager recipes) {
        PurifierRecipes.receivedRecipes(
                recipes.getAllRecipesFor(BlueprintRecipeRegistry.PURIFYING.get()).stream()
                        .map(RecipeHolder::value)
                        .toList());
    }

    public static void disconnected() {
        PurifierRecipes.receivedRecipes(List.of());
    }
}
