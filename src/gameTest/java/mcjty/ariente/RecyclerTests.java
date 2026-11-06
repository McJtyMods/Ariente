package mcjty.ariente;

import mcjty.ariente.blocks.utility.RecyclerTile;
import mcjty.ariente.power.IPowerBlob;
import mcjty.ariente.power.PowerSystem;
import mcjty.ariente.power.PowerType;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.recipes.ConstructorRecipe;
import mcjty.ariente.recipes.Recycling;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.gui.HoloGuiEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.List;

import static mcjty.ariente.gui.InventoryLayout.GRID_X;
import static mcjty.ariente.gui.InventoryLayout.HOTBAR_Y;
import static mcjty.hologui.api.IGuiTile.TAG_MAIN;
import static mcjty.hologui.setup.Registration.HOLOGUI_ENTITY_TYPE;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class RecyclerTests {

    @GameTest(template = "empty")
    public static void everyConstructorRecipeHasDeterministicLoss(GameTestHelper helper) {
        for (ConstructorRecipe recipe : BlueprintRecipeRegistry.getRecipes()) {
            helper.assertTrue(Recycling.findRecipe(recipe.getDestination()) == recipe, "Every Constructor product is recyclable");
            List<ItemStack> recovered = Recycling.recover(recipe);
            List<ItemStack> again = Recycling.recover(recipe);
            int ingredients = recipe.getIngredientList().stream().mapToInt(ItemStack::getCount).sum();
            int total = recovered.stream().mapToInt(ItemStack::getCount).sum();
            helper.assertTrue(total == ingredients * 3 / 4 && total < ingredients, "Recovery always loses materials: " + recipe.getId());
            helper.assertTrue(recovered.size() == again.size(), "Recovery is deterministic");
            for (int index = 0; index < recovered.size(); index++) {
                ItemStack stack = recovered.get(index);
                helper.assertTrue(ItemStack.matches(stack, again.get(index)) && stack.getCount() <= stack.getMaxStackSize(),
                        "Recovery preserves deterministic, valid stacks");
            }
        }
        ConstructorRecipe repeated = new ConstructorRecipe(new ItemStack(Items.DIAMOND), 1,
                new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_INGOT));
        List<ItemStack> recovered = Recycling.recover(repeated);
        helper.assertTrue(recovered.size() == 1 && recovered.get(0).getCount() == 3, "Repeated ingredients merge before rounding");
        ItemStack weapon = new ItemStack(Registration.ENERGY_SABRE.get());
        weapon.getOrCreateTag().putLong("energy", 123);
        weapon.setDamageValue(1);
        helper.assertTrue(Recycling.findRecipe(weapon) != null, "Used and tagged weapons do not need a pristine blueprint match");
        helper.assertTrue(Recycling.findRecipe(new ItemStack(Items.DIRT)) == null, "Non-Constructor products are rejected");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void lootOnlyModulesRecycleWithoutBecomingCraftable(GameTestHelper helper) {
        RecyclerTile tile = place(helper);
        supplyPower(helper, tile);
        IItemHandlerModifiable inventory = inventory(tile);
        Item[] modules = {Registration.MODULE_REGENERATION.get(), Registration.MODULE_SPEED.get(),
                Registration.MODULE_POWER.get(), Registration.MODULE_SCRAMBLE.get(), Registration.MODULE_STEPASSIST.get(),
                Registration.MODULE_HOVER.get(), Registration.MODULE_INVISIBILITY.get()};
        for (Item module : modules) {
            ItemStack input = new ItemStack(module, 2);
            input.getOrCreateTag().putBoolean("recycler_test", true);
            helper.assertTrue(BlueprintRecipeRegistry.findRecipe(input) == null,
                    "Recycling must not add a Constructor recipe for loot-only modules");
            helper.assertTrue(BlueprintRecipeRegistry.getRecipes().stream()
                            .noneMatch(recipe -> recipe.getDestination().is(module)),
                    "Loot-only modules must stay out of the random blueprint catalog");
            helper.assertTrue(Recycling.findRecipe(input) != null, "Every loot-only equipment module is recyclable");
            for (int slot = 0; slot <= 10; slot++) {
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
            }
            helper.assertTrue(inventory.insertItem(0, input, false).isEmpty(), "The real Recycler accepts loot-only modules");
            tick(tile, 40);
            helper.assertTrue(inventory.getStackInSlot(0).getCount() == 1 && outputCount(inventory) == 3,
                    "One module produces three ordinary components after one powered operation");
            for (Item component : new Item[]{Registration.CIRCUIT.get(), Registration.INGOT_SILVER.get(), Registration.DUST_SILICON.get()}) {
                int amount = 0;
                for (int slot = 1; slot <= 10; slot++) {
                    ItemStack output = inventory.getStackInSlot(slot);
                    if (output.is(component)) {
                        amount += output.getCount();
                    }
                }
                helper.assertTrue(amount == 1, "Each module returns one circuit, one silver ingot and one silicon");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void automationUsesBottomAndBackForEveryFacing(GameTestHelper helper) {
        RecyclerTile tile = place(helper);
        for (Direction front : Direction.Plane.HORIZONTAL) {
            helper.getLevel().setBlockAndUpdate(tile.getBlockPos(), tile.getBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, front));
            for (Direction side : Direction.values()) {
                IItemHandler handler = tile.getCapability(ForgeCapabilities.ITEM_HANDLER, side).orElseThrow(IllegalStateException::new);
                boolean output = side == Direction.DOWN || side == front.getOpposite();
                inventory(tile).setStackInSlot(0, ItemStack.EMPTY);
                inventory(tile).setStackInSlot(10, new ItemStack(Items.IRON_INGOT, 3));
                helper.assertTrue(handler.getSlots() == (output ? 10 : 1), "Every output is accessible on " + front + "/" + side);
                ItemStack input = new ItemStack(Registration.CONSTRUCTOR.get());
                helper.assertTrue(handler.insertItem(0, input, true).isEmpty() != output, "Only input faces allow insertion");
                helper.assertTrue(inventory(tile).getStackInSlot(0).isEmpty(), "Simulation cannot insert");
                helper.assertTrue(handler.insertItem(0, input, false).isEmpty() != output, "Real insertion follows the face");
                helper.assertTrue(handler.extractItem(output ? 9 : 0, 1, true).is(Items.IRON_INGOT) == output,
                        "Only output faces allow extraction, including the tenth slot");
                helper.assertTrue(inventory(tile).getStackInSlot(10).getCount() == 3, "Simulation cannot extract");
                helper.assertTrue(!handler.isItemValid(0, new ItemStack(Items.DIRT)), "Invalid inputs are rejected");
            }
        }
        helper.assertTrue(inventory(tile).extractItem(0, 1, false).isEmpty(), "Unsided automation protects input");
        var old = tile.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP);
        tile.invalidateCaps();
        helper.assertTrue(!old.isPresent(), "Sided handlers are invalidated on removal");
        tile.reviveCaps();
        helper.assertTrue(tile.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).isPresent(), "Revival creates fresh handlers");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fortyPoweredTicksSurviveReloadAndBlockedOutputs(GameTestHelper helper) {
        RecyclerTile tile = place(helper);
        IItemHandlerModifiable inventory = inventory(tile);
        inventory.setStackInSlot(0, new ItemStack(Registration.CONSTRUCTOR.get(), 2));
        tick(tile, 40);
        helper.assertTrue(inventory.getStackInSlot(0).getCount() == 2 && tile.getUsingPower() == 0, "Recycling requires both fluxes");
        supplyPower(helper, tile);
        tick(tile, 20);
        CompoundTag saved = tile.saveWithoutMetadata();
        tile.load(saved);
        helper.assertTrue(tile.saveWithoutMetadata().getCompound("Info").getInt("recycleProgress") == 20, "Progress survives reload");
        tick(tile, 19);
        helper.assertTrue(inventory.getStackInSlot(0).getCount() == 2 && inventory.getStackInSlot(1).isEmpty(), "No operation finishes early");
        tile.tickServer();
        int expected = Recycling.recover(Recycling.findRecipe(new ItemStack(Registration.CONSTRUCTOR.get())))
                .stream().mapToInt(ItemStack::getCount).sum();
        helper.assertTrue(inventory.getStackInSlot(0).getCount() == 1 && outputCount(inventory) == expected, "Tick 40 produces the full recovery atomically");
        CompoundTag client = new CompoundTag();
        tile.saveClientDataToNBT(client);
        RecyclerTile display = new RecyclerTile(tile.getBlockPos(), tile.getBlockState());
        display.loadClientDataFromNBT(client);
        for (int slot = 0; slot <= 10; slot++) {
            helper.assertTrue(ItemStack.matches(inventory(display).getStackInSlot(slot), inventory.getStackInSlot(slot)),
                    "Every inventory slot synchronizes to the client");
        }
        for (int slot = 1; slot <= 10; slot++) {
            inventory.setStackInSlot(slot, new ItemStack(Items.DIRT, 64));
        }
        tick(tile, 80);
        helper.assertTrue(inventory.getStackInSlot(0).getCount() == 1 && outputCount(inventory) == 640 && tile.getUsingPower() == 0,
                "Blocked outputs never consume input, replace products, or spend power");
        for (int slot = 1; slot <= 10; slot++) {
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
        }
        tick(tile, 40);
        helper.assertTrue(inventory.getStackInSlot(0).isEmpty() && outputCount(inventory) == expected, "Recycling resumes when output room returns");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void batchProductsNeverRefundAWholeRecipePerItem(GameTestHelper helper) {
        RecyclerTile tile = place(helper);
        supplyPower(helper, tile);
        IItemHandlerModifiable inventory = inventory(tile);
        ItemStack cables = new ItemStack(Registration.NETCABLE_NEGARITE.get(), 7);
        inventory.setStackInSlot(0, cables);
        tick(tile, 80);
        helper.assertTrue(inventory.getStackInSlot(0).getCount() == 7 && outputCount(inventory) == 0, "A partial batch cannot refund full recipe ingredients");
        inventory.setStackInSlot(0, new ItemStack(Registration.NETCABLE_NEGARITE.get(), 8));
        tick(tile, 40);
        int expected = Recycling.recover(Recycling.findRecipe(cables)).stream().mapToInt(ItemStack::getCount).sum();
        helper.assertTrue(inventory.getStackInSlot(0).isEmpty() && outputCount(inventory) == expected, "One complete batch refunds only 75% once");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hologramTransfersHotbarInputAndTenthOutputWithoutLosingRemainders(GameTestHelper helper) {
        RecyclerTile tile = place(helper);
        Player player = helper.makeMockSurvivalPlayer();
        player.setPos(Vec3.atCenterOf(tile.getBlockPos()));
        HoloGuiEntity holo = new HoloGuiEntity(HOLOGUI_ENTITY_TYPE.get(), helper.getLevel());
        holo.setGuiTile(tile.getBlockPos());
        holo.setTag(TAG_MAIN);
        var root = holo.getGui(player);
        player.getInventory().setItem(0, new ItemStack(Registration.CONSTRUCTOR.get(), 2));
        root.hit(player, holo, GRID_X + .2, HOTBAR_Y + .2);
        root.hit(player, holo, GRID_X + .2, HOTBAR_Y + .2);
        helper.assertTrue(player.getInventory().getItem(0).isEmpty() && inventory(tile).getStackInSlot(0).getCount() == 2, "Hotbar double-click inserts into the input");
        root.hit(player, holo, .6, 1.5);
        root.hit(player, holo, .6, 1.5);
        helper.assertTrue(inventory(tile).getStackInSlot(0).isEmpty(), "Input can be retrieved");
        inventory(tile).setStackInSlot(10, new ItemStack(Items.IRON_INGOT, 12));
        root.hit(player, holo, 5.6, 2.5);
        root.hit(player, holo, 5.6, 2.5);
        helper.assertTrue(inventory(tile).getStackInSlot(10).isEmpty(), "The tenth fixed output slot can be retrieved");
        for (int slot = 0; slot < 36; slot++) {
            player.getInventory().setItem(slot, new ItemStack(Items.DIRT, 64));
        }
        player.getInventory().setItem(0, new ItemStack(Items.IRON_INGOT, 60));
        inventory(tile).setStackInSlot(10, new ItemStack(Items.IRON_INGOT, 12));
        tile.transferToPlayer(player, 10);
        helper.assertTrue(player.getInventory().getItem(0).getCount() == 64 && inventory(tile).getStackInSlot(10).getCount() == 8,
                "Partial extraction keeps the remainder in output despite insertion restrictions");
        helper.succeed();
    }

    private static RecyclerTile place(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().setBlockAndUpdate(pos, Registration.RECYCLER.get().defaultBlockState());
        return (RecyclerTile) helper.getLevel().getBlockEntity(pos);
    }

    private static IItemHandlerModifiable inventory(RecyclerTile tile) {
        return (IItemHandlerModifiable) tile.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
    }

    private static int outputCount(IItemHandler inventory) {
        int total = 0;
        for (int slot = 1; slot <= 10; slot++) {
            total += inventory.getStackInSlot(slot).getCount();
        }
        return total;
    }

    private static void tick(RecyclerTile tile, int ticks) {
        for (int tick = 0; tick < ticks; tick++) {
            tile.tickServer();
        }
    }

    private static void supplyPower(GameTestHelper helper, RecyclerTile tile) {
        PowerSystem power = PowerSystem.getPowerSystem(helper.getLevel());
        for (PowerType type : PowerType.values()) {
            BlockPos pos = tile.getBlockPos().relative(type == PowerType.NEGARITE ? Direction.NORTH : Direction.SOUTH);
            helper.getLevel().setBlockAndUpdate(pos, (type == PowerType.NEGARITE ? Registration.NEGARITE_GENERATOR.get()
                    : Registration.POSIRITE_GENERATOR.get()).defaultBlockState());
            int id = power.allocateId();
            ((IPowerBlob) helper.getLevel().getBlockEntity(pos)).fillCableId(id);
            power.addPower(id, 10000, type);
        }
    }
}
