package mcjty.ariente.config;

import mcjty.lib.thirteen.ConfigSpec;

public class PowerConfiguration {

    private static final String CATEGORY_POWER = "power";

    public static ConfigSpec.IntValue FORCEFIELD_BUILDUP_POWER;

    public static void init(ConfigSpec.Builder SERVER_BUILDER, ConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Power settings").push(CATEGORY_POWER);

        FORCEFIELD_BUILDUP_POWER = SERVER_BUILDER
                .comment("The amount of power to consume per forcefield panel while it is building up")
                .defineInRange("forcefieldBuildupPower", 500, 0, 1000000000);

        SERVER_BUILDER.pop();
    }
}
