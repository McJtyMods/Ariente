package mcjty.ariente.compat.jei;


import mcjty.ariente.recipes.ConstructorRecipe;

public class JeiConstructorRecipe {

    private final ConstructorRecipe recipe;

    public JeiConstructorRecipe(ConstructorRecipe recipe) {
        this.recipe = recipe;
    }

    public ConstructorRecipe getRecipe() {
        return recipe;
    }
}
