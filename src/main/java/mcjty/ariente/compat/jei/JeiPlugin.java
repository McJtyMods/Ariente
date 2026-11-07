package mcjty.ariente.compat.jei;

import mcjty.ariente.Ariente;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.recipes.BlueprintExchangeRecipe;
import mcjty.ariente.recipes.ConstructorRecipe;
import mcjty.ariente.recipes.PurifyingRecipe;
import mcjty.ariente.recipes.PurifierRecipes;
import net.minecraft.client.Minecraft;
import mcjty.ariente.setup.Registration;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {

    public static final RecipeType<ConstructorRecipe> CONSTRUCTOR = RecipeType.create(Ariente.MODID, "constructor", ConstructorRecipe.class);
    public static final RecipeType<PurifyingRecipe> PURIFYING = RecipeType.create(Ariente.MODID, "purifying", PurifyingRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Ariente.MODID, "constructor");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(Registration.BLUEPRINT.get(), (stack, context) -> {
            if (BlueprintExchangeRecipe.isPreview(stack)) {
                return "exchange";
            }
            ConstructorRecipe recipe = BlueprintRecipeRegistry.findRecipe(BlueprintItem.getDestination(stack));
            return recipe == null ? "empty" : recipe.getId().toString();
        });
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new JeiConstructorRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new JeiPurifyingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(CONSTRUCTOR, BlueprintRecipeRegistry.getRecipes());
        if (Minecraft.getInstance().level != null) {
            registration.addRecipes(PURIFYING, PurifierRecipes.getRecipes(Minecraft.getInstance().level));
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Registration.CONSTRUCTOR.get()), CONSTRUCTOR);
        registration.addRecipeCatalyst(new ItemStack(Registration.AUTO_CONSTRUCTOR.get()), CONSTRUCTOR);
        registration.addRecipeCatalyst(new ItemStack(Registration.PURIFIER_START.get()), PURIFYING);
        registration.addRecipeCatalyst(new ItemStack(Registration.PURIFIER_FINISH.get()), PURIFYING);
        registration.addRecipeCatalyst(new ItemStack(Registration.PURIFIER_TUBE.get()), PURIFYING);
    }
}
