package mcjty.ariente;

import java.util.List;
import mcjty.ariente.blocks.utility.StorageTile;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.items.ItemData;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BaseBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class StorageTests {

    @GameTest(template = "empty")
    public static void bothVariantsKeepSixtyFourStacksPerType(GameTestHelper helper) {
        for (BaseBlock block : variants()) {
            StorageTile tile = place(helper, block);
            int types = block == Registration.STORAGE.get() ? 4 : 9;
            IItemHandler inventory = inventory(tile);
            helper.assertTrue(tile.getTypeCount() == types && inventory.getSlots() == types * 64,
                    "Original and 3x3 cubes expose four and nine groups of 64 stack slots");
            for (int type = 0; type < types; type++) {
                ItemStack stack = taggedStack(type, 64);
                for (int offset = 0; offset < 64; offset++) {
                    helper.assertTrue(inventory.insertItem(type * 64 + offset, stack.copy(), false).isEmpty(),
                            "Every slot in type " + type + " accepts its own item");
                }
                helper.assertTrue(tile.getTotalStack(type).getCount() == 4096, "Every area holds 4096 stackable items");
                helper.assertTrue(inventory.insertItem(type * 64, stack, false).getCount() == 64, "Full areas reject overflow");
                helper.assertTrue(!inventory.isItemValid(type * 64, taggedStack(type + 100, 1)), "Groups keep separate item data");
                helper.assertTrue(inventory.extractItem(type * 64, 1, true).getCount() == 1
                        && tile.getTotalStack(type).getCount() == 4096, "Simulated extraction keeps totals unchanged");
                helper.assertTrue(inventory.extractItem(type * 64, 1, false).getCount() == 1
                        && tile.getTotalStack(type).getCount() == 4095, "Extraction updates only its group's total");
            }
            for (Direction side : Direction.values()) {
                helper.assertTrue(tile.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, tile.getBlockPos(), side)
                        .getSlots() == types * 64, "Every side exposes the complete grouped inventory");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void saveAndClientSyncPreserveEveryArea(GameTestHelper helper) {
        for (BaseBlock block : variants()) {
            StorageTile tile = place(helper, block);
            for (int type = 0; type < tile.getTypeCount(); type++) {
                inventory(tile).insertItem(type * 64, taggedStack(type, 32), false);
                inventory(tile).insertItem(type * 64 + 63, taggedStack(type, 17), false);
            }
            tile.setKeyId("storage-test");
            tile.setLocked(true);
            CompoundTag saved = tile.saveWithoutMetadata(helper.getLevel().registryAccess());
            int types = tile.getTypeCount();
            helper.assertTrue(saved.getCompound("Info").getIntArray("Counts").length == types * 64,
                    "Saved counts retain the original format and the variant's capacity");
            StorageTile restored = new StorageTile(tile.getBlockPos(), tile.getBlockState());
            restored.setLevel(helper.getLevel());
            restored.loadWithComponents(saved, helper.getLevel().registryAccess());
            CompoundTag clientData = new CompoundTag();
            tile.saveClientDataToNBT(clientData, helper.getLevel().registryAccess());
            StorageTile client = new StorageTile(tile.getBlockPos(), tile.getBlockState());
            client.loadClientDataFromNBT(clientData, helper.getLevel().registryAccess());
            helper.assertTrue(restored.isLocked() && "storage-test".equals(restored.getKeyId()) && client.isLocked(),
                    "Save/load and client sync preserve locking");
            helper.assertTrue(inventory(restored).extractItem(0, 1, false).isEmpty(), "Restored access locks block automation");
            restored.setLocked(false);
            for (int type = 0; type < types; type++) {
                ItemStack expected = taggedStack(type, 49);
                helper.assertTrue(ItemStack.matches(restored.getTotalStack(type), expected)
                        && ItemStack.matches(client.getTotalStack(type), expected), "Every area's item, data and total survive sync");
                helper.assertTrue(ItemStack.matches(inventory(restored).extractItem(type * 64 + 63, 64, false), taggedStack(type, 17)),
                        "Save/load preserves the last physical stack of every group");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void frontSelectionAndTransfersFollowAllSixFacings(GameTestHelper helper) {
        for (BaseBlock block : variants()) {
            StorageTile tile = place(helper, block);
            Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
            int size = tile.getGridSize();
            for (Direction front : Direction.values()) {
                helper.getLevel().setBlockAndUpdate(tile.getBlockPos(), tile.getBlockState().setValue(BlockStateProperties.FACING, front));
                for (int type = 0; type < tile.getTypeCount(); type++) {
                    double x = size == 2 ? .285 + (type % size) * .375 : .23 + (type % size) * .25;
                    double y = size == 2 ? .745 - (type / size) * .375 : .8 - (type / size) * .25;
                    Vec3 hit = frontHit(front, x, y);
                    Vec3 worldHit = hit.add(Vec3.atLowerCornerOf(tile.getBlockPos()));
                    helper.assertTrue(StorageTile.getSlot(helper.getLevel(), tile.getBlockPos(), front, worldHit) == type,
                            "Rendered area " + type + " selects itself facing " + front);
                    helper.assertTrue(StorageTile.getSlot(helper.getLevel(), tile.getBlockPos(), front.getOpposite(), worldHit) == -1,
                            "Other faces cannot select a front area");
                    player.setItemInHand(InteractionHand.MAIN_HAND, taggedStack(type, 3));
                    StorageTile.onActivate(helper.getLevel(), tile.getBlockPos(), player, front, hit.x, hit.y, hit.z);
                    helper.assertTrue(player.getMainHandItem().isEmpty() && tile.getTotalStack(type).getCount() == 3,
                            "Right-click inserts into the selected area");
                    tile.giveToPlayer(type, player);
                    helper.assertTrue(player.getMainHandItem().getCount() == 1 && tile.getTotalStack(type).getCount() == 2,
                            "Left-click retrieves one item");
                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    player.setShiftKeyDown(true);
                    tile.giveToPlayer(type, player);
                    helper.assertTrue(player.getMainHandItem().getCount() == 2 && tile.getTotalStack(type).isEmpty(),
                            "Sneak-left-click retrieves the remaining stack");
                    player.setShiftKeyDown(false);
                }
                Vec3 bottom = frontHit(front, .5, .05).add(Vec3.atLowerCornerOf(tile.getBlockPos()));
                helper.assertTrue(StorageTile.getSlot(helper.getLevel(), tile.getBlockPos(), front, bottom) == -1,
                        "The lower strip still opens the access panel");
            }
            tile.giveToPlayer(-1, player);
            tile.giveToPlayer(tile.getTypeCount(), player);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void bothBlueprintsResolveToTheirOwnCube(GameTestHelper helper) {
        for (BaseBlock block : variants()) {
            ItemStack blueprint = BlueprintItem.makeBluePrint(new ItemStack(block));
            var recipe = BlueprintRecipeRegistry.findRecipe(BlueprintItem.getDestination(blueprint));
            helper.assertTrue(recipe != null && recipe.getDestination().is(block.asItem()),
                    "Each cube has its own populated blueprint and Constructor recipe");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void machineDropComponentsPreserveInventoryAndLock(GameTestHelper helper) {
        StorageTile tile = place(helper, Registration.STORAGE.get());
        inventory(tile).insertItem(0, taggedStack(7, 23), false);
        tile.setKeyId("component-round-trip");
        tile.setLocked(true);
        ItemStack drop = net.minecraft.world.level.block.Block.getDrops(tile.getBlockState(), helper.getLevel(),
                tile.getBlockPos(), tile).stream().filter(stack -> stack.is(Registration.STORAGE.get().asItem())).findFirst().orElseThrow();
        helper.assertTrue(drop.has(Registration.MACHINE_DATA.get()), "Machine loot copies its persistent component");
        ItemStack decoded = ItemStack.parse(helper.getLevel().registryAccess(), drop.save(helper.getLevel().registryAccess())).orElseThrow();
        StorageTile restored = new StorageTile(tile.getBlockPos(), tile.getBlockState());
        restored.setLevel(helper.getLevel());
        restored.applyComponentsFromItemStack(decoded);
        helper.assertTrue(restored.isLocked() && "component-round-trip".equals(restored.getKeyId()), "Placement restores lock settings");
        helper.assertTrue(ItemStack.matches(restored.getTotalStack(0), taggedStack(7, 23)), "Dropped and placed machines retain full item components and counts");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void itemComponentsUseCopyOnWrite(GameTestHelper helper) {
        ItemStack original = new ItemStack(Registration.ENERGY_HOLDER.get());
        ItemData.update(original, tag -> tag.putInt("mode", 1));
        ItemStack copy = original.copy();
        ItemData.update(copy, tag -> tag.putInt("mode", 2));
        ItemData.get(original).putInt("mode", 99);
        helper.assertTrue(ItemData.get(original).getInt("mode") == 1 && ItemData.get(copy).getInt("mode") == 2,
                "Stack copies and read snapshots cannot mutate each other's component state");
        ItemStack module = new ItemStack(Registration.DISPLAY_TEXT.get());
        mcjty.ariente.display.DisplayModuleItem.editConfig(module, config -> config.putString("Text", "Saved hologram"));
        ItemStack decoded = ItemStack.parse(helper.getLevel().registryAccess(), module.save(helper.getLevel().registryAccess())).orElseThrow();
        helper.assertTrue("Saved hologram".equals(mcjty.ariente.display.DisplayModuleItem.config(decoded).getString("Text")),
                "Display configuration survives item serialization");
        helper.succeed();
    }

    private static Vec3 frontHit(Direction front, double x, double y) {
        return switch (front) {
            case NORTH -> new Vec3(1 - x, y, 0);
            case SOUTH -> new Vec3(x, y, 1);
            case WEST -> new Vec3(0, y, x);
            case EAST -> new Vec3(1, y, 1 - x);
            case UP -> new Vec3(x, 1, 1 - y);
            case DOWN -> new Vec3(x, 0, y);
        };
    }

    private static List<BaseBlock> variants() {
        return List.of(Registration.STORAGE.get(), Registration.STORAGE_3X3.get());
    }

    private static StorageTile place(GameTestHelper helper, BaseBlock block) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().removeBlock(pos, false);
        helper.getLevel().setBlockAndUpdate(pos, block.defaultBlockState());
        return (StorageTile) helper.getLevel().getBlockEntity(pos);
    }

    private static IItemHandler inventory(StorageTile tile) {
        return tile.getAutomationHandler(null);
    }

    private static ItemStack taggedStack(int type, int count) {
        ItemStack stack = new ItemStack(Items.STONE, count);
        ItemData.update(stack, tag -> tag.putInt("StorageType", type));
        return stack;
    }
}
