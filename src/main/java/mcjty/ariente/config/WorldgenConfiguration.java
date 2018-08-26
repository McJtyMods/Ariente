package mcjty.ariente.config;

import net.minecraftforge.common.config.Configuration;

public class WorldgenConfiguration {

    private static final String CATEGORY_WORLDGEN = "worldgen";

    public static int DIMENSION_ID = 222;
    public static float CITY_DUNGEON_CHANCE = .7f;

    public static void init(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_WORLDGEN, "World generation settings");

        DIMENSION_ID = cfg.getInt("dimensionId", CATEGORY_WORLDGEN, DIMENSION_ID, -100000, 100000, "The id of the Ariente dimension");
        CITY_DUNGEON_CHANCE = cfg.getFloat("cityDungeonChance", CATEGORY_WORLDGEN, CITY_DUNGEON_CHANCE, 0, 1, "The chance that a city dungeon spot will actually have a city dungeon");
    }
}
