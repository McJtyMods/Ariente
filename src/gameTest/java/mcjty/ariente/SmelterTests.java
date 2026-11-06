package mcjty.ariente;

import mcjty.ariente.blocks.utility.SmelterTile;
import mcjty.ariente.config.PowerConfiguration;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.power.IPowerBlob;
import mcjty.ariente.power.PowerSystem;
import mcjty.ariente.power.PowerType;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import static mcjty.ariente.blocks.BlockProperties.WORKING;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class SmelterTests {

    @GameTest(template = "empty")
    public static void sidedAutomationFollowsEveryHorizontalFacing(GameTestHelper helper) {
        SmelterTile tile = place(helper);
        for (Direction front : Direction.Plane.HORIZONTAL) {
            helper.getLevel().setBlockAndUpdate(tile.getBlockPos(), tile.getBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, front));
            for (Direction side : Direction.values()) {
                IItemHandler handler = handler(tile, side);
                inventory(tile).setStackInSlot(SmelterTile.SLOT_INPUT, ItemStack.EMPTY);
                inventory(tile).setStackInSlot(SmelterTile.SLOT_OUTPUT, new ItemStack(Items.IRON_INGOT, 3));
                boolean output = side == Direction.DOWN || side == front.getOpposite();
                ItemStack raw = new ItemStack(Items.RAW_IRON, 2);
                helper.assertTrue(handler.insertItem(0, raw, true).isEmpty() != output, "Insertion simulation follows " + front + "/" + side);
                helper.assertTrue(tile.getStack(SmelterTile.SLOT_INPUT).isEmpty(), "Simulation must not insert items");
                helper.assertTrue(handler.insertItem(0, raw, false).isEmpty() != output, "Only input faces accept ore");
                helper.assertTrue(!handler.isItemValid(0, new ItemStack(Items.STICK)), "Non-smeltable items must be rejected");
                helper.assertTrue(handler.extractItem(0, 1, true).is(Items.IRON_INGOT) == output, "Only output faces expose products");
                helper.assertTrue(tile.getStack(SmelterTile.SLOT_OUTPUT).getCount() == 3, "Simulation must not extract items");
                helper.assertTrue(handler.extractItem(0, 1, false).is(Items.IRON_INGOT) == output, "Input cannot be extracted by automation");
            }
        }
        IItemHandler unsided = tile.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
        helper.assertTrue(unsided.extractItem(SmelterTile.SLOT_INPUT, 1, false).isEmpty(), "Unsided automation must also protect input");
        helper.assertTrue(!unsided.insertItem(SmelterTile.SLOT_OUTPUT, new ItemStack(Items.IRON_INGOT), false).isEmpty(), "Unsided automation must protect output");
        var oldCapability = tile.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP);
        tile.invalidateCaps();
        helper.assertTrue(!oldCapability.isPresent(), "Removing the tile must invalidate its sided handlers");
        tile.reviveCaps();
        helper.assertTrue(tile.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).isPresent(), "Revived tiles must expose fresh handlers");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void furnaceRecipeUsesConfiguredSpeedAndPower(GameTestHelper helper) {
        try (var config = new ServerConfigScope()) {
            PowerConfiguration.SMELTER_SPEED.set(10);
            PowerConfiguration.SMELTER_POWER_PER_TICK.set(20);
            SmelterTile tile = place(helper);
            helper.assertTrue(!tile.getBlockState().getValue(WORKING), "A newly placed smelter must start with the working glow disabled");
            handler(tile, Direction.UP).insertItem(0, new ItemStack(Items.RAW_IRON, 2), false);
            tick(tile, 30);
            helper.assertTrue(tile.getStack(SmelterTile.SLOT_OUTPUT).isEmpty() && tile.getUsingPower() == 0, "Smelting requires both fluxes");
            helper.assertTrue(!tile.getBlockState().getValue(WORKING), "An unpowered smelter must not show the working glow");
            int[] networks = supplyPower(helper.getLevel(), tile.getBlockPos());
            tick(tile, 19);
            helper.assertTrue(tile.getStack(SmelterTile.SLOT_OUTPUT).isEmpty(), "At 10x speed a normal recipe must not complete before tick 20");
            helper.assertTrue(tile.getBlockState().getValue(WORKING), "Powered progress must enable the working glow");
            tile.tickServer();
            helper.assertTrue(tile.getStack(SmelterTile.SLOT_OUTPUT).is(Items.IRON_INGOT)
                    && tile.getStack(SmelterTile.SLOT_OUTPUT).getCount() == 1
                    && tile.getStack(SmelterTile.SLOT_INPUT).getCount() == 1, "Tick 20 consumes one input and produces one output");
            PowerSystem power = PowerSystem.getPowerSystem(helper.getLevel());
            helper.assertTrue(power.getTotalPower(networks[0], PowerType.NEGARITE) == 9600
                    && power.getTotalPower(networks[1], PowerType.POSIRITE) == 9600, "20 active ticks consume 400 of each flux");
            inventory(tile).setStackInSlot(SmelterTile.SLOT_OUTPUT, new ItemStack(Items.IRON_INGOT, 64));
            tick(tile, 30);
            helper.assertTrue(tile.getUsingPower() == 0 && tile.getStack(SmelterTile.SLOT_INPUT).getCount() == 1, "Full output pauses smelting");
            helper.assertTrue(!tile.getBlockState().getValue(WORKING), "Blocked output must disable the working glow");
            helper.assertTrue(power.getTotalPower(networks[0], PowerType.NEGARITE) == 9600, "A blocked smelter must not consume power");
            inventory(tile).setStackInSlot(SmelterTile.SLOT_OUTPUT, new ItemStack(Items.GOLD_INGOT));
            tick(tile, 30);
            helper.assertTrue(tile.getUsingPower() == 0 && tile.getStack(SmelterTile.SLOT_OUTPUT).is(Items.GOLD_INGOT), "Incompatible output must not be replaced");

            PowerConfiguration.SMELTER_SPEED.set(5);
            inventory(tile).setStackInSlot(SmelterTile.SLOT_OUTPUT, ItemStack.EMPTY);
            tick(tile, 39);
            helper.assertTrue(tile.getStack(SmelterTile.SLOT_OUTPUT).isEmpty(), "At 5x speed a normal recipe must not complete before tick 40");
            tile.tickServer();
            helper.assertTrue(tile.getStack(SmelterTile.SLOT_OUTPUT).is(Items.IRON_INGOT)
                    && tile.getStack(SmelterTile.SLOT_INPUT).isEmpty(), "Changing speed to 5x must take 40 powered ticks");
            helper.assertTrue(power.getTotalPower(networks[0], PowerType.NEGARITE) == 8800
                    && power.getTotalPower(networks[1], PowerType.POSIRITE) == 8800, "Per-tick power cost stays independent of speed");
            tile.tickServer();
            helper.assertTrue(!tile.getBlockState().getValue(WORKING), "Empty input must disable the working glow");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void progressAndBothSlotsSurviveReloadAndPowerLoss(GameTestHelper helper) {
        SmelterTile tile = place(helper);
        int speed = PowerConfiguration.SMELTER_SPEED.get();
        int cookingTicks = (200 + speed - 1) / speed;
        int firstTicks = cookingTicks / 2;
        int remainingTicks = cookingTicks - firstTicks;
        handler(tile, Direction.UP).insertItem(0, new ItemStack(Items.RAW_IRON, 2), false);
        supplyPower(helper.getLevel(), tile.getBlockPos());
        tick(tile, firstTicks);
        CompoundTag saved = tile.saveWithoutMetadata();
        tile.load(saved);
        helper.getLevel().removeBlock(tile.getBlockPos().north(), false);
        tick(tile, 30);
        helper.assertTrue(tile.saveWithoutMetadata().getCompound("Info").getInt("smeltProgress") == firstTicks * speed, "Unpowered progress must remain saved");
        helper.assertTrue(!tile.getBlockState().getValue(WORKING), "Losing power must disable the working glow");
        supplyPower(helper.getLevel(), tile.getBlockPos());
        tick(tile, remainingTicks);
        helper.assertTrue(tile.getStack(SmelterTile.SLOT_OUTPUT).is(Items.IRON_INGOT), "Reloaded progress completes after the remaining powered ticks");
        CompoundTag client = new CompoundTag();
        tile.saveClientDataToNBT(client);
        SmelterTile display = new SmelterTile(tile.getBlockPos(), tile.getBlockState());
        display.loadClientDataFromNBT(client);
        helper.assertTrue(ItemStack.matches(display.getStack(0), tile.getStack(0))
                && ItemStack.matches(display.getStack(1), tile.getStack(1)), "Both front slot stacks must synchronize to the client");
        tile.load(tile.saveWithoutMetadata());
        helper.assertTrue(tile.getStack(0).is(Items.RAW_IRON) && tile.getStack(1).is(Items.IRON_INGOT), "Both inventory slots must survive NBT loading");
        tick(tile, firstTicks);
        inventory(tile).setStackInSlot(SmelterTile.SLOT_INPUT, new ItemStack(Items.RAW_GOLD));
        inventory(tile).setStackInSlot(SmelterTile.SLOT_OUTPUT, ItemStack.EMPTY);
        tick(tile, cookingTicks - 1);
        helper.assertTrue(tile.getStack(1).isEmpty(), "A different recipe must not inherit the old progress");
        tick(tile, 1);
        helper.assertTrue(tile.getStack(1).is(Items.GOLD_INGOT), "The new recipe must complete after its own configured cooking time");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void frontSlotsWorkWithoutGuiAndBlueprintResolves(GameTestHelper helper) {
        SmelterTile tile = place(helper);
        Player player = helper.makeMockSurvivalPlayer();
        for (Direction front : Direction.Plane.HORIZONTAL) {
            helper.getLevel().setBlockAndUpdate(tile.getBlockPos(), tile.getBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, front));
            Direction left = front.getClockWise();
            Vec3 center = Vec3.atCenterOf(tile.getBlockPos()).add(Vec3.atLowerCornerOf(front.getNormal()).scale(.5));
            BlockHitResult input = new BlockHitResult(center.add(Vec3.atLowerCornerOf(left.getNormal()).scale(4.25 / 16)), front, tile.getBlockPos(), false);
            BlockHitResult output = new BlockHitResult(center.subtract(Vec3.atLowerCornerOf(left.getNormal()).scale(4.25 / 16)), front, tile.getBlockPos(), false);
            helper.assertTrue(tile.getSlot(input) == SmelterTile.SLOT_INPUT && tile.getSlot(output) == SmelterTile.SLOT_OUTPUT, "Visible slots follow " + front);
            helper.assertTrue(tile.getSlot(new BlockHitResult(center, front, tile.getBlockPos(), false)) == -1, "Lightning is not a slot");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.RAW_IRON, 3));
            tile.onBlockActivated(tile.getBlockState(), player, InteractionHand.MAIN_HAND, input);
            helper.assertTrue(player.getMainHandItem().isEmpty() && tile.getStack(0).getCount() == 3, "Direct insertion must work without a GUI");
            tile.onBlockActivated(tile.getBlockState(), player, InteractionHand.MAIN_HAND, input);
            helper.assertTrue(tile.getStack(0).getCount() == 2, "Empty-hand interaction retrieves one item");
            inventory(tile).setStackInSlot(0, ItemStack.EMPTY);
        }
        ItemStack blueprint = BlueprintItem.makeBluePrint(new ItemStack(Registration.SMELTER.get()));
        helper.assertTrue(BlueprintRecipeRegistry.findRecipe(BlueprintItem.getDestination(blueprint)) != null, "A populated Smelter blueprint must resolve in the Constructor catalog");
        helper.succeed();
    }

    private static SmelterTile place(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().setBlockAndUpdate(pos, Registration.SMELTER.get().defaultBlockState());
        return (SmelterTile) helper.getLevel().getBlockEntity(pos);
    }

    private static IItemHandler handler(SmelterTile tile, Direction side) {
        return tile.getCapability(ForgeCapabilities.ITEM_HANDLER, side).orElseThrow(IllegalStateException::new);
    }

    private static IItemHandlerModifiable inventory(SmelterTile tile) {
        return (IItemHandlerModifiable) tile.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
    }

    private static void tick(SmelterTile tile, int ticks) {
        for (int tick = 0; tick < ticks; tick++) {
            tile.tickServer();
        }
    }

    private static int[] supplyPower(ServerLevel level, BlockPos pos) {
        level.setBlockAndUpdate(pos.north(), Registration.NEGARITE_GENERATOR.get().defaultBlockState());
        level.setBlockAndUpdate(pos.south(), Registration.POSIRITE_GENERATOR.get().defaultBlockState());
        PowerSystem power = PowerSystem.getPowerSystem(level);
        int negarite = power.allocateId();
        int posirite = power.allocateId();
        ((IPowerBlob) level.getBlockEntity(pos.north())).fillCableId(negarite);
        ((IPowerBlob) level.getBlockEntity(pos.south())).fillCableId(posirite);
        power.addPower(negarite, 10000, PowerType.NEGARITE);
        power.addPower(posirite, 10000, PowerType.POSIRITE);
        return new int[]{negarite, posirite};
    }
}
