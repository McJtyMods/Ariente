package mcjty.ariente;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import mcjty.ariente.config.RecipeConfiguration;
import mcjty.ariente.config.YesNoDefault;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.recipes.BlueprintExchangeRecipe;
import mcjty.ariente.recipes.BlueprintRarity;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.recipes.ConstructorRecipe;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class BlueprintExchangeTests {

    @GameTest(template = "empty")
    public static void everyTierRollUsesTheProposedProbabilities(GameTestHelper helper) {
        for (BlueprintRarity average : BlueprintRarity.values()) {
            List<ConstructorRecipe> inputs = BlueprintRecipeRegistry.getRecipes().stream()
                    .filter(recipe -> BlueprintRarity.of(recipe) == average).limit(3).toList();
            // Extreme only has two enabled recipes with the companion installed.
            ItemStack[] blueprints = new ItemStack[]{blueprint(inputs.get(0)), blueprint(inputs.get(1)),
                    blueprint(inputs.get(inputs.size() == 3 ? 2 : 0))};
            for (int roll = 0; roll < 10; roll++) {
                ItemStack output = preview(helper, blueprints);
                helper.assertTrue(BlueprintExchangeRecipe.averageRarity(output) == average, "Three matching tiers keep their average");
                boolean enabledAlternative = BlueprintRecipeRegistry.getRecipes().stream()
                        .anyMatch(recipe -> BlueprintRarity.of(recipe) == average && !inputs.contains(recipe));
                BlueprintExchangeRecipe.resolve(output, new ScriptedRandom(roll, 0));
                ConstructorRecipe result = BlueprintRecipeRegistry.findRecipe(BlueprintItem.getDestination(output));
                int expected = Math.max(0, Math.min(3, average.ordinal() + (roll == 0 ? -1 : roll >= 8 ? 1 : 0)));
                if (expected == BlueprintRarity.EXTREME.ordinal() && !enabledAlternative && average == BlueprintRarity.EXTREME) {
                    expected = BlueprintRarity.RARE.ordinal();
                }
                helper.assertTrue(result != null && BlueprintRarity.of(result).ordinal() == expected,
                        "Roll " + roll + " at " + average + " must use the correct tier or empty-tier fallback");
                helper.assertTrue(inputs.stream().noneMatch(input -> input.getId().equals(result.getId())),
                        "Every consumed pattern is excluded");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void mixedTiersRoundAndGridArrangementsDoNotRevealResults(GameTestHelper helper) {
        CraftingContainer grid = grid(helper, 3, 3);
        ItemStack common = blueprint(Registration.CIRCUIT.get().getDefaultInstance());
        ItemStack uncommon = blueprint(Registration.ALARM.get().asItem().getDefaultInstance());
        ItemStack rare = blueprint(Registration.ENERGY_LANCE.get().getDefaultInstance());
        ItemStack extreme = blueprint(Registration.MODULE_FLIGHT.get().getDefaultInstance());
        ItemStack result = preview(helper, common, uncommon, rare);
        helper.assertTrue(BlueprintExchangeRecipe.averageRarity(result) == BlueprintRarity.UNCOMMON,
                "Common + Uncommon + Rare averages to Uncommon");
        helper.assertTrue(BlueprintExchangeRecipe.averageRarity(preview(helper, common, rare, extreme)) == BlueprintRarity.RARE,
                "A fractional average rounds to the nearest tier");
        for (int index = 0; index < 9; index++) {
            grid.clearContent();
            grid.setItem(index, common.copy());
            grid.setItem((index + 2) % 9, uncommon.copy());
            grid.setItem((index + 5) % 9, rare.copy());
            ItemStack other = recipe(helper).assemble(grid, helper.getLevel().registryAccess());
            helper.assertTrue(BlueprintExchangeRecipe.averageRarity(other) == BlueprintExchangeRecipe.averageRarity(result)
                            && BlueprintItem.getDestination(other).isEmpty(),
                    "Moving ingredients only changes placement, never reveals or rerolls the preview");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void invalidBlueprintsAndExtraIngredientsAreRejected(GameTestHelper helper) {
        CraftingContainer grid = grid(helper, 2, 2);
        BlueprintExchangeRecipe recipe = recipe(helper);
        ItemStack valid = blueprint(Registration.CIRCUIT.get().getDefaultInstance());
        grid.setItem(0, valid.copy());
        grid.setItem(1, valid.copy());
        helper.assertTrue(!recipe.matches(grid, helper.getLevel()), "Two blueprints are insufficient");
        grid.setItem(2, valid.copy());
        helper.assertTrue(recipe.matches(grid, helper.getLevel()), "Duplicate valid blueprints are accepted");
        for (ItemStack invalid : List.of(new ItemStack(Registration.BLUEPRINT.get()), blueprint(new ItemStack(Items.DIAMOND)),
                recipe.assemble(grid, helper.getLevel().registryAccess()), new ItemStack(Items.PAPER))) {
            grid.setItem(2, invalid);
            helper.assertTrue(!recipe.matches(grid, helper.getLevel()) && recipe.assemble(grid, helper.getLevel().registryAccess()).isEmpty(),
                    "Blank, unknown, unrevealed and non-blueprint inputs cannot be exchanged");
        }
        for (String json : List.of("{broken", "{}", "{\"item\":null}", "{\"item\":{}}",
                "{\"item\":\"ariente:circuit\",\"amount\":{}}")) {
            ItemStack malformed = new ItemStack(Registration.BLUEPRINT.get());
            malformed.getOrCreateTag().putString("destination", json);
            grid.setItem(2, malformed);
            helper.assertTrue(!recipe.matches(grid, helper.getLevel()), "Malformed destination data is rejected safely");
        }
        grid.setItem(2, valid.copy());
        grid.setItem(3, valid.copy());
        helper.assertTrue(!recipe.matches(grid, helper.getLevel()), "Four blueprints are not a three-blueprint exchange");
        grid.setItem(3, new ItemStack(Items.DIAMOND));
        helper.assertTrue(!recipe.matches(grid, helper.getLevel()), "Extra non-blueprint ingredients are rejected");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void clicksRevealExactlyOnePaidBlueprintInBothGrids(GameTestHelper helper) {
        for (boolean table : List.of(false, true)) {
            Player player = player(helper);
            AbstractContainerMenu menu = menu(helper, player, table);
            fill(menu, 1);
            helper.assertTrue(BlueprintExchangeRecipe.isPreview(menu.getSlot(0).getItem()), "Both grids expose a random-blueprint preview");
            menu.clicked(0, 0, ClickType.PICKUP, player);
            assertRevealed(helper, menu.getCarried());
            helper.assertTrue(menu.getSlot(1).getItem().isEmpty() && menu.getSlot(2).getItem().isEmpty()
                    && menu.getSlot(3).getItem().isEmpty() && menu.getSlot(0).getItem().isEmpty(),
                    "Taking one result consumes exactly three blueprints and clears the result slot");
            menu.clicked(0, 0, ClickType.PICKUP, player);
            helper.assertTrue(menu.getCarried().getCount() == 1, "A second click cannot duplicate the result");
            player.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shiftCraftingAndFullInventoriesPreserveThePaidRoll(GameTestHelper helper) {
        for (boolean table : List.of(false, true)) {
            Player player = player(helper);
            AbstractContainerMenu menu = menu(helper, player, table);
            for (int slot = 0; slot < 36; slot++) {
                player.getInventory().setItem(slot, new ItemStack(Items.DIAMOND, 64));
            }
            fill(menu, 1);
            ItemStack before = menu.getSlot(0).getItem().copy();
            player.getRandom().setSeed(1977);
            menu.clicked(0, 0, ClickType.QUICK_MOVE, player);
            helper.assertTrue(ItemStack.matches(before, menu.getSlot(0).getItem()) && !menu.getSlot(1).getItem().isEmpty(),
                    "A failed shift-click neither changes the preview nor consumes ingredients");
            helper.assertTrue(player.getRandom().nextLong() == RandomSource.create(1977).nextLong(),
                    "A full inventory must not even spend the exchange random roll");
            player.getInventory().setItem(0, ItemStack.EMPTY);
            menu.clicked(0, 0, ClickType.QUICK_MOVE, player);
            assertRevealed(helper, player.getInventory().getItem(0));
            helper.assertTrue(menu.getSlot(1).getItem().isEmpty() && menu.getSlot(2).getItem().isEmpty()
                    && menu.getSlot(3).getItem().isEmpty(), "Successful shift-click consumes the ingredients once");
            player.getInventory().clearContent();
            fill(menu, 2);
            menu.clicked(0, 0, ClickType.QUICK_MOVE, player);
            int count = 0;
            for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty()) {
                    assertRevealed(helper, stack);
                    count += stack.getCount();
                }
            }
            helper.assertTrue(count == 2 && menu.getSlot(1).getItem().isEmpty(),
                    "Repeated shift-crafting produces one revealed result per set of three inputs");
            player.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hotbarCraftingAndDeferredOutputsAreRevealed(GameTestHelper helper) {
        Player player = player(helper);
        AbstractContainerMenu menu = menu(helper, player, true);
        fill(menu, 1);
        menu.clicked(0, 0, ClickType.SWAP, player);
        assertRevealed(helper, player.getInventory().getItem(0));
        ItemStack unrelated = preview(helper, commonInputs());
        player.getInventory().setItem(5, unrelated);
        fill(menu, 1);
        menu.clicked(0, 0, ClickType.QUICK_MOVE, player);
        helper.assertTrue(BlueprintExchangeRecipe.isPreview(unrelated),
                "The crafting event must reveal its delivered copy, not an older pending output with the same input patterns");
        ItemStack deferred = preview(helper, commonInputs());
        deferred.getItem().inventoryTick(deferred, helper.getLevel(), player, 1, false);
        assertRevealed(helper, deferred);
        ItemStack alreadyRevealed = deferred.copy();
        deferred.getItem().inventoryTick(deferred, helper.getLevel(), player, 1, false);
        helper.assertTrue(ItemStack.matches(deferred, alreadyRevealed), "An already revealed blueprint is never rerolled");
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void pendingCraftsRespectRecipeChangesBeforeTakingTheResult(GameTestHelper helper) {
        try (ServerConfigScope ignored = new ServerConfigScope()) {
            for (ClickType click : List.of(ClickType.PICKUP, ClickType.QUICK_MOVE)) {
                RecipeConfiguration.EXTREME_CIRCUIT_RECIPE.set(YesNoDefault.YES);
                Player player = player(helper);
                AbstractContainerMenu menu = menu(helper, player, true);
                menu.getSlot(1).set(blueprint(Registration.EXTREME_CIRCUIT.get().getDefaultInstance()));
                menu.getSlot(2).set(blueprint(Registration.MODULE_FLIGHT.get().getDefaultInstance()));
                menu.getSlot(3).set(blueprint(Registration.MODULE_FORCEFIELD.get().getDefaultInstance()));
                helper.assertTrue(BlueprintExchangeRecipe.isPreview(menu.getSlot(0).getItem()), "The enabled recipe must produce a preview");
                RecipeConfiguration.EXTREME_CIRCUIT_RECIPE.set(YesNoDefault.NO);
                player.getRandom().setSeed(1977);
                menu.clicked(0, 0, click, player);
                ItemStack result = click == ClickType.PICKUP ? menu.getCarried() : player.getInventory().items.stream()
                        .filter(stack -> !stack.isEmpty()).findFirst().orElseThrow();
                helper.assertTrue(BlueprintRarity.of(BlueprintRecipeRegistry.findRecipe(BlueprintItem.getDestination(result))) == BlueprintRarity.RARE,
                        "An already prepared exchange uses the current reward catalog after a setting change");
                helper.assertTrue(menu.getSlot(1).getItem().isEmpty() && menu.getSlot(2).getItem().isEmpty()
                        && menu.getSlot(3).getItem().isEmpty(), "The prepared exchange still consumes exactly three inputs");
                player.discard();
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void enabledCatalogAndSmallExtremePoolsStayValid(GameTestHelper helper) {
        boolean world = Ariente.setup.arienteWorld;
        try (ServerConfigScope ignored = new ServerConfigScope()) {
            for (boolean companion : List.of(false, true)) {
                Ariente.setup.arienteWorld = companion;
                for (YesNoDefault setting : YesNoDefault.values()) {
                    RecipeConfiguration.EXTREME_CIRCUIT_RECIPE.set(setting);
                    boolean enabled = RecipeConfiguration.extremeCircuitRecipeEnabled();
                    CraftingContainer grid = grid(helper, 2, 2);
                    grid.setItem(0, blueprint(Registration.EXTREME_CIRCUIT.get().getDefaultInstance()));
                    grid.setItem(1, blueprint(Registration.MODULE_FLIGHT.get().getDefaultInstance()));
                    grid.setItem(2, blueprint(Registration.MODULE_FORCEFIELD.get().getDefaultInstance()));
                    helper.assertTrue(recipe(helper).matches(grid, helper.getLevel()) == enabled,
                            "Disabled Extreme Circuit blueprints cannot enter the exchange");
                    ItemStack rare = preview(helper, rareInputs());
                    BlueprintExchangeRecipe.resolve(rare, new ScriptedRandom(8, 0));
                    helper.assertTrue(BlueprintItem.getDestination(rare).is(Registration.EXTREME_CIRCUIT.get()) == enabled,
                            "Extreme Circuit results respect explicit settings and companion defaults");
                    ItemStack smallPool = preview(helper, blueprint(Registration.MODULE_FLIGHT.get().getDefaultInstance()),
                            blueprint(Registration.MODULE_FORCEFIELD.get().getDefaultInstance()),
                            blueprint(Registration.MODULE_FLIGHT.get().getDefaultInstance()));
                    BlueprintExchangeRecipe.resolve(smallPool, new ScriptedRandom(9, 0));
                    ConstructorRecipe result = BlueprintRecipeRegistry.findRecipe(BlueprintItem.getDestination(smallPool));
                    helper.assertTrue(result != null && (enabled ? result.getDestination().is(Registration.EXTREME_CIRCUIT.get())
                            : BlueprintRarity.of(result) == BlueprintRarity.RARE),
                            "An exhausted Extreme tier falls back to Rare while preserving input exclusions");
                }
            }
        } finally {
            Ariente.setup.arienteWorld = world;
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void serializationAndBatchBlueprintsRemainCanonical(GameTestHelper helper) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            BlueprintRecipeRegistry.BLUEPRINT_EXCHANGE.get().toNetwork(buffer, recipe(helper));
            BlueprintExchangeRecipe decoded = BlueprintRecipeRegistry.BLUEPRINT_EXCHANGE.get().fromNetwork(BlueprintExchangeRecipe.ID, buffer);
            helper.assertTrue(decoded != null && decoded.category() == CraftingBookCategory.MISC
                    && decoded.getIngredients().size() == 3 && decoded.canCraftInDimensions(2, 2)
                    && !decoded.canCraftInDimensions(2, 1), "Recipe sync retains its category, ingredients and grid dimensions");
        } finally {
            buffer.release();
        }
        List<ConstructorRecipe> candidates = BlueprintRecipeRegistry.getRecipes().stream()
                .filter(recipe -> BlueprintRarity.of(recipe) == BlueprintRarity.COMMON)
                .filter(recipe -> !recipe.getDestination().is(Registration.CIRCUIT.get())
                        && !recipe.getDestination().is(Registration.ENERGY_HOLDER.get())
                        && !recipe.getDestination().is(Registration.SMELTER.get().asItem())).toList();
        ConstructorRecipe cable = BlueprintRecipeRegistry.findRecipe(Registration.NETCABLE_NEGARITE.get().asItem().getDefaultInstance());
        ItemStack result = preview(helper, commonInputs());
        BlueprintExchangeRecipe.resolve(result, new ScriptedRandom(1, candidates.indexOf(cable)));
        helper.assertTrue(BlueprintItem.getDestination(result).is(Registration.NETCABLE_NEGARITE.get().asItem())
                && BlueprintItem.getDestination(result).getCount() == 8, "Batch recipes retain their canonical output in exchanged blueprints");
        helper.succeed();
    }

    private static BlueprintExchangeRecipe recipe(GameTestHelper helper) {
        return (BlueprintExchangeRecipe) helper.getLevel().getRecipeManager().byKey(BlueprintExchangeRecipe.ID).orElseThrow();
    }

    private static Player player(GameTestHelper helper) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "blueprint-test"));
    }

    private static AbstractContainerMenu menu(GameTestHelper helper, Player player, boolean table) {
        helper.setBlock(BlockPos.ZERO, Blocks.CRAFTING_TABLE);
        AbstractContainerMenu menu = table ? new CraftingMenu(17, player.getInventory(),
                ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(BlockPos.ZERO))) : player.inventoryMenu;
        player.containerMenu = menu;
        return menu;
    }

    private static CraftingContainer grid(GameTestHelper helper, int width, int height) {
        return new TransientCraftingContainer(player(helper).inventoryMenu, width, height);
    }

    private static ItemStack blueprint(ConstructorRecipe recipe) {
        return blueprint(recipe.getDestination());
    }

    private static ItemStack blueprint(ItemStack destination) {
        return BlueprintItem.makeBluePrint(destination);
    }

    private static ItemStack[] commonInputs() {
        return new ItemStack[]{blueprint(Registration.CIRCUIT.get().getDefaultInstance()),
                blueprint(Registration.ENERGY_HOLDER.get().getDefaultInstance()),
                blueprint(Registration.SMELTER.get().asItem().getDefaultInstance())};
    }

    private static ItemStack[] rareInputs() {
        return new ItemStack[]{blueprint(Registration.ADVANCED_CIRCUIT.get().getDefaultInstance()),
                blueprint(Registration.FLUX_CAPACITOR.get().getDefaultInstance()),
                blueprint(Registration.ENERGY_LANCE.get().getDefaultInstance())};
    }

    private static ItemStack preview(GameTestHelper helper, ItemStack... inputs) {
        CraftingContainer grid = grid(helper, 2, 2);
        for (int index = 0; index < inputs.length; index++) {
            grid.setItem(index, inputs[index].copy());
        }
        helper.assertTrue(recipe(helper).matches(grid, helper.getLevel()), "Fixture must be a valid exchange");
        return recipe(helper).assemble(grid, helper.getLevel().registryAccess());
    }

    private static void fill(AbstractContainerMenu menu, int count) {
        ItemStack[] inputs = commonInputs();
        for (int index = 0; index < inputs.length; index++) {
            menu.getSlot(index + 1).set(inputs[index].copyWithCount(count));
        }
    }

    private static void assertRevealed(GameTestHelper helper, ItemStack stack) {
        ConstructorRecipe recipe = BlueprintRecipeRegistry.findRecipe(BlueprintItem.getDestination(stack));
        helper.assertTrue(stack.is(Registration.BLUEPRINT.get()) && stack.getCount() == 1
                        && !BlueprintExchangeRecipe.isPreview(stack) && recipe != null,
                "Crafting must immediately deliver one usable blueprint");
        for (ItemStack input : commonInputs()) {
            helper.assertTrue(!BlueprintItem.getDestination(input).is(recipe.getDestination().getItem()),
                    "The paid result must differ from every input pattern");
        }
    }

    private static class ScriptedRandom extends LegacyRandomSource {

        private final int tierRoll;
        private final int choice;
        private int calls;

        private ScriptedRandom(int tierRoll, int choice) {
            super(0);
            this.tierRoll = tierRoll;
            this.choice = choice;
        }

        @Override
        public int nextInt(int bound) {
            int value = calls++ == 0 ? tierRoll : choice;
            if (value < 0 || value >= bound) {
                throw new IllegalArgumentException("Invalid test roll " + value + " for " + bound);
            }
            return value;
        }
    }
}
