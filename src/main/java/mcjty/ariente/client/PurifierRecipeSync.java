package mcjty.ariente.client;

import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes;

import java.util.List;
import mcjty.ariente.Ariente;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.recipes.PurifierRecipes;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class PurifierRecipeSync {

    public static void received(SynchronizedRecipes recipes) {
        PurifierRecipes.receivedRecipes(recipes.getAllOfType(BlueprintRecipeRegistry.PURIFYING.get()).stream().map(RecipeHolder::value).toList());
    }

    public static void disconnected() {
        PurifierRecipes.receivedRecipes(List.of());
    }
}
