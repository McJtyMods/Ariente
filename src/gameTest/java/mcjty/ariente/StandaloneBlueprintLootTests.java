package mcjty.ariente;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mcjty.ariente.config.RecipeConfiguration;
import mcjty.ariente.config.WorldgenConfiguration;
import mcjty.ariente.config.YesNoDefault;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.loot.DungeonLootModifier;
import mcjty.ariente.loot.StandaloneBlueprintLoot;
import mcjty.ariente.recipes.BlueprintRarity;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.recipes.ConstructorRecipe;
import mcjty.ariente.setup.Registration;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class StandaloneBlueprintLootTests {

    @GameTest(template = "empty")
    public static void freshDefaultsGiveUsefulBundlesAtTheIntendedRate(GameTestHelper helper) {
        boolean original = Ariente.setup.arienteWorld;
        try (ServerConfigScope ignored = new ServerConfigScope()) {
            Ariente.setup.arienteWorld = false;
            defaults();
            DungeonLootModifier modifier = new DungeonLootModifier(new LootItemCondition[0]);
            LootContext context = context(helper);
            int rewarded = 0;
            for (int trial = 0; trial < 5000; trial++) {
                ObjectArrayList<ItemStack> loot = new ObjectArrayList<>();
                loot.add(new ItemStack(Items.DIAMOND));
                modifier.apply(loot, context);
                helper.assertTrue(loot.get(0).is(Items.DIAMOND), "Vanilla loot must be preserved");
                if (loot.size() == 1) {
                    continue;
                }
                rewarded++;
                helper.assertTrue(loot.size() == 6 && starters().contains(BlueprintItem.getDestination(loot.get(1)).getItem()),
                        "A successful default roll gives five blueprints, including a core pattern");
                for (int slot = 1; slot < loot.size(); slot++) {
                    ItemStack stack = loot.get(slot);
                    helper.assertTrue(stack.is(Registration.BLUEPRINT.get()) && stack.getCount() == 1
                            && BlueprintRecipeRegistry.findRecipe(BlueprintItem.getDestination(stack)) != null,
                            "Each reward is a valid, individually encoded blueprint");
                }
            }
            helper.assertTrue(rewarded > 3875 && rewarded < 4125,
                    "Default chest reward frequency must be close to 80%: " + rewarded + " / 5000");
        } finally {
            Ariente.setup.arienteWorld = original;
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void tierSharesAndDisabledRecipesMatchStandaloneProgression(GameTestHelper helper) {
        boolean original = Ariente.setup.arienteWorld;
        try (ServerConfigScope ignored = new ServerConfigScope()) {
            Ariente.setup.arienteWorld = false;
            RecipeConfiguration.EXTREME_CIRCUIT_RECIPE.set(YesNoDefault.NO);
            int[] counts = new int[4];
            for (int roll = 0; roll < 100; roll++) {
                ObjectArrayList<ItemStack> loot = new ObjectArrayList<>();
                StandaloneBlueprintLoot.addBlueprints(loot, new TierRandom(roll), 2);
                helper.assertTrue(starters().contains(BlueprintItem.getDestination(loot.get(0)).getItem()),
                        "The first blueprint is selected from the core patterns");
                ConstructorRecipe recipe = BlueprintRecipeRegistry.findRecipe(BlueprintItem.getDestination(loot.get(1)));
                counts[BlueprintRarity.of(recipe).ordinal()]++;
                helper.assertTrue(!recipe.getDestination().is(Registration.EXTREME_CIRCUIT.get()),
                        "A disabled recipe must not be selected even on an Extreme roll");
            }
            helper.assertTrue(counts[0] == 65 && counts[1] == 20 && counts[2] == 13 && counts[3] == 2,
                    "Standalone selection must use 65/20/13/2 tier shares, independently of catalog sizes");
        } finally {
            Ariente.setup.arienteWorld = original;
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void disablingAndCompanionOptInsKeepTheirExistingMeaning(GameTestHelper helper) {
        boolean original = Ariente.setup.arienteWorld;
        try (ServerConfigScope ignored = new ServerConfigScope()) {
            defaults();
            WorldgenConfiguration.OVERWORLD_LOOT_BLUEPRINTS.set(1000000);
            DungeonLootModifier existing = new DungeonLootModifier(new LootItemCondition[0]);
            DungeonLootModifier extra = new DungeonLootModifier(new LootItemCondition[0], true);
            LootContext context = context(helper);
            Ariente.setup.arienteWorld = true;
            helper.assertTrue(existing.apply(new ObjectArrayList<>(), context).isEmpty(),
                    "With the companion installed, DEFAULT still disables vanilla blueprint additions");
            WorldgenConfiguration.OVERWORLD_DUNGEON_LOOT.set(YesNoDefault.YES);
            helper.assertTrue(existing.apply(new ObjectArrayList<>(), context).size() == 1,
                    "Explicit companion opt-in still adds one blueprint, without the standalone bundle");
            helper.assertTrue(extra.apply(new ObjectArrayList<>(), context).isEmpty(),
                    "The expanded standalone chest list must not activate with the companion");
            Ariente.setup.arienteWorld = false;
            WorldgenConfiguration.STANDALONE_BLUEPRINT_COUNT.set(3);
            helper.assertTrue(existing.apply(new ObjectArrayList<>(), context).size() == 3,
                    "The standalone bundle size is configurable");
            WorldgenConfiguration.OVERWORLD_LOOT_BLUEPRINTS.set(0);
            helper.assertTrue(existing.apply(new ObjectArrayList<>(), context).isEmpty(), "Zero blueprint weight still disables rewards");
            WorldgenConfiguration.OVERWORLD_DUNGEON_LOOT.set(YesNoDefault.NO);
            WorldgenConfiguration.OVERWORLD_LOOT_BLUEPRINTS.set(1000000);
            helper.assertTrue(existing.apply(new ObjectArrayList<>(), context).isEmpty(), "NO still disables rewards");
        } finally {
            Ariente.setup.arienteWorld = original;
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void newChestTablesAreValidDisjointAndActuallyInjectLoot(GameTestHelper helper) throws Exception {
        Set<ResourceLocation> tables = new HashSet<>();
        for (String name : List.of("dungeon_loot", "standalone_chest_loot")) {
            try (var stream = StandaloneBlueprintLootTests.class.getResourceAsStream("/data/ariente/loot_modifiers/" + name + ".json")) {
                JsonObject json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                if (name.equals("standalone_chest_loot")) {
                    helper.assertTrue(json.get("standalone_only").getAsBoolean(), "Expanded tables must be standalone only");
                }
                for (var term : json.getAsJsonArray("conditions").get(0).getAsJsonObject().getAsJsonArray("terms")) {
                    ResourceLocation id = new ResourceLocation(term.getAsJsonObject().get("loot_table_id").getAsString());
                    helper.assertTrue(BuiltInLootTables.all().contains(id) && tables.add(id),
                            "Chest tables must exist and never receive both modifiers: " + id);
                }
            }
        }
        helper.assertTrue(tables.size() == 35 && !tables.contains(BuiltInLootTables.SPAWN_BONUS_CHEST),
                "The supported exploration tables must not include the starter bonus chest");
        boolean original = Ariente.setup.arienteWorld;
        try (ServerConfigScope ignored = new ServerConfigScope()) {
            defaults();
            WorldgenConfiguration.OVERWORLD_LOOT_BLUEPRINTS.set(1000000);
            var level = helper.getLevel();
            var params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.ZERO).create(LootContextParamSets.CHEST);
            for (boolean companion : List.of(false, true)) {
                Ariente.setup.arienteWorld = companion;
                WorldgenConfiguration.OVERWORLD_DUNGEON_LOOT.set(YesNoDefault.YES);
                for (ResourceLocation table : List.of(BuiltInLootTables.SIMPLE_DUNGEON, BuiltInLootTables.VILLAGE_PLAINS_HOUSE,
                        BuiltInLootTables.SHIPWRECK_TREASURE, BuiltInLootTables.STRONGHOLD_LIBRARY)) {
                    var loot = level.getServer().getLootData().getLootTable(table).getRandomItems(params, 777);
                    long blueprints = loot.stream().filter(stack -> stack.is(Registration.BLUEPRINT.get())).count();
                    helper.assertTrue(blueprints == (companion ? table.equals(BuiltInLootTables.SIMPLE_DUNGEON) ? 1 : 0 : 5),
                            "Registered modifiers must give the intended standalone/companion rewards for " + table + ": " + blueprints);
                }
            }
        } finally {
            Ariente.setup.arienteWorld = original;
        }
        helper.succeed();
    }

    private static void defaults() {
        WorldgenConfiguration.OVERWORLD_DUNGEON_LOOT.set(YesNoDefault.DEFAULT);
        WorldgenConfiguration.OVERWORLD_LOOT_BLUEPRINTS.set(WorldgenConfiguration.OVERWORLD_LOOT_BLUEPRINTS.getDefault());
        WorldgenConfiguration.OVERWORLD_LOOT_ITEMS.set(0);
        WorldgenConfiguration.OVERWORLD_LOOT_SUIT.set(0);
        WorldgenConfiguration.STANDALONE_BLUEPRINT_COUNT.set(WorldgenConfiguration.STANDALONE_BLUEPRINT_COUNT.getDefault());
        WorldgenConfiguration.STANDALONE_BLUEPRINT_MULTIPLIER.set(WorldgenConfiguration.STANDALONE_BLUEPRINT_MULTIPLIER.getDefault());
    }

    private static LootContext context(GameTestHelper helper) {
        return new LootContext.Builder(new LootParams.Builder(helper.getLevel()).create(LootContextParamSets.EMPTY))
                .withOptionalRandomSeed(723723).create(null);
    }

    private static Set<Item> starters() {
        return Set.of(Registration.CIRCUIT.get(), Registration.ENERGY_HOLDER.get(), Registration.NEGARITE_GENERATOR.get().asItem(),
                Registration.POSIRITE_GENERATOR.get().asItem(), Registration.SMELTER.get().asItem(),
                Registration.AUTO_CONSTRUCTOR.get().asItem(), Registration.RECYCLER.get().asItem());
    }

    private static class TierRandom extends LegacyRandomSource {

        private final int roll;

        private TierRandom(int roll) {
            super(87343);
            this.roll = roll;
        }

        @Override
        public int nextInt(int bound) {
            return bound == 100 ? roll : super.nextInt(bound);
        }
    }
}
