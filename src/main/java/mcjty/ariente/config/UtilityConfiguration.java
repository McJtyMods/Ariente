package mcjty.ariente.config;

import mcjty.lib.thirteen.ConfigSpec;

public class UtilityConfiguration {

    private static final String CATEGORY_UTILITY = "utility";

    public static ConfigSpec.IntValue MAX_DOOR_HEIGHT;
    public static ConfigSpec.IntValue POWERSUIT_MAXPOWER;
    public static ConfigSpec.IntValue POWERSUIT_MAXPOWER_OPTIMIZED;
    public static ConfigSpec.IntValue POWERSUIT_TICKS;
    public static ConfigSpec.IntValue POWERSUIT_TICKS_OPTIMIZED;

    public static ConfigSpec.IntValue AUTOFIELD_POWER_PER_OPERATION;
    public static ConfigSpec.DoubleValue AUTOFIELD_FACTOR_PER_COMBINED_STACK;
    public static ConfigSpec.IntValue AUTOFIELD_ACCUMULATE_PER_TICK;
    public static ConfigSpec.IntValue AUTOFIELD_MAX_ACCUMULATED_POWER;

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
        AUTOFIELD_POWER_PER_OPERATION = SERVER_BUILDER
                .comment("The automation field will gather power in an internal capacitor to be able to do a single operation. This represents the amount of accumulated power needed for this operation")
                .defineInRange("autoFieldPowerPerOperation", 100, 1, Integer.MAX_VALUE);
        AUTOFIELD_FACTOR_PER_COMBINED_STACK = SERVER_BUILDER
                .comment("When transfering stacks with more than 1 item, the size of the stack will be multiplied with this factor and that cost will be added to the 'per operation' cost for transfering the stack")
                .defineInRange("autoFieldFactorPerCombinedStack", 2.0, 0.0, 1000000000.0);
        AUTOFIELD_ACCUMULATE_PER_TICK = SERVER_BUILDER
                .comment("This is the maximum amount of power the automation field will accumulate in a tick. Limiting this will basically set a limit to the amount of operations per tick that can be handled")
                .defineInRange("autoFieldAccumulatePerTick", 400, 1, Integer.MAX_VALUE);
        AUTOFIELD_MAX_ACCUMULATED_POWER = SERVER_BUILDER
                .comment("This is the maximum amount of power the automation field can accumulate")
                .defineInRange("autoFieldMaxAccumulatedPower", 3200, 1, Integer.MAX_VALUE);

        SERVER_BUILDER.pop();
    }
}
