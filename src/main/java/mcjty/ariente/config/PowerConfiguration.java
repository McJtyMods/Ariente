package mcjty.ariente.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class PowerConfiguration {

    private static final String CATEGORY_POWER = "power";

    public static ForgeConfigSpec.IntValue FORCEFIELD_BUILDUP_POWER;
    public static ForgeConfigSpec.IntValue SMELTER_POWER_PER_TICK;
    public static ForgeConfigSpec.IntValue SMELTER_SPEED;

    public static void init(ForgeConfigSpec.Builder SERVER_BUILDER, ForgeConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Power settings").push(CATEGORY_POWER);

        FORCEFIELD_BUILDUP_POWER = SERVER_BUILDER
                .comment("The amount of power to consume per forcefield panel while it is building up")
                .defineInRange("forcefieldBuildupPower", 500, 0, 1000000000);

        SMELTER_POWER_PER_TICK = SERVER_BUILDER
                .comment("The amount of each Ariente flux consumed per powered smelting tick")
                .defineInRange("smelterPowerPerTick", 20, 1, 1000000000);

        SMELTER_SPEED = SERVER_BUILDER
                .comment("Furnace recipe ticks processed per powered smelting tick (1 = vanilla speed)")
                .defineInRange("smelterSpeed", 10, 1, 200);

        SERVER_BUILDER.pop();
    }
}
