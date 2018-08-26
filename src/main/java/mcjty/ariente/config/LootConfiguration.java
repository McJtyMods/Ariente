package mcjty.ariente.config;

import net.minecraftforge.common.config.Configuration;

public class LootConfiguration {

    private static final String CATEGORY_LOOT = "loot";

    public static float SOLDIER_CITYKEY_CHANCE = .2f;

    public static void init(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_LOOT, "Loot settings");

        SOLDIER_CITYKEY_CHANCE = cfg.getFloat("soldierCitykeyChance", CATEGORY_LOOT, SOLDIER_CITYKEY_CHANCE, 0, 1, "The chance that a killed soldier will drop a keycard");
    }
}
