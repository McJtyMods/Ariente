package mcjty.ariente.datagen;

import mcjty.lib.datagen.BaseRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;

import java.util.function.Consumer;

public class Recipes extends BaseRecipeProvider {

    public Recipes(DataGenerator generatorIn) {
        super(generatorIn);
        group("ariente");
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
    }
}
