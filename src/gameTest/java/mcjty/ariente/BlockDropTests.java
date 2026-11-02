package mcjty.ariente;

import mcjty.ariente.blocks.utility.autofield.InputItemNodeTile;
import mcjty.ariente.blocks.utility.autofield.OutputItemNodeTile;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.multipart.MultipartTE;
import mcjty.ariente.setup.Registration;
import mcjty.lib.multipart.PartSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class BlockDropTests {

    @GameTest(template = "empty")
    public static void lootAndPlacementPreserveMachineSettings(GameTestHelper helper) throws Exception {
        List<SavedBlock> cases = List.of(
                new SavedBlock(Registration.AICORE.get(), "{cityName:'Drop test city'}"),
                new SavedBlock(Registration.FORCEFIELD.get(), "{scale:4}"),
                new SavedBlock(Registration.NEGARITE_GENERATOR.get(), "{dust:123}"),
                new SavedBlock(Registration.POSIRITE_GENERATOR.get(), "{dust:234}"),
                new SavedBlock(Registration.POWER_COMBINER.get(), "{transfer:345}"),
                new SavedBlock(Registration.AUTOMATION_FIELD.get(), "{height:3,renderItems:1b,renderOutline:1b}"),
                new SavedBlock(Registration.DOOR_MARKER.get(), "{icon:5,locked:1b}"),
                new SavedBlock(Registration.ELEVATOR.get(), "{height:3}"),
                new SavedBlock(Registration.LOCK.get(), "{locked:1b,keyId:'drop-test',vertical:7,horizontal:9}"),
                new SavedBlock(Registration.STORAGE.get(), "{locked:1b,keyId:'storage-test'}"),
                new SavedBlock(Registration.WARPER.get(), "{charges:17}"),
                new SavedBlock(Registration.SIGNAL_TRANSMITTER.get(), "{channel:41,desired:42}"),
                new SavedBlock(Registration.SIGNAL_RECEIVER.get(), "{channel:43,desired:44}"),
                new SavedBlock(Registration.WIRELESS_BUTTON.get(), "{channel:45,desired:46,locked:1b}"),
                new SavedBlock(Registration.WIRELESS_LOCK.get(), "{channel:47,desired:48,vertical:6,horizontal:8}"),
                new SavedBlock(Registration.SENSOR_ITEM_NODE.get(), "{f0:3,f2:6,outColor:5,op:2,amount:17}"),
                new SavedBlock(Registration.FIELD_MARKER.get(), "{autofield:[I;123,64,-345]}"));
        for (SavedBlock test : cases) {
            CompoundTag expected = TagParser.parseTag(test.info());
            CompoundTag data = new CompoundTag();
            data.put("Info", expected.copy());
            BlockEntity source = source(helper, test.block());
            source.load(data);
            ItemStack drop = loot(helper, source);
            assertContains(helper, expected, BlockItem.getBlockEntityData(drop).getCompound("Info"), "Dropped " + test.block());
            BlockEntity restored = place(helper, drop, test.block());
            assertContains(helper, expected, restored.saveWithoutMetadata().getCompound("Info"), "Replaced " + test.block());
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void lootAndPlacementPreserveInventoryContents(GameTestHelper helper) {
        List<Block> blocks = List.of(Registration.STORAGE.get(), Registration.NEGARITE_GENERATOR.get(),
                Registration.POSIRITE_GENERATOR.get(), Registration.AUTO_CONSTRUCTOR.get(), Registration.BLUEPRINT_STORAGE.get());
        ItemStack blueprint = BlueprintItem.makeBluePrint(new ItemStack(Registration.FIELD_MARKER.get()));
        List<ItemStack> contents = List.of(new ItemStack(Items.IRON_INGOT, 64), new ItemStack(Registration.DUST_NEGARITE.get(), 7),
                new ItemStack(Registration.DUST_POSIRITE.get(), 9), new ItemStack(Items.GOLD_INGOT, 13), blueprint);
        for (int i = 0; i < blocks.size(); i++) {
            BlockEntity source = source(helper, blocks.get(i));
            ItemStack expected = contents.get(i);
            IItemHandler inventory = source.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
            helper.assertTrue(ItemHandlerHelper.insertItem(inventory, expected.copy(), false).isEmpty(), "Inventory accepts test contents");
            BlockEntity restored = place(helper, loot(helper, source), blocks.get(i));
            IItemHandler restoredInventory = restored.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
            helper.assertTrue(ItemStack.matches(expected, restoredInventory.getStackInSlot(0)),
                    "Loot and replacement must preserve inventory counts and item data for " + blocks.get(i));
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void lootAndPlacementPreserveNodeFilters(GameTestHelper helper) throws Exception {
        for (boolean input : new boolean[]{true, false}) {
            Block block = input ? Registration.INPUT_ITEM_NODE.get() : Registration.OUTPUT_ITEM_NODE.get();
            String key = input ? "input" : "output";
            CompoundTag filter = TagParser.parseTag("{Items:[{Slot:0b,id:'minecraft:iron_ingot',Count:1b,tag:{FilterTest:'keep'}}]}");
            CompoundTag expected = TagParser.parseTag(input
                    ? "{f0:2,f3:9,inDamage:1b,inNBT:1b,inOre:1b}"
                    : "{f0:2,f3:9,outDamage:1b,outNBT:1b,outOre:1b,outSS:23}");
            expected.put(key, filter);
            CompoundTag data = new CompoundTag();
            data.put("Info", expected.copy());
            BlockEntity source = source(helper, block);
            source.load(data);
            ItemStack drop = loot(helper, source);
            assertContains(helper, expected, BlockItem.getBlockEntityData(drop).getCompound("Info"), "Node loot");
            BlockEntity restored = place(helper, drop, block);
            assertContains(helper, expected, restored.saveWithoutMetadata().getCompound("Info"), "Replaced node");
            ItemStack restoredFilter = input ? ((InputItemNodeTile) restored).getInputFilter().get(0)
                    : ((OutputItemNodeTile) restored).getOutputFilter().get(0);
            helper.assertTrue(restoredFilter.is(Items.IRON_INGOT) && restoredFilter.getTag().getString("FilterTest").equals("keep"),
                    "Node filters must retain their actual items and NBT");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void multipartDropsPreserveEachPartsConfiguration(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        level.setBlockAndUpdate(pos, Registration.MULTIPART_BLOCK.get().defaultBlockState());
        MultipartTE multipart = (MultipartTE) level.getBlockEntity(pos);
        InputItemNodeTile input = new InputItemNodeTile(pos, Registration.INPUT_ITEM_NODE.get().defaultBlockState());
        CompoundTag data = TagParser.parseTag("{Info:{f0:2,inNBT:1b,input:{Items:[{Slot:0b,id:'minecraft:gold_ingot',Count:1b}]}}}");
        input.load(data);
        input.setLevel(level);
        multipart.addPart(PartSlot.DOWN_NW, input.getBlockState(), input);
        List<ItemStack> drops = Block.getDrops(multipart.getBlockState(), level, pos, multipart);
        helper.assertTrue(drops.size() == 1, "Multipart breaking produces one configured item per part");
        BlockEntity restored = place(helper, drops.get(0), Registration.INPUT_ITEM_NODE.get());
        assertContains(helper, data.getCompound("Info"), restored.saveWithoutMetadata().getCompound("Info"), "Replaced multipart node");
        helper.succeed();
    }

    private static BlockEntity source(GameTestHelper helper, Block block) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().removeBlock(pos, false);
        helper.getLevel().setBlockAndUpdate(pos, block.defaultBlockState());
        return helper.getLevel().getBlockEntity(pos);
    }

    private static ItemStack loot(GameTestHelper helper, BlockEntity tile) {
        List<ItemStack> drops = Block.getDrops(tile.getBlockState(), helper.getLevel(), tile.getBlockPos(), tile);
        helper.assertTrue(drops.size() == 1 && drops.get(0).is(tile.getBlockState().getBlock().asItem()),
                "Block loot must produce exactly one matching item for " + tile.getBlockState());
        helper.assertTrue(BlockItem.getBlockEntityData(drops.get(0)) != null, "Block loot must attach saved data");
        return drops.get(0);
    }

    private static BlockEntity place(GameTestHelper helper, ItemStack drop, Block block) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 1));
        level.removeBlock(pos, false);
        level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        Player player = helper.makeMockSurvivalPlayer();
        ItemStack stack = drop.copy();
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPlaceContext context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, stack,
                new BlockHitResult(Vec3.atCenterOf(pos.below()).add(0, .5, 0), Direction.UP, pos.below(), false));
        helper.assertTrue(((BlockItem) stack.getItem()).place(context).consumesAction(), "Dropped block must place successfully");
        BlockEntity restored = level.getBlockEntity(pos);
        if (restored instanceof MultipartTE multipart) {
            restored = multipart.getParts().values().stream().filter(part -> part.getState().is(block))
                    .findFirst().orElseThrow().getBlockEntity();
        }
        helper.assertTrue(restored != null, "Placement must create a block entity");
        return restored;
    }

    private static void assertContains(GameTestHelper helper, CompoundTag expected, CompoundTag actual, String context) {
        for (String key : expected.getAllKeys()) {
            helper.assertTrue(expected.get(key).equals(actual.get(key)), context + " must retain " + key
                    + "; expected " + expected.get(key) + ", got " + actual.get(key));
        }
    }

    private record SavedBlock(Block block, String info) {
    }
}
