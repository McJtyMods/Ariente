package mcjty.ariente.compat.jei;

import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.recipes.ConstructorRecipe;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mezz.jei.api.*;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

//@JEIPlugin
public class JeiPlugin {} /* @todo 1.14 implements IModPlugin {

    public static final String ARIENTE_CRAFTING_ID = "Ariente.Crafting";

    @Override
    public void register(@Nonnull IModRegistry registry) {
        registerGrindstoneHandling(registry);
    }

    private void registerGrindstoneHandling(@Nonnull IModRegistry registry) {
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.constructorBlock), ARIENTE_CRAFTING_ID);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.autoConstructorBlock), ARIENTE_CRAFTING_ID);

        List<JeiConstructorRecipe> recipes = new ArrayList<>();
        for (ConstructorRecipe recipe : BlueprintRecipeRegistry.getRecipes()) {
            recipes.add(new JeiConstructorRecipe(recipe));
        }
        registry.addRecipes(recipes, ARIENTE_CRAFTING_ID);
        registry.handleRecipes(JeiConstructorRecipe.class, JeiConstructorRecipeWrapper::new, ARIENTE_CRAFTING_ID);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IJeiHelpers helpers = registry.getJeiHelpers();
        IGuiHelper guiHelper = helpers.getGuiHelper();

        registry.addRecipeCategories(new JeiConstructorRecipeCategory(guiHelper));
    }

}
*/