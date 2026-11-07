package mcjty.ariente;

import mcjty.ariente.blocks.utility.AutoConstructorTile;
import mcjty.ariente.blocks.utility.BlueprintStorageTile;
import mcjty.ariente.blocks.utility.autofield.AbstractItemNodeTile;
import mcjty.ariente.blocks.utility.autofield.AutoFieldTile;
import mcjty.ariente.blocks.utility.autofield.ConsumerInfo;
import mcjty.ariente.blocks.utility.autofield.FieldMarkerTile;
import mcjty.ariente.blocks.utility.autofield.InputItemNodeTile;
import mcjty.ariente.blocks.utility.autofield.ItemFilterCategory;
import mcjty.ariente.blocks.utility.autofield.OutputItemNodeTile;
import mcjty.ariente.blocks.utility.autofield.SensorItemNodeTile;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.multipart.MultipartTE;
import mcjty.ariente.power.IPowerBlob;
import mcjty.ariente.power.PowerSystem;
import mcjty.ariente.power.PowerType;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.gui.HoloGuiEntity;
import mcjty.lib.multipart.PartPos;
import mcjty.lib.multipart.PartSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;

import static mcjty.ariente.blocks.utility.autofield.AbstractNodeTile.ORIENTATION;
import static mcjty.ariente.blocks.utility.autofield.NodeOrientation.UP_NE;
import static mcjty.ariente.blocks.utility.autofield.NodeOrientation.UP_NW;
import static mcjty.hologui.api.IGuiTile.TAG_MAIN;
import static mcjty.hologui.setup.Registration.HOLOGUI_ENTITY_TYPE;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class AutoFieldInventoryTests {

    @GameTest(template = "empty")
    public static void inventoriesExposeEveryFaceAndPreserveSavedItems(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        List<Block> blocks = List.of(Registration.STORAGE.get(), Registration.NEGARITE_GENERATOR.get(),
                Registration.POSIRITE_GENERATOR.get(), Registration.AUTO_CONSTRUCTOR.get(), Registration.BLUEPRINT_STORAGE.get());
        List<ItemStack> contents = List.of(new ItemStack(Items.IRON_INGOT), new ItemStack(Registration.DUST_NEGARITE.get()),
                new ItemStack(Registration.DUST_POSIRITE.get()), new ItemStack(Items.GOLD_INGOT), blueprint());
        for (int i = 0; i < blocks.size(); i++) {
            BlockState state = blocks.get(i).defaultBlockState();
            level.setBlockAndUpdate(pos, state);
            BlockEntity tile = level.getBlockEntity(pos);
            IItemHandler inventory = handler(tile, Direction.NORTH);
            ItemStack item = contents.get(i);
            helper.assertTrue(ItemHandlerHelper.insertItem(inventory, item.copy(), false).isEmpty(),
                    "Sided automation must insert into " + blocks.get(i));
            for (Direction side : Direction.values()) {
                helper.assertTrue(count(handler(tile, side), item) == 1, "Every face must expose the same inventory");
            }
            helper.assertTrue(count(handler(tile, null), item) == 1, "Unsided access must expose the same inventory");

            CompoundTag saved = tile.saveWithoutMetadata();
            BlockEntity restored = tile.getType().create(pos, state);
            restored.setLevel(level);
            restored.load(saved);
            helper.assertTrue(count(handler(restored, Direction.SOUTH), item) == 1,
                    "Inventory contents and item data must survive saving and loading for " + blocks.get(i));
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void blueprintAndConstructorAutomationRespectSlotRules(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        level.setBlockAndUpdate(pos, Registration.BLUEPRINT_STORAGE.get().defaultBlockState());
        IItemHandler blueprints = handler(level.getBlockEntity(pos), Direction.WEST);
        for (int slot = 0; slot < BlueprintStorageTile.BLUEPRINTS; slot++) {
            helper.assertTrue(!blueprints.insertItem(slot, new ItemStack(Items.STONE), false).isEmpty(),
                    "Blueprint storage must reject ordinary items in every slot");
            helper.assertTrue(blueprints.insertItem(slot, blueprint(), false).isEmpty(),
                    "Automation must insert blueprints into every storage slot");
            helper.assertTrue(ItemStack.isSameItemSameTags(blueprints.extractItem(slot, 1, false), blueprint()),
                    "Automation must extract blueprints with their recipe data intact");
        }

        level.setBlockAndUpdate(pos, Registration.AUTO_CONSTRUCTOR.get().defaultBlockState());
        IItemHandler constructor = handler(level.getBlockEntity(pos), Direction.EAST);
        for (int slot = AutoConstructorTile.SLOT_INGREDIENTS; slot < AutoConstructorTile.SLOT_OUTPUT; slot++) {
            helper.assertTrue(!constructor.insertItem(slot, blueprint(), false).isEmpty(),
                    "Constructor ingredient slots must reject blueprints");
            helper.assertTrue(constructor.insertItem(slot, new ItemStack(Items.IRON_INGOT), false).isEmpty(),
                    "Automation must insert ingredients into every input slot");
            helper.assertTrue(constructor.extractItem(slot, 1, false).isEmpty(),
                    "Automation must not extract constructor ingredients");
        }
        for (int slot = AutoConstructorTile.SLOT_OUTPUT; slot < constructor.getSlots(); slot++) {
            helper.assertTrue(!constructor.insertItem(slot, new ItemStack(Items.GOLD_INGOT), false).isEmpty(),
                    "Automation must not insert into constructor output slots");
            ((IItemHandlerModifiable) constructor).setStackInSlot(slot, new ItemStack(Items.GOLD_INGOT));
            helper.assertTrue(constructor.extractItem(slot, 1, false).is(Items.GOLD_INGOT),
                    "Automation must extract products from every output slot");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fieldTransfersBlueprintsBothWaysAndSensorsReadStorage(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(BlockPos.ZERO).above(2);
        level.setBlockAndUpdate(pos, Registration.AUTOMATION_FIELD.get().defaultBlockState());
        AutoFieldTile field = (AutoFieldTile) level.getBlockEntity(pos);
        BlockPos sourcePos = pos.east().above(2);
        BlockPos destinationPos = pos.east(3).above(2);
        level.setBlockAndUpdate(sourcePos, Registration.STORAGE.get().defaultBlockState());
        level.setBlockAndUpdate(destinationPos, Registration.BLUEPRINT_STORAGE.get().defaultBlockState());
        for (int x = 1; x <= 3; x++) {
            BlockPos markerPos = pos.east(x);
            addPart(level, markerPos, PartSlot.DOWN, new FieldMarkerTile(markerPos, Registration.FIELD_MARKER.get().defaultBlockState()));
        }
        BlockPos outputPos = sourcePos.below();
        BlockPos inputPos = destinationPos.below();
        OutputItemNodeTile output = new OutputItemNodeTile(outputPos, Registration.OUTPUT_ITEM_NODE.get().defaultBlockState().setValue(ORIENTATION, UP_NE));
        output.getOutputFilter().set(0, blueprint());
        addPart(level, outputPos, UP_NE.getSlot(), output);
        InputItemNodeTile input = new InputItemNodeTile(inputPos, Registration.INPUT_ITEM_NODE.get().defaultBlockState().setValue(ORIENTATION, UP_NE));
        input.getInputFilter().set(0, blueprint());
        addPart(level, inputPos, UP_NE.getSlot(), input);
        SensorItemNodeTile sensor = new SensorItemNodeTile(inputPos, Registration.SENSOR_ITEM_NODE.get().defaultBlockState().setValue(ORIENTATION, UP_NW));
        CompoundTag sensorData = new CompoundTag();
        CompoundTag info = new CompoundTag();
        info.putInt("op", 2); // Count > 0
        sensorData.put("Info", info);
        sensor.load(sensorData);
        addPart(level, inputPos, UP_NW.getSlot(), sensor);
        IItemHandler source = handler(level.getBlockEntity(sourcePos), Direction.DOWN);
        IItemHandler destination = handler(level.getBlockEntity(destinationPos), Direction.DOWN);
        helper.assertTrue(ItemHandlerHelper.insertItem(source, blueprint(), false).isEmpty(), "The source must contain a blueprint");

        supplyPower(level, pos);
        field.tickServer();
        helper.assertTrue(count(source, blueprint()) == 0 && count(destination, blueprint()) == 1,
                "A powered field must transfer a blueprint from Ariente storage to blueprint storage");
        PartPos sensorPos = PartPos.create(inputPos, UP_NW.getSlot());
        helper.assertTrue(sensor.sense(sensorPos), "The sensor must detect the blueprint in Ariente blueprint storage");

        ((MultipartTE) level.getBlockEntity(outputPos)).removePart(output.getBlockState());
        ((MultipartTE) level.getBlockEntity(inputPos)).removePart(input.getBlockState());
        OutputItemNodeTile reverseOutput = new OutputItemNodeTile(inputPos, output.getBlockState());
        reverseOutput.getOutputFilter().set(0, blueprint());
        addPart(level, inputPos, UP_NE.getSlot(), reverseOutput);
        InputItemNodeTile reverseInput = new InputItemNodeTile(outputPos, input.getBlockState());
        reverseInput.getInputFilter().set(0, blueprint());
        addPart(level, outputPos, UP_NE.getSlot(), reverseInput);
        field.tickServer();
        helper.assertTrue(count(source, blueprint()) == 1 && count(destination, blueprint()) == 0,
                "The field must also extract blueprints and return them to Ariente storage");
        helper.assertTrue(!sensor.sense(sensorPos), "The sensor must detect that blueprint storage is empty again");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void whitelistAndBlacklistModesFilterBothEndsIncludingEmptyLists(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(BlockPos.ZERO).above(2);
        level.setBlockAndUpdate(pos, Registration.AUTOMATION_FIELD.get().defaultBlockState());
        AutoFieldTile field = (AutoFieldTile) level.getBlockEntity(pos);
        BlockPos sourcePos = pos.east().above(2);
        BlockPos destinationPos = pos.east(3).above(2);
        level.setBlockAndUpdate(sourcePos, Registration.STORAGE.get().defaultBlockState());
        level.setBlockAndUpdate(destinationPos, Registration.STORAGE.get().defaultBlockState());
        for (int x = 1; x <= 3; x++) {
            BlockPos markerPos = pos.east(x);
            addPart(level, markerPos, PartSlot.DOWN, new FieldMarkerTile(markerPos, Registration.FIELD_MARKER.get().defaultBlockState()));
        }
        OutputItemNodeTile output = new OutputItemNodeTile(sourcePos.below(), Registration.OUTPUT_ITEM_NODE.get().defaultBlockState().setValue(ORIENTATION, UP_NE));
        InputItemNodeTile input = new InputItemNodeTile(destinationPos.below(), Registration.INPUT_ITEM_NODE.get().defaultBlockState().setValue(ORIENTATION, UP_NE));
        addPart(level, output.getBlockPos(), UP_NE.getSlot(), output);
        addPart(level, input.getBlockPos(), UP_NE.getSlot(), input);
        IItemHandlerModifiable source = (IItemHandlerModifiable) handler(level.getBlockEntity(sourcePos), Direction.DOWN);
        IItemHandlerModifiable destination = (IItemHandlerModifiable) handler(level.getBlockEntity(destinationPos), Direction.DOWN);
        Player player = helper.makeMockPlayer();
        List<ItemStack> items = List.of(new ItemStack(Items.IRON_INGOT), new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.DIAMOND));

        for (boolean emptyOutput : new boolean[] {false, true}) {
            for (boolean emptyInput : new boolean[] {false, true}) {
                output.getOutputFilter().clear();
                input.getInputFilter().clear();
                if (!emptyOutput) {
                    output.getOutputFilter().set(0, items.get(0).copy());
                    output.getOutputFilter().set(1, items.get(1).copy());
                }
                if (!emptyInput) {
                    input.getInputFilter().set(0, items.get(0).copy());
                    input.getInputFilter().set(1, items.get(2).copy());
                }
                AutoFieldTile.notifyField(level, output.getBlockPos());
                for (boolean outputBlacklist : new boolean[] {false, true}) {
                    for (boolean inputBlacklist : new boolean[] {false, true}) {
                        setFilterMode(level, player, output, outputBlacklist);
                        setFilterMode(level, player, input, inputBlacklist);
                        for (int slot = 0; slot < source.getSlots(); slot++) {
                            source.setStackInSlot(slot, ItemStack.EMPTY);
                        }
                        for (int slot = 0; slot < destination.getSlots(); slot++) {
                            destination.setStackInSlot(slot, ItemStack.EMPTY);
                        }
                        items.forEach(item -> ItemHandlerHelper.insertItem(source, item.copy(), false));
                        supplyPower(level, pos);
                        field.tickServer();
                        for (int i = 0; i < items.size(); i++) {
                            boolean provided = outputBlacklist != (!emptyOutput && i != 2);
                            boolean wanted = inputBlacklist != (!emptyInput && i != 1);
                            int expected = provided && wanted ? 1 : 0;
                            helper.assertTrue(count(destination, items.get(i)) == expected && count(source, items.get(i)) == 1 - expected,
                                    "Both node modes must filter all samples, preserve excluded items and handle empty lists: "
                                            + outputBlacklist + "/" + inputBlacklist + "/" + emptyOutput + "/" + emptyInput + "/" + i);
                        }
                    }
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void processingCategoriesPartitionItemsAndRetainSampleExclusions(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.assertTrue(ItemFilterCategory.SMELTABLE.matches(level, new ItemStack(Items.RAW_IRON))
                        && ItemFilterCategory.SMELTABLE.matches(level, new ItemStack(Items.CHICKEN))
                        && ItemFilterCategory.SMELTABLE.matches(level, new ItemStack(Items.OAK_LOG)),
                "Furnace recipes must include raw metals, food and charcoal inputs");
        helper.assertTrue(ItemFilterCategory.WOOD.matches(level, new ItemStack(Items.OAK_PLANKS))
                        && ItemFilterCategory.WOOD.matches(level, new ItemStack(Items.CRIMSON_STEM))
                        && ItemFilterCategory.INGOTS.matches(level, new ItemStack(Registration.INGOT_MANGANESE.get()))
                        && ItemFilterCategory.OTHER.matches(level, new ItemStack(Items.DIAMOND)),
                "Material groups must include non-smeltable wood, modded ingots and miscellaneous items");
        for (var item : ForgeRegistries.ITEMS) {
            ItemStack stack = new ItemStack(item);
            if (!stack.isEmpty()) {
                int destinations = 0;
                for (ItemFilterCategory category : List.of(ItemFilterCategory.SMELTABLE, ItemFilterCategory.WOOD,
                        ItemFilterCategory.INGOTS, ItemFilterCategory.OTHER)) {
                    if (category.matches(level, stack)) {
                        destinations++;
                    }
                }
                helper.assertTrue(destinations == 1, "Each item must have exactly one processing destination: " + item);
            }
        }

        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        InputItemNodeTile input = new InputItemNodeTile(pos, Registration.INPUT_ITEM_NODE.get().defaultBlockState().setValue(ORIENTATION, UP_NE));
        addPart(level, pos, UP_NE.getSlot(), input);
        Set<PartPos> nodes = Set.of(PartPos.create(pos, UP_NE.getSlot()));
        CompoundTag tag = input.saveWithoutMetadata();
        tag.getCompound("Info").putBoolean("inBlacklist", true);
        input.load(tag);
        input.getInputFilter().set(0, new ItemStack(Registration.DUST_NEGARITE.get()));
        input.getInputFilter().set(1, new ItemStack(Registration.DUST_POSIRITE.get()));
        input.setInputCategory(ItemFilterCategory.OTHER);
        input.load(input.saveWithoutMetadata());
        ConsumerInfo consumers = new ConsumerInfo(level, nodes);
        helper.assertTrue(input.getInputCategory() == ItemFilterCategory.OTHER
                        && consumers.getWantedStream(new ItemStack(Items.DIAMOND)).count() == 1
                        && consumers.getWantedStream(new ItemStack(Items.RAW_IRON)).count() == 0
                        && consumers.getWantedStream(new ItemStack(Registration.DUST_NEGARITE.get())).count() == 0
                        && consumers.getWantedStream(new ItemStack(Registration.DUST_POSIRITE.get())).count() == 0,
                "Saved categories and sample blacklists must prevent smeltables and fuel from entering other storage");
        input.setInputCategory(ItemFilterCategory.PURIFIABLE);
        input.load(input.saveWithoutMetadata());
        helper.assertTrue(input.getInputCategory() == ItemFilterCategory.PURIFIABLE
                        && new ConsumerInfo(level, nodes).getWantedStream(new ItemStack(Items.IRON_ORE)).count() == 1
                        && new ConsumerInfo(level, nodes).getWantedStream(new ItemStack(Items.RAW_IRON)).count() == 0,
                "A saved purifier route accepts ore blocks and leaves their raw products for the smelter");
        input.setInputCategory(ItemFilterCategory.DIRECT_SMELTABLE);
        input.load(input.saveWithoutMetadata());
        consumers = new ConsumerInfo(level, nodes);
        helper.assertTrue(input.getInputCategory() == ItemFilterCategory.DIRECT_SMELTABLE
                        && consumers.getWantedStream(new ItemStack(Items.IRON_ORE)).count() == 0
                        && consumers.getWantedStream(new ItemStack(Items.RAW_IRON)).count() == 1
                        && consumers.getWantedStream(new ItemStack(Registration.ORE_NEGARITE.get())).count() == 1
                        && ItemFilterCategory.SMELTABLE.matches(level, new ItemStack(Items.IRON_ORE)),
                "Direct smelting must avoid purifier competition, accept raw metals/fuel ores and preserve existing smelter-only setups");
        input.setInputCategory(ItemFilterCategory.OTHER);
        tag = input.saveWithoutMetadata();
        tag.getCompound("Info").putBoolean("inBlacklist", false);
        input.load(tag);
        helper.assertTrue(new ConsumerInfo(level, nodes).getWantedStream(new ItemStack(Items.DIAMOND)).count() == 0,
                "A category must not bypass an empty or nonmatching whitelist");
        tag.getCompound("Info").remove("inCategory");
        input.load(tag);
        helper.assertTrue(input.getInputCategory() == ItemFilterCategory.ALL, "Existing saves must retain unrestricted categories");
        helper.succeed();
    }

    private static void setFilterMode(ServerLevel level, Player player, AbstractItemNodeTile node, boolean blacklist) {
        boolean current = node instanceof InputItemNodeTile input ? input.isInputBlacklist() : ((OutputItemNodeTile) node).isOutputBlacklist();
        if (current != blacklist) {
            HoloGuiEntity holo = new HoloGuiEntity(HOLOGUI_ENTITY_TYPE.get(), level);
            holo.setGuiTile(node.getBlockPos());
            holo.setTag(UP_NE.getSlot().name() + ":" + TAG_MAIN);
            var root = holo.getGui(player);
            IGuiComponent<?> option = root.findChild("filter_mode").orElseThrow();
            root.hit(player, holo, option.getX() + option.getW() / 2, option.getY() + option.getH() / 2);
        }
    }

    private static ItemStack blueprint() {
        return BlueprintItem.makeBluePrint(new ItemStack(Registration.FIELD_MARKER.get()));
    }

    private static IItemHandler handler(BlockEntity tile, Direction side) {
        return tile.getCapability(ForgeCapabilities.ITEM_HANDLER, side)
                .orElseThrow(() -> new IllegalStateException("Missing item handler for " + tile.getType() + " on " + side));
    }

    private static int count(IItemHandler inventory, ItemStack item) {
        int count = 0;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (ItemStack.isSameItemSameTags(stack, item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void addPart(ServerLevel level, BlockPos pos, PartSlot slot, BlockEntity part) {
        if (!(level.getBlockEntity(pos) instanceof MultipartTE)) {
            level.setBlock(pos, Registration.MULTIPART_BLOCK.get().defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        ((MultipartTE) level.getBlockEntity(pos)).addPart(slot, part.getBlockState(), part);
    }

    private static void supplyPower(ServerLevel level, BlockPos pos) {
        level.setBlockAndUpdate(pos.north(), Registration.NEGARITE_GENERATOR.get().defaultBlockState());
        level.setBlockAndUpdate(pos.south(), Registration.POSIRITE_GENERATOR.get().defaultBlockState());
        PowerSystem power = PowerSystem.getPowerSystem(level);
        int negarite = power.allocateId();
        int posirite = power.allocateId();
        ((IPowerBlob) level.getBlockEntity(pos.north())).fillCableId(negarite);
        ((IPowerBlob) level.getBlockEntity(pos.south())).fillCableId(posirite);
        power.addPower(negarite, 1000, PowerType.NEGARITE);
        power.addPower(posirite, 1000, PowerType.POSIRITE);
    }
}
