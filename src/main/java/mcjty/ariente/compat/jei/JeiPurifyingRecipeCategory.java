package mcjty.ariente.compat.jei;

import mcjty.ariente.config.PowerConfiguration;
import mcjty.ariente.blocks.purifier.PurifierTile;
import mcjty.ariente.recipes.PurifyingRecipe;
import mcjty.ariente.setup.Registration;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class JeiPurifyingRecipeCategory implements IRecipeCategory<PurifyingRecipe> {

    private final IDrawable background;
    private final IDrawable icon;

    public JeiPurifyingRecipeCategory(IGuiHelper helper) {
        background = helper.createBlankDrawable(170, 22 + 22 * PowerConfiguration.PURIFIER_MAX_TUBES.get());
        icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Registration.PURIFIER_TUBE.get()));
    }

    @Override
    public RecipeType<PurifyingRecipe> getRecipeType() {
        return JeiPlugin.PURIFYING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.ariente.purifying");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PurifyingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 3, 20).addIngredients(recipe.ingredient());
        for (int tubes = 1; tubes <= PowerConfiguration.PURIFIER_MAX_TUBES.get(); tubes++) {
            int count = tubes;
            builder.addSlot(RecipeIngredientRole.OUTPUT, 148, 18 + (tubes - 1) * 22)
                    .addItemStack(recipe.result().copyWithCount(PurifierTile.yieldForTubes(tubes)))
                    .addTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("jei.ariente.purifying_cost", count,
                            count * PowerConfiguration.PURIFIER_TICKS_PER_TUBE.get() / 20.0,
                            (long) count * PowerConfiguration.PURIFIER_POWER_PER_TUBE.get())));
        }
    }

    @Override
    public void draw(PurifyingRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, Component.translatable("jei.ariente.purifying_header"), 30, 2, 0x404040, false);
        for (int tubes = 1; tubes <= PowerConfiguration.PURIFIER_MAX_TUBES.get(); tubes++) {
            graphics.drawString(font, Component.translatable("jei.ariente.purifying_row", tubes,
                    tubes * PowerConfiguration.PURIFIER_TICKS_PER_TUBE.get() / 20.0), 30, 22 + (tubes - 1) * 22, 0x404040, false);
        }
    }
}
