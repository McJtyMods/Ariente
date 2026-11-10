package mcjty.ariente.loot;

import java.util.List;
import java.util.Set;
import mcjty.ariente.Ariente;
import mcjty.ariente.config.WorldgenConfiguration;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.recipes.ConstructorRecipe;
import mcjty.ariente.setup.Registration;
import mcjty.lib.varia.LootGeneratedEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public final class DungeonLootModifier {

    private DungeonLootModifier() {
    }

    public static void register() {
        LootGeneratedEvent.EVENT.register((id, context, drops) -> {
            String table = id.toString();
            if (Set.of("minecraft:chests/abandoned_mineshaft", "minecraft:chests/igloo_chest", "minecraft:chests/desert_pyramid", "minecraft:chests/jungle_temple", "minecraft:chests/nether_bridge", "minecraft:chests/simple_dungeon", "minecraft:chests/village/village_toolsmith").contains(table)) apply(drops, context, false);
            if (Set.of("minecraft:chests/ancient_city", "minecraft:chests/buried_treasure", "minecraft:chests/pillager_outpost", "minecraft:chests/ruined_portal", "minecraft:chests/shipwreck_map", "minecraft:chests/shipwreck_supply", "minecraft:chests/shipwreck_treasure", "minecraft:chests/stronghold_corridor", "minecraft:chests/stronghold_crossing", "minecraft:chests/stronghold_library", "minecraft:chests/underwater_ruin_big", "minecraft:chests/underwater_ruin_small", "minecraft:chests/village/village_armorer", "minecraft:chests/village/village_butcher", "minecraft:chests/village/village_cartographer", "minecraft:chests/village/village_desert_house", "minecraft:chests/village/village_fisher", "minecraft:chests/village/village_fletcher", "minecraft:chests/village/village_mason", "minecraft:chests/village/village_plains_house", "minecraft:chests/village/village_savanna_house", "minecraft:chests/village/village_shepherd", "minecraft:chests/village/village_snowy_house", "minecraft:chests/village/village_taiga_house", "minecraft:chests/village/village_tannery", "minecraft:chests/village/village_temple", "minecraft:chests/village/village_weaponsmith", "minecraft:chests/woodland_mansion").contains(table)) apply(drops, context, true);
        });
    }

    public static List<ItemStack> apply(List<ItemStack> loot, LootContext context, boolean standaloneOnly) {
        // Server config is read when loot is rolled, never while tables are parsed.
        if (!WorldgenConfiguration.doDungeonLoot() || standaloneOnly && Ariente.setup.arienteWorld) {
            return loot;
        }
        boolean standalone = !Ariente.setup.arienteWorld;
        double blueprints = WorldgenConfiguration.OVERWORLD_LOOT_BLUEPRINTS.get()
                * (standalone ? (double) WorldgenConfiguration.STANDALONE_BLUEPRINT_MULTIPLIER.get() : 1.0);
        int results = WorldgenConfiguration.OVERWORLD_LOOT_ITEMS.get();
        int suit = WorldgenConfiguration.OVERWORLD_LOOT_SUIT.get();
        RandomSource random = context.getRandom();
        double roll = random.nextDouble() * (100.0 + blueprints + results + 6.0 * suit);
        if (roll < blueprints + (double) results) {
            if (standalone && roll < blueprints) {
                StandaloneBlueprintLoot.addBlueprints(loot, random, WorldgenConfiguration.STANDALONE_BLUEPRINT_COUNT.get());
            } else {
                ConstructorRecipe recipe = BlueprintRecipeRegistry.getRandomRecipe(random, roll < blueprints);
                loot.add(roll < blueprints ? BlueprintItem.makeBluePrint(recipe.getDestination()) : recipe.getDestination().copy());
            }
        } else if (roll < blueprints + (double) results + 6.0 * suit) {
            Item[] items = {Registration.POWERSUIT_CHEST.get(), Registration.POWERSUIT_FEET.get(), Registration.POWERSUIT_LEGS.get(),
                    Registration.POWERSUIT_HEAD.get(), Registration.ENERGY_SABRE.get(), Registration.ENHANCED_ENERGY_SABRE.get()};
            loot.add(new ItemStack(items[random.nextInt(items.length)]));
        }
        return loot;
    }

}
