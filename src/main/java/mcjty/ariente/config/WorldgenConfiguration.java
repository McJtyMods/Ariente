package mcjty.ariente.config;

import mcjty.lib.thirteen.ConfigSpec;

public class WorldgenConfiguration {

    private static final String CATEGORY_WORLDGEN = "worldgen";

    public static ConfigSpec.IntValue DIMENSION_ID;
    public static ConfigSpec.DoubleValue CITY_DUNGEON_CHANCE;
    public static ConfigSpec.DoubleValue OVERWORLD_DUNGEON_CHANCE;

    public static void init(ConfigSpec.Builder SERVER_BUILDER, ConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("World generation settings").push(CATEGORY_WORLDGEN);

        DIMENSION_ID = SERVER_BUILDER
                .comment("The id of the Ariente dimension")
                .defineInRange("dimensionId", 222, -100000, 100000);
        CITY_DUNGEON_CHANCE = SERVER_BUILDER
                .comment("The chance that a city dungeon spot will actually have a city dungeon")
                .defineInRange("cityDungeonChance", .7, 0, 1);
        OVERWORLD_DUNGEON_CHANCE = SERVER_BUILDER
                .comment("The chance that a chunk in the overworld will have an Ariente dungeon")
                .defineInRange("overworldDungeonChance", .002, 0, 1);

        SERVER_BUILDER.pop();
    }
}
