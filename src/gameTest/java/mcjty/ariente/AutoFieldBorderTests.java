package mcjty.ariente;

import mcjty.ariente.blocks.utility.autofield.AutoFieldTile;
import mcjty.ariente.blocks.utility.autofield.FieldMarkerTile;
import mcjty.ariente.blocks.utility.autofield.InputItemNodeTile;
import mcjty.ariente.blocks.utility.autofield.OutputItemNodeTile;
import mcjty.ariente.blocks.utility.autofield.RoundRobinNodeTile;
import mcjty.ariente.blocks.utility.autofield.SensorItemNodeTile;
import mcjty.ariente.multipart.MultipartTE;
import mcjty.ariente.power.IPowerBlob;
import mcjty.ariente.power.PowerSystem;
import mcjty.ariente.power.PowerType;
import mcjty.ariente.setup.Registration;
import mcjty.lib.multipart.PartSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import static mcjty.ariente.blocks.utility.autofield.AbstractNodeTile.ORIENTATION;
import static mcjty.ariente.blocks.utility.autofield.NodeOrientation.EAST_DN;
import static mcjty.ariente.blocks.utility.autofield.NodeOrientation.EAST_UN;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class AutoFieldBorderTests {

    @GameTest(template = "automation_field")
    public static void bottomLayerSupportsMachinesSensorsModifiersAndNodeChanges(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        AutoFieldTile field = rectangle(level, pos);
        field.tickServer(); // Cache the empty interior before adding any nodes.
        BlockPos outputPos = pos.offset(1, 0, 2);
        IItemHandler source = storage(level, outputPos.east());
        OutputItemNodeTile output = output(level, outputPos);
        output.getFilters()[0] = DyeColor.WHITE;
        BlockPos inputPos = pos.offset(3, 0, 2);
        IItemHandler first = storage(level, inputPos.east());
        input(level, inputPos);
        BlockPos secondInputPos = inputPos.south();
        IItemHandler second = storage(level, secondInputPos.east());
        InputItemNodeTile secondInput = input(level, secondInputPos);
        supplyPower(level, pos);
        put(source, Items.IRON_INGOT, 4);
        field.tickServer();
        helper.assertTrue(count(source, Items.IRON_INGOT) == 4, "A missing positive sensor must prevent transfers");

        SensorItemNodeTile sensor = new SensorItemNodeTile(outputPos,
                Registration.SENSOR_ITEM_NODE.get().defaultBlockState().setValue(ORIENTATION, EAST_UN));
        CompoundTag sensorData = sensor.saveWithoutMetadata();
        sensorData.getCompound("Info").putInt("op", 2); // Count > 0
        sensor.load(sensorData);
        addPart(level, outputPos, EAST_UN.getSlot(), sensor);
        RoundRobinNodeTile modifier = new RoundRobinNodeTile(outputPos,
                Registration.ROUND_ROBIN_NODE.get().defaultBlockState().setValue(ORIENTATION, EAST_DN));
        addPart(level, outputPos, EAST_DN.getBackSlot(), modifier);
        field.tickServer();
        helper.assertTrue(count(source, Items.IRON_INGOT) == 0
                        && count(first, Items.IRON_INGOT) == 2 && count(second, Items.IRON_INGOT) == 2,
                "Interior bottom-layer nodes must discover sensors and distribute items with a round-robin modifier");

        ((MultipartTE) level.getBlockEntity(secondInputPos)).removePart(secondInput.getBlockState());
        put(source, Items.IRON_INGOT, 2);
        field.tickServer();
        helper.assertTrue(count(first, Items.IRON_INGOT) == 4 && count(second, Items.IRON_INGOT) == 2,
                "Removing an interior input must refresh the cached destinations");

        output.getOutputFilter().set(0, new ItemStack(Items.GOLD_INGOT));
        InputItemNodeTile firstInput = (InputItemNodeTile) ((MultipartTE) level.getBlockEntity(inputPos))
                .getParts().get(EAST_DN.getSlot()).getBlockEntity();
        firstInput.getInputFilter().set(0, new ItemStack(Items.GOLD_INGOT));
        AutoFieldTile.notifyField(level, inputPos);
        AutoFieldTile.notifyField(level, outputPos);
        put(source, Items.GOLD_INGOT, 1);
        field.tickServer();
        helper.assertTrue(count(first, Items.GOLD_INGOT) == 1, "Interior filter changes must reach their controller without markers below");
        helper.succeed();
    }

    @GameTest(template = "automation_field")
    public static void openingAndClosingBorderRefreshesInteriorCoverage(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        AutoFieldTile field = rectangle(level, pos);
        IItemHandler source = storage(level, pos.offset(2, 0, 2));
        IItemHandler destination = storage(level, pos.offset(4, 0, 2));
        output(level, pos.offset(1, 0, 2));
        input(level, pos.offset(3, 0, 2));
        supplyPower(level, pos);
        put(source, Items.IRON_INGOT, 1);
        field.tickServer();
        helper.assertTrue(count(destination, Items.IRON_INGOT) == 1, "A closed border must cover the interior");

        BlockPos gap = pos.east(3);
        level.removeBlock(gap, false);
        put(source, Items.IRON_INGOT, 1);
        field.tickServer();
        helper.assertTrue(count(source, Items.IRON_INGOT) == 1, "Opening the border must remove interior coverage");
        level.setBlockAndUpdate(gap, Blocks.STONE.defaultBlockState());
        field.setHeight(field.getHeight());
        field.tickServer();
        helper.assertTrue(count(source, Items.IRON_INGOT) == 1, "Ordinary blocks must not close the marker border");
        marker(level, gap);
        field.tickServer();
        helper.assertTrue(count(source, Items.IRON_INGOT) == 0 && count(destination, Items.IRON_INGOT) == 2,
                "Replacing the missing marker must restore interior transfers immediately");
        helper.succeed();
    }

    @GameTest(template = "automation_field")
    public static void concaveBorderExcludesOutsideNodesAndRespectsHeight(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        AutoFieldTile field = controller(level, pos);
        int[][] corners = {{0, 0}, {6, 0}, {6, 2}, {3, 2}, {3, 5}, {0, 5}, {0, 0}};
        for (int i = 0; i < corners.length - 1; i++) {
            int x = corners[i][0];
            int z = corners[i][1];
            int dx = Integer.signum(corners[i + 1][0] - x);
            int dz = Integer.signum(corners[i + 1][1] - z);
            while (x != corners[i + 1][0] || z != corners[i + 1][1]) {
                BlockPos markerPos = pos.offset(x, 0, z);
                if (!markerPos.equals(pos)) {
                    marker(level, markerPos);
                }
                x += dx;
                z += dz;
            }
        }
        IItemHandler source = storage(level, pos.offset(2, 0, 1));
        output(level, pos.offset(1, 0, 1));
        IItemHandler inside = storage(level, pos.offset(2, 1, 4));
        input(level, pos.offset(1, 1, 4));
        IItemHandler outside = storage(level, pos.offset(5, 0, 4));
        input(level, pos.offset(4, 0, 4));
        IItemHandler tooHigh = storage(level, pos.offset(2, 2, 3));
        input(level, pos.offset(1, 2, 3));
        supplyPower(level, pos);
        put(source, Items.IRON_INGOT, 1);
        field.tickServer();
        helper.assertTrue(count(source, Items.IRON_INGOT) == 0 && count(inside, Items.IRON_INGOT) == 1
                        && count(outside, Items.IRON_INGOT) == 0 && count(tooHigh, Items.IRON_INGOT) == 0,
                "Only enclosed columns from the base through the configured height must participate");

        field.setHeight(2);
        ((MultipartTE) level.getBlockEntity(pos.offset(1, 1, 4))).removePart(
                Registration.INPUT_ITEM_NODE.get().defaultBlockState().setValue(ORIENTATION, EAST_DN));
        put(source, Items.IRON_INGOT, 1);
        field.tickServer();
        helper.assertTrue(count(tooHigh, Items.IRON_INGOT) == 1 && count(outside, Items.IRON_INGOT) == 0,
                "Increasing height must include the new top layer while keeping the concave exterior excluded");
        helper.succeed();
    }

    private static AutoFieldTile rectangle(ServerLevel level, BlockPos pos) {
        AutoFieldTile field = controller(level, pos);
        field.getFieldBox(); // The first marker must also refresh an already cached, empty controller.
        for (int x = 0; x <= 6; x++) {
            for (int z = 0; z <= 4; z++) {
                if ((x == 0 || x == 6 || z == 0 || z == 4) && (x != 0 || z != 0)) {
                    marker(level, pos.offset(x, 0, z));
                }
            }
        }
        return field;
    }

    private static AutoFieldTile controller(ServerLevel level, BlockPos pos) {
        level.setBlockAndUpdate(pos, Registration.AUTOMATION_FIELD.get().defaultBlockState());
        AutoFieldTile field = (AutoFieldTile) level.getBlockEntity(pos);
        field.onLoad();
        return field;
    }

    private static void marker(ServerLevel level, BlockPos pos) {
        addPart(level, pos, PartSlot.DOWN, new FieldMarkerTile(pos, Registration.FIELD_MARKER.get().defaultBlockState()));
    }

    private static OutputItemNodeTile output(ServerLevel level, BlockPos pos) {
        OutputItemNodeTile node = new OutputItemNodeTile(pos,
                Registration.OUTPUT_ITEM_NODE.get().defaultBlockState().setValue(ORIENTATION, EAST_DN));
        node.getOutputFilter().set(0, new ItemStack(Items.IRON_INGOT));
        addPart(level, pos, EAST_DN.getSlot(), node);
        return node;
    }

    private static InputItemNodeTile input(ServerLevel level, BlockPos pos) {
        InputItemNodeTile node = new InputItemNodeTile(pos,
                Registration.INPUT_ITEM_NODE.get().defaultBlockState().setValue(ORIENTATION, EAST_DN));
        node.getInputFilter().set(0, new ItemStack(Items.IRON_INGOT));
        addPart(level, pos, EAST_DN.getSlot(), node);
        return node;
    }

    private static void addPart(ServerLevel level, BlockPos pos, PartSlot slot, BlockEntity part) {
        if (!(level.getBlockEntity(pos) instanceof MultipartTE)) {
            level.setBlock(pos, Registration.MULTIPART_BLOCK.get().defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        ((MultipartTE) level.getBlockEntity(pos)).addPart(slot, part.getBlockState(), part);
    }

    private static IItemHandler storage(ServerLevel level, BlockPos pos) {
        level.setBlockAndUpdate(pos, Registration.STORAGE.get().defaultBlockState());
        return level.getBlockEntity(pos).getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.WEST).orElseThrow(IllegalStateException::new);
    }

    private static void put(IItemHandler inventory, Item item, int amount) {
        if (!ItemHandlerHelper.insertItem(inventory, new ItemStack(item, amount), false).isEmpty()) {
            throw new IllegalStateException("Test source is full");
        }
    }

    private static int count(IItemHandler inventory, Item item) {
        int count = 0;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void supplyPower(ServerLevel level, BlockPos pos) {
        BlockPos negaritePos = pos.north();
        BlockPos posiritePos = pos.west();
        level.setBlockAndUpdate(negaritePos, Registration.NEGARITE_GENERATOR.get().defaultBlockState());
        level.setBlockAndUpdate(posiritePos, Registration.POSIRITE_GENERATOR.get().defaultBlockState());
        PowerSystem power = PowerSystem.getPowerSystem(level);
        int negarite = power.allocateId();
        int posirite = power.allocateId();
        ((IPowerBlob) level.getBlockEntity(negaritePos)).fillCableId(negarite);
        ((IPowerBlob) level.getBlockEntity(posiritePos)).fillCableId(posirite);
        power.addPower(negarite, 10000, PowerType.NEGARITE);
        power.addPower(posirite, 10000, PowerType.POSIRITE);
    }
}
