package mcjty.ariente.config;

import mcjty.ariente.Ariente;
import net.neoforged.neoforge.common.ModConfigSpec;

public class RecipeConfiguration {

    public static ModConfigSpec.EnumValue<YesNoDefault> EXTREME_CIRCUIT_RECIPE;

    public static void init(ModConfigSpec.Builder server) {
        server.comment("Constructor recipe settings").push("recipes");
        EXTREME_CIRCUIT_RECIPE = server
                .comment("Extreme Circuit recipe and blueprint: 'yes', 'no', or 'default' (only without Ariente World)")
                .defineEnum("extremeCircuitRecipe", YesNoDefault.DEFAULT);
        server.pop();
    }

    public static boolean extremeCircuitRecipeEnabled() {
        YesNoDefault setting = Config.SERVER_CONFIG.isLoaded()
                ? EXTREME_CIRCUIT_RECIPE.get() : EXTREME_CIRCUIT_RECIPE.getDefault();
        return setting == YesNoDefault.YES || setting == YesNoDefault.DEFAULT && !Ariente.setup.arienteWorld;
    }
}
