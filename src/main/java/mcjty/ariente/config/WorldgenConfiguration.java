package mcjty.ariente.config;

import mcjty.ariente.Ariente;
import mcjty.lib.thirteen.ConfigSpec;

public class WorldgenConfiguration {

    private static final String CATEGORY_WORLDGEN = "worldgen";

    public static ConfigSpec.ConfigValue<OverworldOregen> OVERWORLD_OREGEN;

    public static void init(ConfigSpec.Builder SERVER_BUILDER, ConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("World generation settings").push(CATEGORY_WORLDGEN);

        OVERWORLD_OREGEN = SERVER_BUILDER
                .comment("Enable overworld oregen: 'yes', 'no', or 'default' (only when Ariente World is not installed)")
                .defineEnum("overworldOregen", OverworldOregen.DEFAULT, OverworldOregen.DEFAULT, OverworldOregen.YES, OverworldOregen.NO);

        SERVER_BUILDER.pop();
    }

    public static boolean doWorldGen() {
        OverworldOregen oregen = OVERWORLD_OREGEN.get();
        switch (oregen) {
            case DEFAULT:
                return !Ariente.setup.arienteWorld;
            case YES:
                return true;
            case NO:
            default:
                return false;
        }
    }
}
