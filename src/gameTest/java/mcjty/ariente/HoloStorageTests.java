package mcjty.ariente;

import mcjty.ariente.blocks.storagecube.StorageCubeTile;
import mcjty.ariente.blocks.storagecube.StorageCubeTransfer;
import mcjty.ariente.blocks.storagecube.StorageCubeSort;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.Set;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class HoloStorageTests {

    @GameTest(template = "empty")
    public static void capacitiesAllSidesAndLifecycle(GameTestHelper helper) {
        int[] sizes = {30, 60, 120};
        List<Block> variants = variants();
        for (int tier = 0; tier < variants.size(); tier++) {
            StorageCubeTile tile = place(helper, variants.get(tier));
            IItemHandler items = tile.getItems();
            helper.assertTrue(items.getSlots() == sizes[tier], "Tier capacity must be exact");
            for (int slot = 0; slot < sizes[tier]; slot++) {
                helper.assertTrue(items.insertItem(slot, new ItemStack(Items.COBBLESTONE, 64), false).isEmpty(),
                        "Every slot accepts a full stack");
            }
            helper.assertTrue(ItemHandlerHelper.insertItemStacked(items, new ItemStack(Items.COBBLESTONE), false).getCount() == 1,
                    "Full cubes reject overflow");
            for (Direction side : Direction.values()) {
                IItemHandler sided = tile.getCapability(ForgeCapabilities.ITEM_HANDLER, side).orElseThrow(IllegalStateException::new);
                helper.assertTrue(sided.getSlots() == sizes[tier], "Every face exposes every slot");
                helper.assertTrue(sided.extractItem(0, 1, true).getCount() == 1 && items.getStackInSlot(0).getCount() == 64,
                        "Extraction simulation is read-only");
                helper.assertTrue(sided.extractItem(0, 1, false).getCount() == 1, "Every side allows output");
                helper.assertTrue(sided.insertItem(0, new ItemStack(Items.COBBLESTONE), true).isEmpty()
                        && items.getStackInSlot(0).getCount() == 63, "Insertion simulation is read-only");
                helper.assertTrue(sided.insertItem(0, new ItemStack(Items.COBBLESTONE), false).isEmpty(), "Every side allows input");
            }
            var old = tile.getCapability(ForgeCapabilities.ITEM_HANDLER);
            tile.invalidateCaps();
            helper.assertTrue(!old.isPresent(), "Removed tiles invalidate their handler");
            tile.reviveCaps();
            helper.assertTrue(tile.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent(), "Revival restores automation");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void saveAndBlockLootPreserveLastSlotAndItemData(GameTestHelper helper) {
        for (Block block : variants()) {
            StorageCubeTile tile = place(helper, block);
            int last = tile.getItems().getSlots() - 1;
            ItemStack named = new ItemStack(Items.DIAMOND, 23);
            named.getOrCreateTag().putString("StorageTest", "different components");
            tile.getItems().setStackInSlot(last, named);
            tile.getItems().setStackInSlot(0, new ItemStack(Items.DIAMOND_SWORD));
            CompoundTag saved = tile.saveWithoutMetadata();
            StorageCubeTile restored = new StorageCubeTile(tile.getBlockPos(), tile.getBlockState());
            restored.load(saved);
            helper.assertTrue(ItemStack.matches(named, restored.getItems().getStackInSlot(last)), "All tiers retain their last slot and NBT");
            helper.assertTrue(restored.getItems().getStackInSlot(0).getCount() == 1, "Unstackable items retain their own stack limit");
            List<ItemStack> drops = Block.getDrops(tile.getBlockState(), helper.getLevel(), tile.getBlockPos(), tile);
            helper.assertTrue(drops.size() == 1 && drops.get(0).is(block.asItem()), "Breaking produces one storage cube");
            CompoundTag blockData = BlockItem.getBlockEntityData(drops.get(0));
            helper.assertTrue(blockData != null && blockData.getList("McItems", Tag.TAG_COMPOUND).size() == tile.getItems().getSlots(),
                    "The dropped cube carries its inventory");
            StorageCubeTile fromDrop = new StorageCubeTile(tile.getBlockPos(), tile.getBlockState());
            fromDrop.load(blockData);
            helper.assertTrue(ItemStack.matches(named, fromDrop.getItems().getStackInSlot(last)), "Dropped cube restores its contents");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void atomicDragsMergeSwapAndRejectStaleSources(GameTestHelper helper) {
        StorageCubeTile tile = place(helper, Registration.HOLO_STORAGE_1.get());
        Player player = helper.makeMockSurvivalPlayer();
        player.getInventory().clearContent();
        int playerSlot = tile.getItems().getSlots();
        player.getInventory().setItem(0, new ItemStack(Items.IRON_INGOT, 40));
        tile.getItems().setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 50));
        helper.assertTrue(move(tile, player, playerSlot, 0, 40), "A drag can merge into storage");
        helper.assertTrue(tile.getItems().getStackInSlot(0).getCount() == 64 && player.getInventory().getItem(0).getCount() == 26,
                "A partial merge leaves all overflow at its source");
        helper.assertTrue(move(tile, player, playerSlot, 1, 13), "Half-stack movement works");
        helper.assertTrue(tile.getItems().getStackInSlot(1).getCount() == 13 && player.getInventory().getItem(0).getCount() == 13,
                "Half-stack movement conserves items");
        tile.getItems().setStackInSlot(2, new ItemStack(Items.GOLD_INGOT, 3));
        helper.assertTrue(move(tile, player, playerSlot, 2, 13), "Incompatible stacks swap when dragging a full stack");
        helper.assertTrue(player.getInventory().getItem(0).is(Items.GOLD_INGOT) && tile.getItems().getStackInSlot(2).getCount() == 13,
                "Swap returns the displaced stack to the original source");
        ItemStack expected = tile.getItems().getStackInSlot(1).copy();
        tile.getItems().extractItem(1, 1, false);
        helper.assertTrue(!StorageCubeTransfer.apply(tile, player, 1, playerSlot + 1, 13, false, expected, ItemStack.EMPTY),
                "Automation or another viewer changing the source rejects the stale drag");
        helper.assertTrue(tile.getItems().getStackInSlot(1).getCount() == 12 && player.getInventory().getItem(1).isEmpty(),
                "Rejecting stale input never moves items");
        player.getInventory().setItem(1, new ItemStack(Items.DIAMOND));
        helper.assertTrue(!StorageCubeTransfer.apply(tile, player, 1, playerSlot + 1, 12, false,
                tile.getItems().getStackInSlot(1).copy(), ItemStack.EMPTY), "A changed destination rejects the stale drag");
        helper.assertTrue(player.getInventory().getItem(1).is(Items.DIAMOND)
                && tile.getItems().getStackInSlot(1).getCount() == 12, "Stale destinations must never be overwritten");
        helper.assertTrue(!StorageCubeTransfer.apply(tile, player, playerSlot + 40, 0, 64, false,
                new ItemStack(Items.IRON_INGOT), ItemStack.EMPTY), "Armor and offhand cannot be addressed by panel actions");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void quickMovesRespectFullInventoriesAndStackLimits(GameTestHelper helper) {
        StorageCubeTile tile = place(helper, Registration.HOLO_STORAGE_1.get());
        Player player = helper.makeMockSurvivalPlayer();
        player.getInventory().clearContent();
        player.getInventory().setItem(0, new ItemStack(Items.ENDER_PEARL, 16));
        int size = tile.getItems().getSlots();
        helper.assertTrue(StorageCubeTransfer.apply(tile, player, size, -1, 64, true,
                new ItemStack(Items.ENDER_PEARL, 16), ItemStack.EMPTY), "Sneak-click inserts a stack");
        helper.assertTrue(player.getInventory().getItem(0).isEmpty() && tile.getItems().getStackInSlot(0).getCount() == 16,
                "Pearls keep their native stack size");
        for (int i = 0; i < 36; i++) {
            player.getInventory().setItem(i, new ItemStack(Items.COBBLESTONE, 64));
        }
        StorageCubeTransfer.apply(tile, player, 0, -1, 64, true, new ItemStack(Items.ENDER_PEARL, 16), ItemStack.EMPTY);
        helper.assertTrue(tile.getItems().getStackInSlot(0).getCount() == 16, "Full player inventories retain the source");
        player.getInventory().setItem(35, ItemStack.EMPTY);
        StorageCubeTransfer.apply(tile, player, 0, -1, 64, true, new ItemStack(Items.ENDER_PEARL, 16), ItemStack.EMPTY);
        helper.assertTrue(tile.getItems().getStackInSlot(0).isEmpty() && player.getInventory().getItem(35).getCount() == 16,
                "Extraction reaches all 36 main inventory slots");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void previewIsBoundedWeightedAndSeparateFromInventory(GameTestHelper helper) {
        StorageCubeTile tile = place(helper, Registration.HOLO_STORAGE_3.get());
        tile.getItems().setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 64));
        tile.getItems().setStackInSlot(1, new ItemStack(Items.IRON_INGOT, 32));
        tile.getItems().setStackInSlot(119, new ItemStack(Items.DIAMOND));
        helper.runAfterDelay(25, () -> {
            helper.assertTrue(tile.getPreview().size() == 2, "Duplicate item stacks share one preview entry");
            var iron = tile.getPreview().stream().filter(entry -> entry.stack().is(Items.IRON_INGOT)).findFirst().orElseThrow();
            helper.assertTrue(iron.weight() == 96 && iron.stack().getCount() == 1, "Preview weights reflect total item counts");
            CompoundTag sync = tile.getUpdateTag();
            helper.assertTrue(!sync.contains("Items") && !sync.contains("McItems") && sync.getAllKeys().equals(Set.of("Preview")),
                    "Chunk watchers receive no real inventory or player data");
            StorageCubeTile client = new StorageCubeTile(tile.getBlockPos(), tile.getBlockState());
            client.handleUpdateTag(sync);
            helper.assertTrue(client.getPreview().size() == 2 && client.getItems().getStackInSlot(119).isEmpty(),
                    "Chunk updates load only the preview, without creating a client inventory mirror");
            for (int i = 0; i < 120; i++) {
                ItemStack stack = new ItemStack(Items.DIAMOND);
                stack.getOrCreateTag().putInt("Variant", i);
                tile.getItems().setStackInSlot(i, stack);
            }
            helper.runAfterDelay(25, () -> {
                helper.assertTrue(tile.getPreview().size() == StorageCubeTile.PREVIEW_SIZE, "Large inventories send only a bounded candidate pool");
                helper.succeed();
            });
        });
    }

    @GameTest(template = "empty")
    public static void everyTierHasAnIncreasingBlueprintCost(GameTestHelper helper) {
        int previousCost = 0;
        for (Block block : variants()) {
            var recipe = BlueprintRecipeRegistry.findRecipe(new ItemStack(block));
            helper.assertTrue(recipe != null, "Every tier needs a populated Constructor blueprint");
            int cost = recipe.getIngredientList().stream().mapToInt(ItemStack::getCount).sum();
            helper.assertTrue(cost > previousCost * 2, "Higher tiers cost substantially more materials");
            helper.assertTrue(recipe.getIngredientList().size() <= 9, "Recipes fit the nine-ingredient JEI display");
            previousCost = cost;
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sortsKeepCountsNamesAndItemDataAcrossAllTiers(GameTestHelper helper) {
        for (Block block : variants()) {
            StorageCubeTile tile = place(helper, block);
            var items = tile.getItems();
            ItemStack diamond = new ItemStack(Items.DIAMOND, 5).setHoverName(Component.literal("Zulu"));
            diamond.getOrCreateTag().putString("Variant", "keep this data");
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD).setHoverName(Component.literal("alpha"));
            sword.enchant(Enchantments.SHARPNESS, 2);
            ItemStack iron = new ItemStack(Items.IRON_INGOT, 32).setHoverName(Component.literal("Beta"));
            ItemStack taggedIron = new ItemStack(Items.IRON_INGOT, 12).setHoverName(Component.literal("Beta"));
            taggedIron.getOrCreateTag().putString("Batch", "distinct");
            List<ItemStack> original = List.of(diamond.copy(), sword.copy(), iron.copy(), taggedIron.copy());
            items.setStackInSlot(items.getSlots() - 1, sword);
            items.setStackInSlot(0, diamond);
            items.setStackInSlot(3, iron);
            items.setStackInSlot(5, taggedIron);

            StorageCubeSort.apply(items, false);
            helper.assertTrue(ItemStack.matches(items.getStackInSlot(0), sword)
                            && ItemStack.matches(items.getStackInSlot(1), iron)
                            && ItemStack.matches(items.getStackInSlot(2), taggedIron)
                            && ItemStack.matches(items.getStackInSlot(3), diamond),
                    "A-Z uses case-insensitive visible names, then largest stacks for matching names");
            StorageCubeSort.apply(items, true);
            int[] counts = {32, 12, 5, 1};
            for (int slot = 0; slot < items.getSlots(); slot++) {
                ItemStack stack = items.getStackInSlot(slot);
                helper.assertTrue(slot < 4 ? stack.getCount() == counts[slot] : stack.isEmpty(),
                        "Count sort puts large stacks first and every empty slot at the end");
            }
            for (ItemStack expected : original) {
                int matches = 0;
                for (int slot = 0; slot < items.getSlots(); slot++) {
                    if (ItemStack.matches(expected, items.getStackInSlot(slot))) {
                        matches++;
                    }
                }
                helper.assertTrue(matches == 1, "Sorting preserves each complete stack, including names, tags and enchantments");
            }
            CompoundTag saved = tile.saveWithoutMetadata();
            StorageCubeTile restored = new StorageCubeTile(tile.getBlockPos(), tile.getBlockState());
            restored.load(saved);
            helper.assertTrue(ItemStack.matches(restored.getItems().getStackInSlot(0), iron)
                            && ItemStack.matches(restored.getItems().getStackInSlot(3), sword),
                    "Sorting changes the saved storage inventory");
        }
        helper.succeed();
    }

    private static boolean move(StorageCubeTile tile, Player player, int source, int destination, int amount) {
        int size = tile.getItems().getSlots();
        ItemStack from = source < size ? tile.getItems().getStackInSlot(source) : player.getInventory().getItem(source - size);
        ItemStack to = destination < size ? tile.getItems().getStackInSlot(destination) : player.getInventory().getItem(destination - size);
        return StorageCubeTransfer.apply(tile, player, source, destination, amount, false, from.copy(), to.copy());
    }

    private static StorageCubeTile place(GameTestHelper helper, Block block) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos, block.defaultBlockState());
        return (StorageCubeTile) helper.getLevel().getBlockEntity(pos);
    }

    private static List<Block> variants() {
        return List.of(Registration.HOLO_STORAGE_1.get(), Registration.HOLO_STORAGE_2.get(), Registration.HOLO_STORAGE_3.get());
    }
}
