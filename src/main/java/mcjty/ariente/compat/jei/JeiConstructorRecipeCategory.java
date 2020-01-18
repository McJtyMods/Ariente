package mcjty.ariente.compat.jei;

// @todo 1.14
public class JeiConstructorRecipeCategory {} /*implements IRecipeCategory<JeiConstructorRecipeWrapper> {

    private final IDrawable background;

    public JeiConstructorRecipeCategory(IGuiHelper guiHelper) {
        ResourceLocation location = new ResourceLocation(Ariente.MODID, "textures/gui/recipe_background.png");
        background = guiHelper.createDrawable(location, 0, 0, 160, 80);
    }

    @Nonnull
    @Override
    public String getUid() {
        return JeiPlugin.ARIENTE_CRAFTING_ID;
    }

    @Nonnull
    @Override
    public String getTitle() {
        return "Constructor";
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void drawExtras(@Nonnull Minecraft minecraft) {
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, JeiConstructorRecipeWrapper recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

        List<List<ItemStack>> ingredientsInputs = ingredients.getInputs(VanillaTypes.ITEM);
        int idx = 0;
        for (int x = 0 ; x < 4 ; x++) {
            for (int y = 0 ; y < 3 ; y++) {
                if (idx < ingredientsInputs.size()) {
                    guiItemStacks.init(idx, true, 23 + x * 18, 14 + y * 18);
                    List<ItemStack> inputs = ingredientsInputs.get(idx);
                    guiItemStacks.set(idx, inputs);
                }
                idx++;
            }
        }

        guiItemStacks.init(idx, false, 126, 50);
        List<ItemStack> outputs = ingredients.getOutputs(VanillaTypes.ITEM).get(0);
        guiItemStacks.set(idx, outputs);
    }

    @Override
    public String getModName() {
        return Ariente.MODNAME;
    }
}
*/