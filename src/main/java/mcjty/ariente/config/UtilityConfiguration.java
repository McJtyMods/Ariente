package mcjty.ariente.config;

import mcjty.lib.thirteen.ConfigSpec;

public class UtilityConfiguration {

    private static final String CATEGORY_UTILITY = "utility";

    public static ConfigSpec.IntValue MAX_DOOR_HEIGHT;
    public static ConfigSpec.IntValue POWERSUIT_MAXPOWER;
    public static ConfigSpec.IntValue POWERSUIT_MAXPOWER_OPTIMIZED;
    public static ConfigSpec.IntValue POWERSUIT_TICKS;
    public static ConfigSpec.IntValue POWERSUIT_TICKS_OPTIMIZED;

    public static void init(ConfigSpec.Builder SERVER_BUILDER, ConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Utility settings").push(CATEGORY_UTILITY);

        MAX_DOOR_HEIGHT = SERVER_BUILDER
                .comment("The maximum height of a door")
                .defineInRange("maxDoorHeight", 6, 1, 50);
        POWERSUIT_MAXPOWER = SERVER_BUILDER
                .comment("The maximum supported power of a power armor suit piece")
                .defineInRange("powersuitMaxPower", 100, 1, 100000);
        POWERSUIT_MAXPOWER_OPTIMIZED = SERVER_BUILDER
                .comment("The maximum supported power of a power armor suit piece with the energy module installed")
                .defineInRange("powersuitMaxPowerOptimized", 130, 1, 100000);
        POWERSUIT_TICKS = SERVER_BUILDER
                .comment("Given a power consumption of 1, this represents the amount of ticks a single negarite/posirite item will last for a powersuit armor piece")
                .defineInRange("powersuitTicks", 30000, 1, 10000000);
        POWERSUIT_TICKS_OPTIMIZED = SERVER_BUILDER
                .comment("Given a power consumption of 1, this represents the amount of ticks a single negarite/posirite item will last for a powersuit armor piece (with energy module installed)")
                .defineInRange("powersuitTicksOptimized", 40000, 1, 10000000);

        SERVER_BUILDER.pop();
    }
}
