package mcjty.ariente;

import mcjty.ariente.blocks.ExtremeMachineTile;
import mcjty.ariente.blocks.utility.AutoConstructorTile;
import mcjty.ariente.blocks.utility.BlueprintStorageTile;
import mcjty.ariente.blocks.utility.RecyclerTile;
import mcjty.ariente.blocks.utility.SmelterTile;
import mcjty.ariente.config.PowerConfiguration;
import mcjty.ariente.config.RecipeConfiguration;
import mcjty.ariente.config.YesNoDefault;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.power.IPowerBlob;
import mcjty.ariente.power.PowerSystem;
import mcjty.ariente.power.PowerType;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.recipes.ConstructorRecipe;
import mcjty.ariente.recipes.Recycling;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.List;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class ExtremeCircuitTests {

    @GameTest(template = "empty")
    public static void installationIsSingleUseAndSurvivesLootAndPlacement(GameTestHelper helper) {
        for (Block block : List.of(Registration.SMELTER.get(), Registration.RECYCLER.get(), Registration.AUTO_CONSTRUCTOR.get(),
                Registration.AUTOMATION_FIELD.get(), Registration.FORCEFIELD.get(), Registration.CHARGER.get(),
                Registration.PURIFIER_START.get(), Registration.POWER_COMBINER.get())) {
            ExtremeMachineTile machine = place(helper, block);
            Player player = helper.makeMockSurvivalPlayer();
            ItemStack circuits = new ItemStack(Registration.EXTREME_CIRCUIT.get(), 2);
            player.setItemInHand(InteractionHand.MAIN_HAND, circuits);
            UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(machine.getBlockPos()), Direction.UP, machine.getBlockPos(), false));
            helper.assertTrue(circuits.onItemUseFirst(context).consumesAction() && circuits.getCount() == 1,
                    "Installing must consume exactly one circuit before the machine handles interaction: " + block);
            circuits.onItemUseFirst(context);
            helper.assertTrue(circuits.getCount() == 1 && machine.hasExtremeCircuit(), "Repeated clicks must not consume or stack upgrades");
            ItemStack drop = drop(helper, machine);
            helper.assertTrue(ExtremeMachineTile.hasExtremeCircuit(drop), "Machine loot must preserve the installed circuit");
            BlockPos pos = machine.getBlockPos();
            helper.getLevel().removeBlock(pos, false);
            helper.getLevel().setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
            player.setItemInHand(InteractionHand.MAIN_HAND, drop);
            BlockPlaceContext placement = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, drop,
                    new BlockHitResult(Vec3.atCenterOf(pos.below()).add(0, .5, 0), Direction.UP, pos.below(), false));
            helper.assertTrue(((BlockItem) drop.getItem()).place(placement).consumesAction()
                    && ((ExtremeMachineTile) helper.getLevel().getBlockEntity(pos)).hasExtremeCircuit(),
                    "Actual block-item placement must restore the upgrade: " + block);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void smelterUsesHalfPowerAndCompletesInHalfTheTime(GameTestHelper helper) {
        try (ServerConfigScope ignored = new ServerConfigScope()) {
            PowerConfiguration.SMELTER_SPEED.set(10);
            PowerConfiguration.SMELTER_POWER_PER_TICK.set(20);
            SmelterTile machine = (SmelterTile) place(helper, Registration.SMELTER.get());
            IItemHandlerModifiable items = inventory(machine);
            items.setStackInSlot(SmelterTile.SLOT_INPUT, new ItemStack(Items.RAW_IRON, 3));
            int[] networks = supplyPower(helper, machine);
            for (int tick = 0; tick < 20; tick++) {
                machine.tickServer();
            }
            helper.assertTrue(items.getStackInSlot(SmelterTile.SLOT_OUTPUT).getCount() == 1, "Ordinary smelting takes twenty ticks");
            assertPower(helper, networks, 9600);
            machine.installExtremeCircuit();
            for (int tick = 0; tick < 9; tick++) {
                machine.tickServer();
            }
            helper.assertTrue(items.getStackInSlot(SmelterTile.SLOT_OUTPUT).getCount() == 1, "Upgraded smelting must still need ten ticks");
            machine.tickServer();
            helper.assertTrue(items.getStackInSlot(SmelterTile.SLOT_OUTPUT).getCount() == 2 && machine.getUsingPower() == 10,
                    "The upgrade doubles speed and halves actual per-tick consumption");
            assertPower(helper, networks, 9500);
            PowerConfiguration.SMELTER_SPEED.set(200);
            items.setStackInSlot(SmelterTile.SLOT_INPUT, new ItemStack(Items.RAW_IRON, 3));
            machine.tickServer();
            helper.assertTrue(items.getStackInSlot(SmelterTile.SLOT_OUTPUT).getCount() == 4,
                    "The upgrade must still double throughput when the normal recipe already completes in one tick");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void autoConstructorDoublesProductionAtHalfThePowerDraw(GameTestHelper helper) {
        ConstructorRecipe recipe = BlueprintRecipeRegistry.findRecipe(new ItemStack(Registration.CIRCUIT.get()));
        for (boolean upgraded : List.of(false, true)) {
            AutoConstructorTile machine = (AutoConstructorTile) place(helper, Registration.AUTO_CONSTRUCTOR.get());
            if (upgraded) {
                machine.installExtremeCircuit();
            }
            IItemHandlerModifiable items = inventory(machine);
            for (int slot = 0; slot < recipe.getIngredientList().size(); slot++) {
                items.setStackInSlot(slot, recipe.getIngredientList().get(slot).copyWithCount(64));
            }
            BlockPos libraryPos = machine.getBlockPos().east();
            helper.getLevel().setBlockAndUpdate(libraryPos, Registration.BLUEPRINT_STORAGE.get().defaultBlockState());
            BlueprintStorageTile library = (BlueprintStorageTile) helper.getLevel().getBlockEntity(libraryPos);
            for (int slot = 0; slot < library.getItems().getSlots(); slot++) {
                library.getItems().setStackInSlot(slot, BlueprintItem.makeBluePrint(recipe.getDestination()));
            }
            int[] networks = supplyPower(helper, machine);
            for (int tick = 0; tick < 20; tick++) {
                machine.tickServer();
            }
            helper.assertTrue(items.getStackInSlot(AutoConstructorTile.SLOT_OUTPUT).getCount() == (upgraded ? 4 : 2),
                    "Twenty ticks must produce twice as many circuits after upgrading");
            assertPower(helper, networks, upgraded ? 9800 : 9600);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void recyclingReservesSpaceForTheGuaranteedCircuit(GameTestHelper helper) {
        ExtremeMachineTile source = place(helper, Registration.SMELTER.get());
        source.installExtremeCircuit();
        ItemStack upgraded = drop(helper, source);
        RecyclerTile recycler = (RecyclerTile) place(helper, Registration.RECYCLER.get());
        IItemHandlerModifiable items = inventory(recycler);
        ConstructorRecipe recipe = Recycling.findRecipe(upgraded);
        List<ItemStack> materials = Recycling.recover(recipe);
        for (int slot = 1; slot <= RecyclerTile.OUTPUTS; slot++) {
            items.setStackInSlot(slot, slot <= materials.size() ? materials.get(slot - 1).copyWithCount(1) : new ItemStack(Items.DIRT, 64));
        }
        items.setStackInSlot(0, upgraded);
        int[] networks = supplyPower(helper, recycler);
        for (int tick = 0; tick < RecyclerTile.RECYCLE_TICKS; tick++) {
            recycler.tickServer();
        }
        helper.assertTrue(!items.getStackInSlot(0).isEmpty(), "No room for the circuit must pause recycling without consuming input");
        assertPower(helper, networks, 10000);
        items.setStackInSlot(RecyclerTile.OUTPUTS, ItemStack.EMPTY);
        recycler.installExtremeCircuit();
        for (int tick = 0; tick < RecyclerTile.RECYCLE_TICKS / 2; tick++) {
            recycler.tickServer();
        }
        helper.assertTrue(items.getStackInSlot(0).isEmpty(), "An upgraded Recycler completes in twenty ticks");
        helper.assertTrue(items.getStackInSlot(RecyclerTile.OUTPUTS).is(Registration.EXTREME_CIRCUIT.get())
                && items.getStackInSlot(RecyclerTile.OUTPUTS).getCount() == 1, "Recycling must always return exactly one installed circuit");
        assertPower(helper, networks, 9900);
        helper.assertTrue(Recycling.findRecipe(new ItemStack(Registration.EXTREME_CIRCUIT.get())) == null,
                "Extreme Circuits themselves must never be recyclable");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fallbackRecipeRequiresRareBlueprintAndHonorsConfig(GameTestHelper helper) {
        boolean originalWorld = Ariente.setup.arienteWorld;
        try (ServerConfigScope ignored = new ServerConfigScope()) {
            for (boolean world : List.of(false, true)) {
                Ariente.setup.arienteWorld = world;
                for (YesNoDefault setting : YesNoDefault.values()) {
                    RecipeConfiguration.EXTREME_CIRCUIT_RECIPE.set(setting);
                    boolean enabled = setting == YesNoDefault.YES || setting == YesNoDefault.DEFAULT && !world;
                    ItemStack circuit = new ItemStack(Registration.EXTREME_CIRCUIT.get());
                    ConstructorRecipe recipe = BlueprintRecipeRegistry.findRecipe(circuit);
                    helper.assertTrue((recipe != null) == enabled, "Recipe and blueprint availability must follow " + world + "/" + setting);
                    if (recipe != null) {
                        helper.assertTrue(recipe.getChance() == BlueprintRecipeRegistry.EXTREME
                                && recipe.getIngredientList().stream().anyMatch(stack -> stack.is(Items.NETHER_STAR)),
                                "The fallback must require a Nether Star and an extremely rare blueprint");
                        helper.assertTrue(BlueprintRecipeRegistry.findRecipe(BlueprintItem.getDestination(BlueprintItem.makeBluePrint(circuit))) == recipe,
                                "The normal blueprint system must resolve the fallback recipe");
                    }
                    helper.assertTrue(Recycling.findRecipe(circuit) == null, "Enabling construction must never enable recycling the circuit");
                    // Exercise cached blueprint selection across config changes in the same server.
                    helper.assertTrue(BlueprintRecipeRegistry.getRandomRecipes().weightedList.stream()
                                    .anyMatch(entry -> entry.getLeft().getDestination().is(Registration.EXTREME_CIRCUIT.get())) == enabled,
                            "Cached random blueprint loot must also follow the current config");
                }
            }
        } finally {
            Ariente.setup.arienteWorld = originalWorld;
        }
        helper.succeed();
    }

    private static ExtremeMachineTile place(GameTestHelper helper, Block block) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().removeBlock(pos, false);
        helper.getLevel().setBlockAndUpdate(pos, block.defaultBlockState());
        return (ExtremeMachineTile) helper.getLevel().getBlockEntity(pos);
    }

    private static ItemStack drop(GameTestHelper helper, ExtremeMachineTile machine) {
        List<ItemStack> drops = Block.getDrops(machine.getBlockState(), helper.getLevel(), machine.getBlockPos(), machine);
        helper.assertTrue(drops.size() == 1, "Upgraded machines drop one item with the installed circuit inside");
        return drops.get(0);
    }

    private static IItemHandlerModifiable inventory(ExtremeMachineTile machine) {
        return (IItemHandlerModifiable) machine.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
    }

    private static int[] supplyPower(GameTestHelper helper, ExtremeMachineTile machine) {
        PowerSystem power = PowerSystem.getPowerSystem(helper.getLevel());
        int[] networks = new int[2];
        for (PowerType type : PowerType.values()) {
            BlockPos pos = machine.getBlockPos().relative(type == PowerType.NEGARITE ? Direction.NORTH : Direction.SOUTH);
            helper.getLevel().setBlockAndUpdate(pos, (type == PowerType.NEGARITE ? Registration.NEGARITE_GENERATOR.get()
                    : Registration.POSIRITE_GENERATOR.get()).defaultBlockState());
            int id = power.allocateId();
            ((IPowerBlob) helper.getLevel().getBlockEntity(pos)).fillCableId(id);
            power.addPower(id, 10000, type);
            networks[type.ordinal()] = id;
        }
        return networks;
    }

    private static void assertPower(GameTestHelper helper, int[] networks, long expected) {
        PowerSystem power = PowerSystem.getPowerSystem(helper.getLevel());
        for (PowerType type : PowerType.values()) {
            helper.assertTrue(power.getTotalPower(networks[type.ordinal()], type) == expected,
                    "Actual " + type + " network consumption must match: expected " + expected);
        }
    }
}
