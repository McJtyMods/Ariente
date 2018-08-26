package mcjty.ariente.config;

import net.minecraftforge.common.config.Configuration;

public class UtilityConfiguration {

    private static final String CATEGORY_UTILITY = "utility";

    public static int MAX_DOOR_HEIGHT = 6;
    public static int POWERSUIT_MAXPOWER = 100;
    public static int POWERSUIT_MAXPOWER_OPTIMIZED = 130;
    public static int POWERSUIT_TICKS = 30000;
    public static int POWERSUIT_TICKS_OPTIMIZED = 40000;

    public static void init(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_UTILITY, "Utility settings");

        MAX_DOOR_HEIGHT = cfg.getInt("maxDoorHeight", CATEGORY_UTILITY, MAX_DOOR_HEIGHT, 1, 50, "The maximum height of a door");
        POWERSUIT_MAXPOWER = cfg.getInt("powersuitMaxPower", CATEGORY_UTILITY, POWERSUIT_MAXPOWER, 1, 100000, "The maximum supported power of a power armor suit piece");
        POWERSUIT_MAXPOWER_OPTIMIZED = cfg.getInt("powersuitMaxPowerOptimized", CATEGORY_UTILITY, POWERSUIT_MAXPOWER_OPTIMIZED, 1, 100000, "The maximum supported power of a power armor suit piece with the energy module installed");
        POWERSUIT_TICKS = cfg.getInt("powersuitTicks", CATEGORY_UTILITY, POWERSUIT_TICKS, 1, 10000000, "Given a power consumption of 1, this represents the amount of ticks a single negarite/posirite item will last for a powersuit armor piece");
        POWERSUIT_TICKS_OPTIMIZED = cfg.getInt("powersuitTicksOptimized", CATEGORY_UTILITY, POWERSUIT_TICKS_OPTIMIZED, 1, 10000000, "Given a power consumption of 1, this represents the amount of ticks a single negarite/posirite item will last for a powersuit armor piece (with energy module installed)");
    }
}
