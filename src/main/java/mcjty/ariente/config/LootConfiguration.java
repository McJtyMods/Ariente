package mcjty.ariente.config;

import mcjty.lib.thirteen.ConfigSpec;

public class LootConfiguration {

    private static final String CATEGORY_LOOT = "loot";

    public static ConfigSpec.DoubleValue SOLDIER_CITYKEY_CHANCE;

    public static void init(ConfigSpec.Builder SERVER_BUILDER, ConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Loot settings").push(CATEGORY_LOOT);

        SOLDIER_CITYKEY_CHANCE = SERVER_BUILDER
                .comment("The chance that a killed soldier will drop a keycard")
                .defineInRange("soldierCitykeyChance", .2, 0, 1);
        SERVER_BUILDER.pop();
    }
}
