package mcjty.ariente.config;

import mcjty.ariente.Ariente;
import net.minecraftforge.common.ForgeConfigSpec;

public class WorldgenConfiguration {

    private static final String CATEGORY_WORLDGEN = "worldgen";

    public static ForgeConfigSpec.ConfigValue<YesNoDefault> OVERWORLD_OREGEN;
    public static ForgeConfigSpec.ConfigValue<YesNoDefault> OVERWORLD_DUNGEON_LOOT;
    public static ForgeConfigSpec.IntValue OVERWORLD_LOOT_BLUEPRINTS;
    public static ForgeConfigSpec.IntValue OVERWORLD_LOOT_ITEMS;
    public static ForgeConfigSpec.IntValue OVERWORLD_LOOT_SUIT;
    public static ForgeConfigSpec.IntValue STANDALONE_BLUEPRINT_MULTIPLIER;
    public static ForgeConfigSpec.IntValue STANDALONE_BLUEPRINT_COUNT;

    public static void init(ForgeConfigSpec.Builder SERVER_BUILDER, ForgeConfigSpec.Builder CLIENT_BUILDER) {
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
        STANDALONE_BLUEPRINT_MULTIPLIER = SERVER_BUILDER
                .comment("Without Ariente World: multiply the blueprint weight by this amount (default 20 gives an 80% chance with the default other weights)")
                .defineInRange("standaloneBlueprintMultiplier", 20, 1, 1000);
        STANDALONE_BLUEPRINT_COUNT = SERVER_BUILDER
                .comment("Without Ariente World: blueprints per chest reward, including one core-machine pattern")
                .defineInRange("standaloneBlueprintCount", 5, 1, 16);

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
