package mcjty.ariente;

import mcjty.lib.varia.InventoryTools;
import mcjty.ariente.display.DisplayModuleItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;
import mcjty.ariente.blocks.utility.StorageTile;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.items.ItemData;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BaseBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

@EventBusSubscriber(modid = Ariente.MODID)
public class StorageTests {

    @SubscribeEvent
    public static void functions(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, registry -> {
            registry.register(Identifier.parse("ariente:both_variants_keep_sixty_four_stacks_per_type"), StorageTests::bothVariantsKeepSixtyFourStacksPerType);
            registry.register(Identifier.parse("ariente:save_and_client_sync_preserve_every_area"), StorageTests::saveAndClientSyncPreserveEveryArea);
            registry.register(Identifier.parse("ariente:front_selection_and_transfers_follow_all_six_facings"), StorageTests::frontSelectionAndTransfersFollowAllSixFacings);
            registry.register(Identifier.parse("ariente:both_blueprints_resolve_to_their_own_cube"), StorageTests::bothBlueprintsResolveToTheirOwnCube);
            registry.register(Identifier.parse("ariente:machine_drop_components_preserve_inventory_and_lock"), StorageTests::machineDropComponentsPreserveInventoryAndLock);
            registry.register(Identifier.parse("ariente:item_components_use_copy_on_write"), StorageTests::itemComponentsUseCopyOnWrite);
        });
    }

    @SubscribeEvent
    public static void tests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(Identifier.parse("ariente:port"));
        event.registerTest(Identifier.parse("ariente:both_variants_keep_sixty_four_stacks_per_type"), data ->
                new FunctionGameTestInstance(ResourceKey.create(
                        Registries.TEST_FUNCTION, Identifier.parse("ariente:both_variants_keep_sixty_four_stacks_per_type")), data),
                new TestData<>(environment, Identifier.parse("ariente:empty"), 200, 0, true));
        event.registerTest(Identifier.parse("ariente:save_and_client_sync_preserve_every_area"), data ->
                new FunctionGameTestInstance(ResourceKey.create(
                        Registries.TEST_FUNCTION, Identifier.parse("ariente:save_and_client_sync_preserve_every_area")), data),
                new TestData<>(environment, Identifier.parse("ariente:empty"), 200, 0, true));
        event.registerTest(Identifier.parse("ariente:front_selection_and_transfers_follow_all_six_facings"), data ->
                new FunctionGameTestInstance(ResourceKey.create(
                        Registries.TEST_FUNCTION, Identifier.parse("ariente:front_selection_and_transfers_follow_all_six_facings")), data),
                new TestData<>(environment, Identifier.parse("ariente:empty"), 200, 0, true));
        event.registerTest(Identifier.parse("ariente:both_blueprints_resolve_to_their_own_cube"), data ->
                new FunctionGameTestInstance(ResourceKey.create(
                        Registries.TEST_FUNCTION, Identifier.parse("ariente:both_blueprints_resolve_to_their_own_cube")), data),
                new TestData<>(environment, Identifier.parse("ariente:empty"), 200, 0, true));
        event.registerTest(Identifier.parse("ariente:machine_drop_components_preserve_inventory_and_lock"), data ->
                new FunctionGameTestInstance(ResourceKey.create(
                        Registries.TEST_FUNCTION, Identifier.parse("ariente:machine_drop_components_preserve_inventory_and_lock")), data),
                new TestData<>(environment, Identifier.parse("ariente:empty"), 200, 0, true));
        event.registerTest(Identifier.parse("ariente:item_components_use_copy_on_write"), data ->
                new FunctionGameTestInstance(ResourceKey.create(
                        Registries.TEST_FUNCTION, Identifier.parse("ariente:item_components_use_copy_on_write")), data),
                new TestData<>(environment, Identifier.parse("ariente:empty"), 200, 0, true));
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        helper.assertTrue(condition, Component.literal(message));
    }

    public static void bothVariantsKeepSixtyFourStacksPerType(GameTestHelper helper) {
        for (BaseBlock block : variants()) {
            StorageTile tile = place(helper, block);
            int types = block == Registration.STORAGE.get() ? 4 : 9;
            ResourceHandler<ItemResource> inventory = inventory(tile);
            check(helper, tile.getTypeCount() == types && inventory.size() == types * 64,
                    "Original and 3x3 cubes expose four and nine groups of 64 stack slots");
            for (int type = 0; type < types; type++) {
                ItemStack stack = taggedStack(type, 64);
                for (int offset = 0; offset < 64; offset++) {
                    check(helper, InventoryTools.insertItem(inventory, type * 64 + offset, stack.copy(), false).isEmpty(),
                            "Every slot in type " + type + " accepts its own item");
                }
                check(helper, tile.getTotalStack(type).getCount() == 4096, "Every area holds 4096 stackable items");
                check(helper, InventoryTools.insertItem(inventory, type * 64, stack, false).getCount() == 64, "Full areas reject overflow");
                check(helper, !inventory.isValid(type * 64, ItemResource.of(taggedStack(type + 100, 1))), "Groups keep separate item data");
                check(helper, InventoryTools.extractItem(inventory, type * 64, 1, true).getCount() == 1
                        && tile.getTotalStack(type).getCount() == 4096, "Simulated extraction keeps totals unchanged");
                check(helper, InventoryTools.extractItem(inventory, type * 64, 1, false).getCount() == 1
                        && tile.getTotalStack(type).getCount() == 4095, "Extraction updates only its group's total");
            }
            for (Direction side : Direction.values()) {
                check(helper, tile.getLevel().getCapability(Capabilities.Item.BLOCK, tile.getBlockPos(), side).size() == types * 64, "Every side exposes the complete grouped inventory");
            }
        }
        helper.succeed();
    }

    public static void saveAndClientSyncPreserveEveryArea(GameTestHelper helper) {
        for (BaseBlock block : variants()) {
            StorageTile tile = place(helper, block);
            for (int type = 0; type < tile.getTypeCount(); type++) {
                InventoryTools.insertItem(inventory(tile), type * 64, taggedStack(type, 32), false);
                InventoryTools.insertItem(inventory(tile), type * 64 + 63, taggedStack(type, 17), false);
            }
            tile.setKeyId("storage-test");
            tile.setLocked(true);
            CompoundTag saved = tile.saveWithoutMetadata(helper.getLevel().registryAccess());
            int types = tile.getTypeCount();
            check(helper, saved.getCompoundOrEmpty("Info").getIntArray("Counts").orElseGet(() -> new int[0]).length == types * 64,
                    "Saved counts retain the original format and the variant's capacity");
            StorageTile restored = new StorageTile(tile.getBlockPos(), tile.getBlockState());
            restored.setLevel(helper.getLevel());
            restored.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), saved));
            CompoundTag clientData = new CompoundTag();
            tile.saveClientDataToNBT(clientData, helper.getLevel().registryAccess());
            StorageTile client = new StorageTile(tile.getBlockPos(), tile.getBlockState());
            client.loadClientDataFromNBT(clientData, helper.getLevel().registryAccess());
            check(helper, restored.isLocked() && "storage-test".equals(restored.getKeyId()) && client.isLocked(),
                    "Save/load and client sync preserve locking");
            check(helper, InventoryTools.extractItem(inventory(restored), 0, 1, false).isEmpty(), "Restored access locks block automation");
            restored.setLocked(false);
            for (int type = 0; type < types; type++) {
                ItemStack expected = taggedStack(type, 49);
                check(helper, ItemStack.matches(restored.getTotalStack(type), expected)
                        && ItemStack.matches(client.getTotalStack(type), expected), "Every area's item, data and total survive sync");
                check(helper, ItemStack.matches(InventoryTools.extractItem(inventory(restored), type * 64 + 63, 64, false), taggedStack(type, 17)),
                        "Save/load preserves the last physical stack of every group");
            }
        }
        helper.succeed();
    }

    public static void frontSelectionAndTransfersFollowAllSixFacings(GameTestHelper helper) {
        for (BaseBlock block : variants()) {
            StorageTile tile = place(helper, block);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            int size = tile.getGridSize();
            for (Direction front : Direction.values()) {
                helper.getLevel().setBlockAndUpdate(tile.getBlockPos(), tile.getBlockState().setValue(BlockStateProperties.FACING, front));
                for (int type = 0; type < tile.getTypeCount(); type++) {
                    double x = size == 2 ? .285 + (type % size) * .375 : .23 + (type % size) * .25;
                    double y = size == 2 ? .745 - (type / size) * .375 : .8 - (type / size) * .25;
                    Vec3 hit = frontHit(front, x, y);
                    Vec3 worldHit = hit.add(Vec3.atLowerCornerOf(tile.getBlockPos()));
                    check(helper, StorageTile.getSlot(helper.getLevel(), tile.getBlockPos(), front, worldHit) == type,
                            "Rendered area " + type + " selects itself facing " + front);
                    check(helper, StorageTile.getSlot(helper.getLevel(), tile.getBlockPos(), front.getOpposite(), worldHit) == -1,
                            "Other faces cannot select a front area");
                    player.setItemInHand(InteractionHand.MAIN_HAND, taggedStack(type, 3));
                    StorageTile.onActivate(helper.getLevel(), tile.getBlockPos(), player, front, hit.x, hit.y, hit.z);
                    check(helper, player.getMainHandItem().isEmpty() && tile.getTotalStack(type).getCount() == 3,
                            "Right-click inserts into the selected area");
                    tile.giveToPlayer(type, player);
                    check(helper, player.getMainHandItem().getCount() == 1 && tile.getTotalStack(type).getCount() == 2,
                            "Left-click retrieves one item");
                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    player.setShiftKeyDown(true);
                    tile.giveToPlayer(type, player);
                    check(helper, player.getMainHandItem().getCount() == 2 && tile.getTotalStack(type).isEmpty(),
                            "Sneak-left-click retrieves the remaining stack");
                    player.setShiftKeyDown(false);
                }
                Vec3 bottom = frontHit(front, .5, .05).add(Vec3.atLowerCornerOf(tile.getBlockPos()));
                check(helper, StorageTile.getSlot(helper.getLevel(), tile.getBlockPos(), front, bottom) == -1,
                        "The lower strip still opens the access panel");
            }
            tile.giveToPlayer(-1, player);
            tile.giveToPlayer(tile.getTypeCount(), player);
        }
        helper.succeed();
    }

    public static void bothBlueprintsResolveToTheirOwnCube(GameTestHelper helper) {
        for (BaseBlock block : variants()) {
            ItemStack blueprint = BlueprintItem.makeBluePrint(new ItemStack(block));
            var recipe = BlueprintRecipeRegistry.findRecipe(BlueprintItem.getDestination(blueprint));
            check(helper, recipe != null && recipe.getDestination().is(block.asItem()),
                    "Each cube has its own populated blueprint and Constructor recipe");
        }
        helper.succeed();
    }

    public static void machineDropComponentsPreserveInventoryAndLock(GameTestHelper helper) {
        StorageTile tile = place(helper, Registration.STORAGE.get());
        InventoryTools.insertItem(inventory(tile), 0, taggedStack(7, 23), false);
        tile.setKeyId("component-round-trip");
        tile.setLocked(true);
        ItemStack drop = Block.getDrops(tile.getBlockState(), helper.getLevel(),
                tile.getBlockPos(), tile).stream().filter(stack -> stack.is(Registration.STORAGE.get().asItem())).findFirst().orElseThrow();
        check(helper, drop.has(Registration.MACHINE_DATA.get()), "Machine loot copies its persistent component");
        ItemStack decoded = ItemData.decode(helper.getLevel().registryAccess(), ItemData.encode(helper.getLevel().registryAccess(), drop));
        StorageTile restored = new StorageTile(tile.getBlockPos(), tile.getBlockState());
        restored.setLevel(helper.getLevel());
        restored.applyComponentsFromItemStack(decoded);
        check(helper, restored.isLocked() && "component-round-trip".equals(restored.getKeyId()), "Placement restores lock settings");
        check(helper, ItemStack.matches(restored.getTotalStack(0), taggedStack(7, 23)), "Dropped and placed machines retain full item components and counts");
        helper.succeed();
    }

    public static void itemComponentsUseCopyOnWrite(GameTestHelper helper) {
        ItemStack original = new ItemStack(Registration.ENERGY_HOLDER.get());
        ItemData.update(original, tag -> tag.putInt("mode", 1));
        ItemStack copy = original.copy();
        ItemData.update(copy, tag -> tag.putInt("mode", 2));
        ItemData.get(original).putInt("mode", 99);
        check(helper, ItemData.get(original).getIntOr("mode", 0) == 1 && ItemData.get(copy).getIntOr("mode", 0) == 2,
                "Stack copies and read snapshots cannot mutate each other's component state");
        ItemStack module = new ItemStack(Registration.DISPLAY_TEXT.get());
        DisplayModuleItem.editConfig(module, config -> config.putString("Text", "Saved hologram"));
        ItemStack decoded = ItemData.decode(helper.getLevel().registryAccess(), ItemData.encode(helper.getLevel().registryAccess(), module));
        check(helper, "Saved hologram".equals(DisplayModuleItem.config(decoded).getStringOr("Text", "")),
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

    private static ResourceHandler<ItemResource> inventory(StorageTile tile) {
        return tile.getAutomationHandler(null);
    }

    private static ItemStack taggedStack(int type, int count) {
        ItemStack stack = new ItemStack(Items.STONE, count);
        ItemData.update(stack, tag -> tag.putInt("StorageType", type));
        return stack;
    }
}
