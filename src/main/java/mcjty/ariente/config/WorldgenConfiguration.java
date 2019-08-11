package mcjty.ariente.config;

import mcjty.ariente.Ariente;
import mcjty.lib.thirteen.ConfigSpec;

public class WorldgenConfiguration {

    private static final String CATEGORY_WORLDGEN = "worldgen";

    public static ConfigSpec.ConfigValue<YesNoDefault> OVERWORLD_OREGEN;
    public static ConfigSpec.ConfigValue<YesNoDefault> OVERWORLD_DUNGEON_LOOT;
    public static ConfigSpec.IntValue OVERWORLD_LOOT_BLUEPRINTS;
    public static ConfigSpec.IntValue OVERWORLD_LOOT_ITEMS;
    public static ConfigSpec.IntValue OVERWORLD_LOOT_SUIT;

    public static void init(ConfigSpec.Builder SERVER_BUILDER, ConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("World generation settings").push(CATEGORY_WORLDGEN);

        OVERWORLD_OREGEN = SERVER_BUILDER
                .comment("Enable overworld oregen: 'yes', 'no', or 'default' (only when Ariente World is not installed)")
                .defineEnum("overworldOregen", YesNoDefault.DEFAULT, YesNoDefault.DEFAULT, YesNoDefault.YES, YesNoDefault.NO);
        OVERWORLD_DUNGEON_LOOT = SERVER_BUILDER
                .comment("Enable overworld dungeon loot: 'yes', 'no', or 'default' (only when Ariente World is not installed)")
                .defineEnum("overworldDungeonLoot", YesNoDefault.DEFAULT, YesNoDefault.DEFAULT, YesNoDefault.YES, YesNoDefault.NO);
        OVERWORLD_LOOT_BLUEPRINTS = SERVER_BUILDER
                .comment("For overworld loot: weight for blueprints (use 0 to disable)").defineInRange("overworldLootBlueprints", 20, 0, Integer.MAX_VALUE);
        OVERWORLD_LOOT_ITEMS = SERVER_BUILDER
                .comment("For overworld loot: weight for blueprint results (use 0 to disable)").defineInRange("overworldLootBlueprintResults", 0, 0, Integer.MAX_VALUE);
        OVERWORLD_LOOT_SUIT = SERVER_BUILDER
                .comment("For overworld loot: weight for powersuit pieces (use 0 to disable)").defineInRange("overworldLootSuit", 0, 0, Integer.MAX_VALUE);

        SERVER_BUILDER.pop();
    }

    public static boolean doWorldGen() {
        YesNoDefault oregen = OVERWORLD_OREGEN.get();
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

    public static boolean doDungeonLoot() {
        YesNoDefault oregen = OVERWORLD_DUNGEON_LOOT.get();
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
