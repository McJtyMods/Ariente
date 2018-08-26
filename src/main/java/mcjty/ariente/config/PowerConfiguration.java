package mcjty.ariente.config;

import net.minecraftforge.common.config.Configuration;

public class PowerConfiguration {

    private static final String CATEGORY_POWER = "power";

    public static int FORCEFIELD_BUILDUP_POWER = 500;

    public static void init(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_POWER, "Power settings");

        FORCEFIELD_BUILDUP_POWER = cfg.getInt("forcefieldBuildupPower", CATEGORY_POWER, FORCEFIELD_BUILDUP_POWER, 0, 1000000000, "The amount of power to consume per forcefield panel while it is building up");
    }
}
