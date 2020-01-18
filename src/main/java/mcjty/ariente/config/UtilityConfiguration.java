package mcjty.ariente.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class UtilityConfiguration {

    private static final String CATEGORY_UTILITY = "utility";

    public static ForgeConfigSpec.IntValue MAX_DOOR_HEIGHT;
    public static ForgeConfigSpec.IntValue POWERSUIT_MAXPOWER;
    public static ForgeConfigSpec.IntValue POWERSUIT_MAXPOWER_OPTIMIZED;
    public static ForgeConfigSpec.IntValue POWERSUIT_TICKS;
    public static ForgeConfigSpec.IntValue POWERSUIT_TICKS_OPTIMIZED;
    public static ForgeConfigSpec.DoubleValue POWERSUIT_FLYVERTICAL_FACTOR;
    public static ForgeConfigSpec.DoubleValue POWERSUIT_MAX_FORWARD_GROUND_SPEED;
    public static ForgeConfigSpec.DoubleValue POWERSUIT_MAX_FORWARD_FLY_SPEED;
    public static ForgeConfigSpec.DoubleValue POWERSUIT_MAX_BACK_GROUND_SPEED;
    public static ForgeConfigSpec.DoubleValue POWERSUIT_MAX_BACK_FLY_SPEED;

    public static ForgeConfigSpec.IntValue WARPER_MAX_CHARGES;

    public static ForgeConfigSpec.IntValue AUTOFIELD_POWER_PER_OPERATION;
    public static ForgeConfigSpec.DoubleValue AUTOFIELD_FACTOR_PER_COMBINED_STACK;
    public static ForgeConfigSpec.IntValue AUTOFIELD_ACCUMULATE_PER_TICK;
    public static ForgeConfigSpec.IntValue AUTOFIELD_MAX_ACCUMULATED_POWER;

    public static void init(ForgeConfigSpec.Builder SERVER_BUILDER, ForgeConfigSpec.Builder CLIENT_BUILDER) {
        CLIENT_BUILDER.comment("Utility settings").push(CATEGORY_UTILITY);
        SERVER_BUILDER.comment("Utility settings").push(CATEGORY_UTILITY);

        POWERSUIT_FLYVERTICAL_FACTOR = CLIENT_BUILDER
                .comment("Amount to go up/down with space/shift while flying")
                .defineInRange("flyVerticalFactor", 0.4, 0, 100);
        POWERSUIT_MAX_FORWARD_GROUND_SPEED = CLIENT_BUILDER
                .comment("Maximum acceleration for movement forward on ground")
                .defineInRange("maxForwardGroundSpeed", 0.5, 0, 5);
        POWERSUIT_MAX_FORWARD_FLY_SPEED = CLIENT_BUILDER
                .comment("Maximum acceleration for movement forward while flying")
                .defineInRange("maxForwardFlySpeed", 1.6, 0, 5);
        POWERSUIT_MAX_BACK_GROUND_SPEED = CLIENT_BUILDER
                .comment("Maximum acceleration for movement back on ground")
                .defineInRange("maxBackGroundSpeed", 0.3, 0, 5);
        POWERSUIT_MAX_BACK_FLY_SPEED = CLIENT_BUILDER
                .comment("Maximum acceleration for movement back while flying")
                .defineInRange("maxBackFlySpeed", 1.4, 0, 5);

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

        WARPER_MAX_CHARGES = SERVER_BUILDER
                .comment("Amount of charges needed before the warper can work (from overworld to Ariente)")
                .defineInRange("maxWarperCharges", 10, 0, Integer.MAX_VALUE);

        SERVER_BUILDER.pop();
        CLIENT_BUILDER.pop();
    }
}
