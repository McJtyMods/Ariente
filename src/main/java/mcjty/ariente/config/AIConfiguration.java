package mcjty.ariente.config;

import net.minecraftforge.common.config.Configuration;

public class AIConfiguration {

    private static final String CATEGORY_AI = "ai";

    public static int ALERT_TIME = 400;

    public static void init(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_AI, "AI settings");

        ALERT_TIME = cfg.getInt("alertTime", CATEGORY_AI, ALERT_TIME, 1, 100000000, "The amount of ticks (times 10) that the city will stay on alert after spotting a player. So 120 would be one minute");
    }
}
