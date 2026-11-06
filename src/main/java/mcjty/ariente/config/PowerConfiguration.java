package mcjty.ariente.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class PowerConfiguration {

    private static final String CATEGORY_POWER = "power";

    public static ForgeConfigSpec.IntValue FORCEFIELD_BUILDUP_POWER;
    public static ForgeConfigSpec.IntValue SMELTER_POWER_PER_TICK;
    public static ForgeConfigSpec.IntValue SMELTER_SPEED;
    public static ForgeConfigSpec.IntValue PURIFIER_TICKS_PER_TUBE;
    public static ForgeConfigSpec.IntValue PURIFIER_POWER_PER_TUBE;
    public static ForgeConfigSpec.IntValue PURIFIER_MAX_TUBES;
    public static ForgeConfigSpec.IntValue PURIFIER_BASE_YIELD;
    public static ForgeConfigSpec.IntValue PURIFIER_EXTRA_YIELD;
    public static ForgeConfigSpec.IntValue PURIFIER_RING_PERIOD;
    public static ForgeConfigSpec.IntValue PURIFIER_RING_PARTICLES;
    public static ForgeConfigSpec.IntValue PURIFIER_RING_STREAMS;
    public static ForgeConfigSpec.IntValue PURIFIER_RING_TRAIL_TICKS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> PURIFIER_PREFERRED_OUTPUT_MODS;

    public static void init(ForgeConfigSpec.Builder SERVER_BUILDER, ForgeConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Power settings").push(CATEGORY_POWER);

        FORCEFIELD_BUILDUP_POWER = SERVER_BUILDER
                .comment("The amount of power to consume per forcefield panel while it is building up")
                .defineInRange("forcefieldBuildupPower", 500, 0, 1000000000);

        SMELTER_POWER_PER_TICK = SERVER_BUILDER
                .comment("The amount of each Ariente flux consumed per powered smelting tick")
                .defineInRange("smelterPowerPerTick", 200, 1, 1000000000);

        SMELTER_SPEED = SERVER_BUILDER
                .comment("Furnace recipe ticks processed per powered smelting tick (1 = vanilla speed)")
                .defineInRange("smelterSpeed", 10, 1, 200);

        PURIFIER_TICKS_PER_TUBE = SERVER_BUILDER.comment("Powered ticks to traverse each purifier tube (160 ticks = 8 seconds)")
                .defineInRange("purifierTicksPerTube", 160, 1, 1000000);
        PURIFIER_POWER_PER_TUBE = SERVER_BUILDER.comment("Amount of EACH flux consumed per active tick for EACH tube in the purifier line")
                .defineInRange("purifierPowerPerTube", 400, 1, 1000000000);
        PURIFIER_MAX_TUBES = SERVER_BUILDER.comment("Maximum number of straight horizontal tubes between purifier endpoints")
                .defineInRange("purifierMaxTubes", 4, 1, 16);
        PURIFIER_BASE_YIELD = SERVER_BUILDER.comment("Raw metals recovered per ore with one purifier tube")
                .defineInRange("purifierBaseYield", 2, 1, 64);
        PURIFIER_EXTRA_YIELD = SERVER_BUILDER.comment("Additional raw metals recovered for each extra purifier tube; final yield is capped at 64")
                .defineInRange("purifierExtraYieldPerTube", 1, 0, 63);
        PURIFIER_PREFERRED_OUTPUT_MODS = SERVER_BUILDER
                .comment("Preferred mod namespaces for automatic raw-metal outputs, in priority order.",
                        "Other providers are ordered by registry ID. Explicit purifying recipes override automatic selection.")
                .defineList("purifierPreferredOutputMods", List.of("minecraft", "ariente"),
                        value -> value instanceof String name && name.matches("[a-z0-9_.-]+"));
        SERVER_BUILDER.pop();

        CLIENT_BUILDER.push("purifier");
        PURIFIER_RING_PERIOD = CLIENT_BUILDER.comment("Ticks for each bright particle-stream head to travel once around a tube")
                .defineInRange("ringPeriod", 20, 1, 1200);
        PURIFIER_RING_PARTICLES = CLIENT_BUILDER.comment("Red particles emitted per tube every two client ticks")
                .defineInRange("ringParticles", 6, 0, 32);
        PURIFIER_RING_STREAMS = CLIENT_BUILDER.comment("Moving particle streams around each tube, each with a fading trail")
                .defineInRange("ringStreams", 2, 1, 8);
        PURIFIER_RING_TRAIL_TICKS = CLIENT_BUILDER.comment("Lifetime of orbiting purifier particles in client ticks")
                .defineInRange("ringTrailTicks", 17, 1, 100);
        CLIENT_BUILDER.pop();
    }
}
