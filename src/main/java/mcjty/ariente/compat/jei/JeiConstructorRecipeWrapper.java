package mcjty.ariente.compat.jei;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class JeiConstructorRecipeWrapper implements ICraftingRecipeWrapper {

    private final JeiConstructorRecipe recipe;

    public JeiConstructorRecipeWrapper(JeiConstructorRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(@Nonnull IIngredients ingredients) {
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipe().getDestination());
        List<ItemStack> inputs = new ArrayList<>(recipe.getRecipe().getIngredients());
        ingredients.setInputs(VanillaTypes.ITEM, inputs);
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        return Collections.emptyList();
    }
}
