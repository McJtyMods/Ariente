package mcjty.ariente;

import mcjty.ariente.blocks.purifier.PurifierStructure;
import mcjty.ariente.blocks.purifier.PurifierTile;
import mcjty.ariente.blocks.generators.NegariteGeneratorTile;
import mcjty.ariente.blocks.generators.PosiriteGeneratorTile;
import mcjty.ariente.config.PowerConfiguration;
import mcjty.ariente.power.IPowerBlob;
import mcjty.ariente.power.PowerSystem;
import mcjty.ariente.power.PowerType;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.recipes.PurifierRecipes;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class PurifierTests {

    private static final int TEST_POWER = 10000000;

    @GameTest(template = "purifier", batch = "purifier")
    public static void allHorizontalDirectionsAndTubeCountsGiveExactYieldsTimeAndPower(GameTestHelper helper) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            for (int tubes = 1; tubes <= 4; tubes++) {
                Fixture fixture = place(helper, direction, tubes);
                PurifierTile start = fixture.start();
                start.getItems().insertItem(0, new ItemStack(Registration.ORE_SILVER.get(), 2), false);
                int ticks = tubes * PowerConfiguration.PURIFIER_TICKS_PER_TUBE.get();
                long cost = (long) tubes * PowerConfiguration.PURIFIER_POWER_PER_TUBE.get();
                tick(start, ticks - 1);
                helper.assertTrue(fixture.finish().getItems().getStackInSlot(0).isEmpty(), "No early output for " + direction + "/" + tubes);
                helper.assertTrue(start.getItems().getStackInSlot(0).getCount() == 2, "The input stays available until output can be committed");
                start.tickServer();
                ItemStack result = fixture.finish().getItems().getStackInSlot(0);
                helper.assertTrue(result.is(Registration.RAW_SILVER.get()) && result.getCount() == tubes + 1,
                        "One through four tubes recover two through five raw metals");
                helper.assertTrue(start.getItems().getStackInSlot(0).getCount() == 1, "Exactly one ore is consumed");
                PowerSystem power = PowerSystem.getPowerSystem(helper.getLevel());
                helper.assertTrue(power.getTotalPower(fixture.negarite(), PowerType.NEGARITE) == TEST_POWER - ticks * cost
                        && power.getTotalPower(fixture.posirite(), PowerType.POSIRITE) == TEST_POWER - ticks * cost,
                        "Each active tube costs the configured amount of both fluxes every tick");
                clear(helper);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "purifier", batch = "purifier")
    public static void fourTubesNeedTwoTanksOfEachTypeForFullSpeedButOneTankCanMakeProgress(GameTestHelper helper) {
        int previousCost = PowerConfiguration.PURIFIER_POWER_PER_TUBE.get();
        try {
            PowerConfiguration.PURIFIER_POWER_PER_TUBE.set(400);
            for (int tanks = 1; tanks <= 2; tanks++) {
                Fixture fixture = place(helper, Direction.EAST, 4);
                BlockPos start = fixture.start().getBlockPos();
                helper.getLevel().removeBlock(start.below(), false);
                helper.getLevel().removeBlock(start.above(), false);
                helper.getLevel().setBlockAndUpdate(start.north(), Registration.NEGARITE_GENERATOR.get().defaultBlockState());
                helper.getLevel().setBlockAndUpdate(start.south(), Registration.POSIRITE_GENERATOR.get().defaultBlockState());
                for (int height = 1; height <= tanks; height++) {
                    helper.getLevel().setBlockAndUpdate(start.north().above(height), Registration.NEGARITE_TANK.get().defaultBlockState());
                    helper.getLevel().setBlockAndUpdate(start.south().above(height), Registration.POSIRITE_TANK.get().defaultBlockState());
                }
                var negarite = (NegariteGeneratorTile) helper.getLevel().getBlockEntity(start.north());
                var posirite = (PosiriteGeneratorTile) helper.getLevel().getBlockEntity(start.south());
                PowerSystem power = PowerSystem.getPowerSystem(helper.getLevel());
                negarite.fillCableId(power.allocateId());
                posirite.fillCableId(power.allocateId());
                negarite.feedDust(4);
                posirite.feedDust(4);
                fixture.start().getItems().insertItem(0, new ItemStack(Items.IRON_ORE), false);
                int duration = 4 * PowerConfiguration.PURIFIER_TICKS_PER_TUBE.get();
                for (int tick = 0; tick < duration; tick++) {
                    tickGeneratorsAndPurifier(power, negarite, posirite, fixture.start());
                }
                if (tanks == 1) {
                    helper.assertTrue(fixture.finish().getItems().getStackInSlot(0).isEmpty()
                            && fixture.start().getProgress() > 0 && fixture.start().getProgress() < duration,
                            "One tank of each type runs a four-tube purifier at reduced speed");
                    for (int tick = 0; tick < duration; tick++) {
                        tickGeneratorsAndPurifier(power, negarite, posirite, fixture.start());
                    }
                }
                ItemStack output = fixture.finish().getItems().getStackInSlot(0);
                helper.assertTrue(output.is(Items.RAW_IRON) && output.getCount() == 5
                        && fixture.start().getItems().getStackInSlot(0).isEmpty(),
                        "Two tanks sustain full speed; one tank eventually completes without losing yield");
                clear(helper);
            }
        } finally {
            PowerConfiguration.PURIFIER_POWER_PER_TUBE.set(previousCost);
        }
        helper.succeed();
    }

    private static void tickGeneratorsAndPurifier(PowerSystem power, NegariteGeneratorTile negarite,
                                                  PosiriteGeneratorTile posirite, PurifierTile start) {
        power.tick();
        negarite.tickServer();
        posirite.tickServer();
        start.tickServer();
    }

    @GameTest(template = "purifier", batch = "purifier")
    public static void missingPowerBlockedOutputAndBrokenLinePauseWithoutLosingOre(GameTestHelper helper) {
        Fixture fixture = place(helper, Direction.EAST, 2);
        PurifierTile start = fixture.start();
        start.getItems().insertItem(0, new ItemStack(Registration.ORE_LITHIUM.get()), false);
        tick(start, 40);
        CompoundTag saved = start.saveWithoutMetadata();
        start.load(saved);
        helper.assertTrue(start.getProgress() == 40, "Work progress survives saving and loading");
        helper.getLevel().removeBlock(start.getBlockPos().above(), false);
        tick(start, 20);
        helper.assertTrue(start.getProgress() == 40 && start.getUsingPower() == 0, "Losing one flux pauses progress");
        helper.getLevel().setBlockAndUpdate(start.getBlockPos().above(), Registration.POSIRITE_GENERATOR.get().defaultBlockState());
        ((IPowerBlob) helper.getLevel().getBlockEntity(start.getBlockPos().above())).fillCableId(fixture.posirite());
        fixture.finish().getItems().setStackInSlot(0, new ItemStack(Registration.RAW_LITHIUM.get(), 63));
        tick(start, 20);
        helper.assertTrue(start.getProgress() == 40 && start.getUsingPower() == 0, "The entire output must fit before power is spent");
        fixture.finish().getItems().setStackInSlot(0, new ItemStack(Items.DIAMOND));
        tick(start, 20);
        helper.assertTrue(start.getProgress() == 40, "An incompatible output is preserved");
        fixture.finish().getItems().setStackInSlot(0, ItemStack.EMPTY);
        BlockPos tube = start.getBlockPos().east();
        helper.getLevel().removeBlock(tube, false);
        tick(start, 20);
        helper.assertTrue(start.getProgress() == 40 && start.getItems().getStackInSlot(0).getCount() == 1,
                "Breaking the line pauses work and keeps the original ore");
        helper.getLevel().setBlockAndUpdate(tube, Registration.PURIFIER_TUBE.get().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.X));
        tick(start, 2 * PowerConfiguration.PURIFIER_TICKS_PER_TUBE.get() - 40);
        helper.assertTrue(fixture.finish().getItems().getStackInSlot(0).is(Registration.RAW_LITHIUM.get())
                && fixture.finish().getItems().getStackInSlot(0).getCount() == 3 && start.getItems().getStackInSlot(0).isEmpty(),
                "Restoring the same line resumes work and produces one batch");
        helper.succeed();
    }

    @GameTest(template = "purifier", batch = "purifier")
    public static void invalidConnectionsAndChangingLengthCannotKeepTheHigherYield(GameTestHelper helper) {
        Fixture fixture = place(helper, Direction.EAST, 4);
        PurifierTile start = fixture.start();
        start.getItems().insertItem(0, new ItemStack(Registration.ORE_PLATINUM.get()), false);
        tick(start, 80);
        BlockPos extra = start.getBlockPos().north();
        helper.getLevel().setBlockAndUpdate(extra, Registration.PURIFIER_TUBE.get().defaultBlockState());
        helper.assertTrue(PurifierStructure.find(helper.getLevel(), start.getBlockPos()) == null, "A start cannot attach to two tubes");
        tick(start, 5);
        helper.assertTrue(start.getProgress() == 80, "An ambiguous connection does not process");
        helper.getLevel().removeBlock(extra, false);
        for (int i = 2; i <= 5; i++) {
            helper.getLevel().removeBlock(start.getBlockPos().east(i), false);
        }
        helper.getLevel().setBlockAndUpdate(start.getBlockPos().east(2), Registration.PURIFIER_FINISH.get().defaultBlockState());
        start.tickServer();
        helper.assertTrue(start.getProgress() == 1, "Changing the line length restarts work");
        tick(start, PowerConfiguration.PURIFIER_TICKS_PER_TUBE.get() - 1);
        PurifierTile finish = (PurifierTile) helper.getLevel().getBlockEntity(start.getBlockPos().east(2));
        helper.assertTrue(finish.getItems().getStackInSlot(0).getCount() == 2, "A shorter line never inherits the larger recovery");
        clear(helper);
        fixture = place(helper, Direction.EAST, 5);
        helper.assertTrue(PurifierStructure.find(helper.getLevel(), fixture.start().getBlockPos()) == null, "Five tubes exceed the default limit");
        clear(helper);
        fixture = place(helper, Direction.EAST, 0);
        helper.assertTrue(PurifierStructure.find(helper.getLevel(), fixture.start().getBlockPos()) == null, "At least one tube is required");
        helper.succeed();
    }

    @GameTest(template = "purifier", batch = "purifier")
    public static void automationAndPlayerTransfersRespectInputOutputAndInventorySpace(GameTestHelper helper) {
        Fixture fixture = place(helper, Direction.NORTH, 1);
        Player player = helper.makeMockSurvivalPlayer();
        player.setPos(fixture.start().getBlockPos().getX() + .5, fixture.start().getBlockPos().getY(), fixture.start().getBlockPos().getZ() + .5);
        for (Direction side : Direction.values()) {
            IItemHandler input = fixture.start().getCapability(ForgeCapabilities.ITEM_HANDLER, side).orElseThrow(IllegalStateException::new);
            IItemHandler output = fixture.finish().getCapability(ForgeCapabilities.ITEM_HANDLER, side).orElseThrow(IllegalStateException::new);
            helper.assertTrue(input.getSlots() == 1 && output.getSlots() == 1, "Both endpoints expose one stack on every face");
            helper.assertTrue(input.insertItem(0, new ItemStack(Registration.ORE_MANGANESE.get()), true).isEmpty(), "Every start face accepts supported ore");
            helper.assertTrue(!input.insertItem(0, new ItemStack(Registration.RAW_MANGANESE.get()), true).isEmpty(), "Raw metals cannot be purified again");
            helper.assertTrue(!input.insertItem(0, new ItemStack(Registration.ORE_NEGARITE.get()), true).isEmpty(), "Fuel ores are outside this processing system");
            helper.assertTrue(!output.insertItem(0, new ItemStack(Registration.RAW_MANGANESE.get()), true).isEmpty(), "Finish rejects insertion");
        }
        player.getInventory().setItem(0, new ItemStack(Registration.ORE_MANGANESE.get(), 4));
        fixture.start().transferToMachine(player, 0);
        helper.assertTrue(player.getInventory().getItem(0).isEmpty() && fixture.start().getItems().getStackInSlot(0).getCount() == 4,
                "The HoloGUI input action transfers a stack atomically");
        tick(fixture.start(), PowerConfiguration.PURIFIER_TICKS_PER_TUBE.get());
        for (int slot = 0; slot < 36; slot++) {
            player.getInventory().setItem(slot, new ItemStack(Items.STONE, 64));
        }
        fixture.finish().transferToPlayer(player);
        helper.assertTrue(fixture.finish().getItems().getStackInSlot(0).getCount() == 2, "A full player inventory cannot lose output");
        player.getInventory().setItem(8, new ItemStack(Registration.RAW_MANGANESE.get(), 63));
        fixture.finish().transferToPlayer(player);
        helper.assertTrue(player.getInventory().getItem(8).getCount() == 64 && fixture.finish().getItems().getStackInSlot(0).getCount() == 1,
                "A partial player transfer leaves the remainder at the finish");
        IItemHandler output = fixture.finish().getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.DOWN).orElseThrow(IllegalStateException::new);
        helper.assertTrue(output.extractItem(0, 64, true).getCount() == 1 && output.getStackInSlot(0).getCount() == 1, "Extraction simulation does not mutate output");
        helper.assertTrue(output.extractItem(0, 64, false).getCount() == 1 && output.getStackInSlot(0).isEmpty(), "Automation extracts completed raw metal");
        helper.succeed();
    }

    @GameTest(template = "purifier", batch = "purifier")
    public static void configuredRecoveryAndMachineLootPreserveWorkWithoutExtraItems(GameTestHelper helper) {
        int previousTicks = PowerConfiguration.PURIFIER_TICKS_PER_TUBE.get();
        int previousYield = PowerConfiguration.PURIFIER_BASE_YIELD.get();
        int previousExtra = PowerConfiguration.PURIFIER_EXTRA_YIELD.get();
        try {
            PowerConfiguration.PURIFIER_TICKS_PER_TUBE.set(4);
            PowerConfiguration.PURIFIER_BASE_YIELD.set(3);
            PowerConfiguration.PURIFIER_EXTRA_YIELD.set(2);
            Fixture fixture = place(helper, Direction.SOUTH, 3);
            fixture.start().getItems().insertItem(0, new ItemStack(Registration.ORE_SILVER.get(), 2), false);
            tick(fixture.start(), 5);
            List<ItemStack> drops = Block.getDrops(fixture.start().getBlockState(), helper.getLevel(), fixture.start().getBlockPos(), fixture.start());
            helper.assertTrue(drops.size() == 1, "Breaking a start drops one configured block");
            CompoundTag contents = BlockItem.getBlockEntityData(drops.get(0));
            helper.assertTrue(contents.getCompound("Info").getInt("purifierProgress") == 5, "Dropped starts preserve completed work");
            tick(fixture.start(), 7);
            helper.assertTrue(fixture.finish().getItems().getStackInSlot(0).getCount() == 7,
                    "Configured recovery: output=" + fixture.finish().getItems().getStackInSlot(0) + ", progress="
                            + fixture.start().getProgress() + ", ticks/tube=" + PowerConfiguration.PURIFIER_TICKS_PER_TUBE.get()
                            + ", yield=" + PurifierTile.yieldForTubes(3) + ", power=" + fixture.start().getUsingPower());
            helper.assertTrue(fixture.start().getItems().getStackInSlot(0).getCount() == 1, "One ore is consumed at configured speed");
            for (Block block : List.of(Registration.PURIFIER_START.get(), Registration.PURIFIER_FINISH.get(), Registration.PURIFIER_TUBE.get())) {
                var recipe = BlueprintRecipeRegistry.findRecipe(new ItemStack(block));
                helper.assertTrue(recipe != null && recipe.getIngredientList().stream().noneMatch(stack -> stack.is(Items.NETHER_STAR)),
                        "All three blocks have blueprints without Nether Stars");
            }
        } finally {
            PowerConfiguration.PURIFIER_TICKS_PER_TUBE.set(previousTicks);
            PowerConfiguration.PURIFIER_BASE_YIELD.set(previousYield);
            PowerConfiguration.PURIFIER_EXTRA_YIELD.set(previousExtra);
        }
        helper.succeed();
    }

    @GameTest(template = "purifier", batch = "purifier")
    public static void vanillaRawMetalOresInsertWithoutPowerOrACompleteLine(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(6, 2, 6));
        helper.getLevel().setBlockAndUpdate(pos, Registration.PURIFIER_START.get().defaultBlockState());
        PurifierTile start = (PurifierTile) helper.getLevel().getBlockEntity(pos);
        Player player = helper.makeMockSurvivalPlayer();
        player.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
        Item[] ores = {Items.IRON_ORE, Items.DEEPSLATE_IRON_ORE, Items.GOLD_ORE,
                Items.DEEPSLATE_GOLD_ORE, Items.COPPER_ORE, Items.DEEPSLATE_COPPER_ORE};
        Item[] raw = {Items.RAW_IRON, Items.RAW_IRON, Items.RAW_GOLD, Items.RAW_GOLD, Items.RAW_COPPER, Items.RAW_COPPER};
        for (int i = 0; i < ores.length; i++) {
            helper.assertTrue(start.findRecipe(new ItemStack(ores[i])).result().is(raw[i]), "Vanilla ore discovers its matching raw metal");
            for (Direction side : Direction.values()) {
                IItemHandler input = start.getCapability(ForgeCapabilities.ITEM_HANDLER, side).orElseThrow(IllegalStateException::new);
                helper.assertTrue(input.insertItem(0, new ItemStack(ores[i]), true).isEmpty(), "Unpowered automation accepts " + ores[i]);
            }
            player.getInventory().setItem(0, new ItemStack(ores[i], 4));
            start.transferToMachine(player, 0);
            tick(start, 2);
            helper.assertTrue(player.getInventory().getItem(0).isEmpty() && start.getItems().getStackInSlot(0).getCount() == 4
                    && start.getProgress() == 0 && start.getUsingPower() == 0, "The unpowered HoloGUI accepts and retains " + ores[i]);
            start.getItems().setStackInSlot(0, ItemStack.EMPTY);
        }
        for (Item item : List.of(Items.RAW_IRON, Items.IRON_INGOT, Items.DIAMOND_ORE, Items.COAL_ORE, Items.REDSTONE_ORE)) {
            helper.assertTrue(!start.getItems().isItemValid(0, new ItemStack(item)), "Only ores with raw-material forms are automatically supported");
        }
        helper.succeed();
    }

    @GameTest(template = "purifier", batch = "purifier")
    public static void automaticallyDiscoveredVanillaAndModdedOresProduceRawMetals(GameTestHelper helper) {
        Item[] ores = {Items.IRON_ORE, Items.DEEPSLATE_IRON_ORE, Items.GOLD_ORE, Items.DEEPSLATE_GOLD_ORE,
                Items.COPPER_ORE, Items.DEEPSLATE_COPPER_ORE, testItem(PurifierTestContent.ORE), testItem(PurifierTestContent.BLOCK_TAG_ORE)};
        Item[] outputs = {Items.RAW_IRON, Items.RAW_IRON, Items.RAW_GOLD, Items.RAW_GOLD,
                Items.RAW_COPPER, Items.RAW_COPPER, testItem(PurifierTestContent.ALTERNATIVE_RAW), testItem(PurifierTestContent.OTHER_RAW)};
        for (int i = 0; i < ores.length; i++) {
            Fixture fixture = place(helper, Direction.EAST, 4);
            helper.assertTrue(fixture.start().getItems().insertItem(0, new ItemStack(ores[i]), false).isEmpty(), "Automatic insertion accepts " + ores[i]);
            tick(fixture.start(), 4 * PowerConfiguration.PURIFIER_TICKS_PER_TUBE.get());
            ItemStack result = fixture.finish().getItems().getStackInSlot(0);
            helper.assertTrue(result.is(outputs[i]) && result.getCount() == 5 && fixture.start().getItems().getStackInSlot(0).isEmpty(),
                    "Tagged ores, including block-tag-only ores, recover five matching raw metals without individual recipes");
            clear(helper);
        }
        helper.succeed();
    }

    @GameTest(template = "purifier", batch = "purifier")
    public static void outputPreferencesAndJsonOverridesAreDeterministicAndMatchJei(GameTestHelper helper) {
        List<? extends String> previous = PowerConfiguration.PURIFIER_PREFERRED_OUTPUT_MODS.get();
        try {
            PowerConfiguration.PURIFIER_PREFERRED_OUTPUT_MODS.set(List.of("purifier_test", "minecraft", "ariente"));
            ItemStack ore = new ItemStack(testItem(PurifierTestContent.ORE));
            helper.assertTrue(PurifierRecipes.find(helper.getLevel(), ore).result().is(testItem(PurifierTestContent.RAW)), "Configured providers take priority");
            PowerConfiguration.PURIFIER_PREFERRED_OUTPUT_MODS.set(List.of("purifier_alternative_test", "minecraft", "ariente"));
            helper.assertTrue(PurifierRecipes.find(helper.getLevel(), ore).result().is(testItem(PurifierTestContent.ALTERNATIVE_RAW)), "Changing preference invalidates the cached output");
            ItemStack overridden = new ItemStack(testItem(PurifierTestContent.OVERRIDE_ORE));
            var recipe = PurifierRecipes.find(helper.getLevel(), overridden);
            helper.assertTrue(recipe.id().equals(new ResourceLocation(Ariente.MODID, "purifying/test_override"))
                    && recipe.result().is(testItem(PurifierTestContent.RAW)), "Explicit JSON overrides automatic output selection");
            var shown = PurifierRecipes.getRecipes(helper.getLevel()).stream().filter(entry -> entry.ingredient().test(overridden)).toList();
            helper.assertTrue(shown.size() == 1 && shown.get(0).result().is(testItem(PurifierTestContent.RAW)), "JEI has exactly the effective overridden conversion");
            helper.assertTrue(PurifierRecipes.find(helper.getLevel(), new ItemStack(testItem(PurifierTestContent.AMBIGUOUS_ORE))) == null,
                    "An ore tagged as two different metals requires an explicit recipe");
        } finally {
            PowerConfiguration.PURIFIER_PREFERRED_OUTPUT_MODS.set(previous);
        }
        helper.succeed();
    }

    private static Item testItem(ResourceLocation id) {
        return ForgeRegistries.ITEMS.getValue(id);
    }

    private static Fixture place(GameTestHelper helper, Direction direction, int tubes) {
        BlockPos pos = helper.absolutePos(new BlockPos(6, 2, 6));
        helper.getLevel().setBlockAndUpdate(pos, Registration.PURIFIER_START.get().defaultBlockState());
        for (int i = 1; i <= tubes; i++) {
            helper.getLevel().setBlockAndUpdate(pos.relative(direction, i), Registration.PURIFIER_TUBE.get().defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_AXIS, direction.getAxis()));
        }
        BlockPos finish = pos.relative(direction, tubes + 1);
        helper.getLevel().setBlockAndUpdate(finish, Registration.PURIFIER_FINISH.get().defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos.below(), Registration.NEGARITE_GENERATOR.get().defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos.above(), Registration.POSIRITE_GENERATOR.get().defaultBlockState());
        PowerSystem power = PowerSystem.getPowerSystem(helper.getLevel());
        int negarite = power.allocateId();
        int posirite = power.allocateId();
        ((IPowerBlob) helper.getLevel().getBlockEntity(pos.below())).fillCableId(negarite);
        ((IPowerBlob) helper.getLevel().getBlockEntity(pos.above())).fillCableId(posirite);
        power.addPower(negarite, TEST_POWER, PowerType.NEGARITE);
        power.addPower(posirite, TEST_POWER, PowerType.POSIRITE);
        return new Fixture((PurifierTile) helper.getLevel().getBlockEntity(pos), (PurifierTile) helper.getLevel().getBlockEntity(finish), negarite, posirite);
    }

    private static void clear(GameTestHelper helper) {
        for (BlockPos pos : BlockPos.betweenClosed(0, 1, 0, 12, 4, 12)) {
            helper.getLevel().setBlockAndUpdate(helper.absolutePos(pos), Blocks.AIR.defaultBlockState());
        }
    }

    private static void tick(PurifierTile tile, int ticks) {
        for (int i = 0; i < ticks; i++) {
            tile.tickServer();
        }
    }

    private record Fixture(PurifierTile start, PurifierTile finish, int negarite, int posirite) {
    }
}
