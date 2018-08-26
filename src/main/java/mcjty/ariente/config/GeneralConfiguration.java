package mcjty.ariente.config;

import net.minecraftforge.common.config.Configuration;

public class GeneralConfiguration {

    private static final String CATEGORY_GENERAL = "general";

    public static void init(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_GENERAL, "General settings");

    }
}
