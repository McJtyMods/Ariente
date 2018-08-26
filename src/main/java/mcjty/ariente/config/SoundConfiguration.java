package mcjty.ariente.config;

import net.minecraftforge.common.config.Configuration;

public class SoundConfiguration {

    private static final String CATEGORY_SOUND = "sound";

    public static void init(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_SOUND, "Sound settings");
    }
}
