package mcjty.ariente.compat.jei;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import mcjty.ariente.Ariente;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.recipes.ConstructorRecipe;
import mcjty.ariente.setup.Registration;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class JeiConstructorRecipeCategory implements IRecipeCategory<ConstructorRecipe> {

    private final IDrawable background;
    private final IDrawable icon;

    public JeiConstructorRecipeCategory(IGuiHelper helper) {
        background = helper.createDrawable(Identifier.fromNamespaceAndPath(Ariente.MODID, "textures/gui/recipe_background.png"), 0, 0, 160, 80);
        icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Registration.CONSTRUCTOR.get()));
    }

    @Override
    public RecipeType<ConstructorRecipe> getRecipeType() {
        return JeiPlugin.CONSTRUCTOR;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.ariente.constructor");
    }

    @Override
    public int getWidth() {
        return background.getWidth();
    }

    @Override
    public int getHeight() {
        return background.getHeight();
    }

    @Override
    public void draw(ConstructorRecipe recipe, mezz.jei.api.gui.ingredient.IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        background.draw(graphics);
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ConstructorRecipe recipe, IFocusGroup focuses) {
        for (int index = 0; index < recipe.getIngredientList().size(); index++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 23 + (index / 3) * 18, 14 + (index % 3) * 18)
                    .addItemStack(recipe.getIngredientList().get(index));
        }
        builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 126, 14)
                .addItemStack(BlueprintItem.makeBluePrint(recipe.getDestination()))
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("jei.ariente.blueprint_required")));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 126, 50).addItemStack(recipe.getDestination());
    }
}
