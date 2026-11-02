package mcjty.ariente;

import mcjty.ariente.blocks.utility.AutoConstructorTile;
import mcjty.ariente.blocks.utility.BlueprintStorageTile;
import mcjty.ariente.blocks.utility.autofield.AbstractItemNodeTile;
import mcjty.ariente.blocks.utility.autofield.InputItemNodeTile;
import mcjty.ariente.blocks.utility.autofield.OutputItemNodeTile;
import mcjty.ariente.gui.PlayerInventoryPanel;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.multipart.MultipartTE;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.components.IPlayerSlots;
import mcjty.hologui.gui.HoloGuiEntity;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandlerModifiable;

import static mcjty.ariente.blocks.utility.autofield.AbstractNodeTile.ORIENTATION;
import static mcjty.ariente.blocks.utility.autofield.NodeOrientation.UP_NE;
import static mcjty.ariente.gui.InventoryLayout.*;
import static mcjty.hologui.api.IGuiTile.TAG_MAIN;
import static mcjty.hologui.setup.Registration.HOLOGUI_ENTITY_TYPE;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class PlayerInventoryPanelTests {

    @GameTest(template = "empty")
    public static void everyInventorySlotAndHotbarGapUseTheDisplayedPositions(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        InspectableInventoryPanel panel = new InspectableInventoryPanel();
        for (int slot = 0; slot < 36; slot++) {
            player.getInventory().setItem(slot, new ItemStack(Items.IRON_INGOT, slot + 1));
            panel.hit(player, null, slotX(slot), slotY(slot));
            helper.assertTrue(panel.getSelected() == slot, "Displayed slot must select inventory index " + slot);
        }
        for (double[] point : new double[][] {{GRID_X - .01, PLAYER_Y}, {GRID_X + WIDTH, PLAYER_Y},
                {GRID_X, PLAYER_Y - .01}, {GRID_X, PLAYER_Y + 3 * CELL + .15}, {GRID_X, HOTBAR_Y + CELL}}) {
            panel.hit(player, null, point[0], point[1]);
            helper.assertTrue(panel.getSelected() == -1, "Borders and the hotbar gap must not select a slot");
        }
        panel.filter((stack, index) -> index == 35);
        panel.hit(player, null, slotX(35), slotY(35));
        helper.assertTrue(panel.getSelected() == 35, "Filtering preserves the last row's real index");
        panel.hit(player, null, slotX(9), slotY(9));
        helper.assertTrue(panel.getSelected() == -1, "Unavailable items cannot be selected");
        panel.hitClient(player, null, slotX(9), slotY(9));
        helper.assertTrue(panel.getSelected() == -1, "Unavailable items do not trigger client selection or click feedback");
        helper.assertTrue(panel.hoveredStack(player, slotX(9), slotY(9)).is(Items.IRON_INGOT),
                "Unavailable items retain their hover tooltip");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void nodeFiltersCopySamplesFromTheLowerInventoryAndHotbar(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        for (boolean input : new boolean[] {true, false}) {
            helper.getLevel().setBlockAndUpdate(pos, Registration.MULTIPART_BLOCK.get().defaultBlockState());
            var state = (input ? Registration.INPUT_ITEM_NODE.get() : Registration.OUTPUT_ITEM_NODE.get())
                    .defaultBlockState().setValue(ORIENTATION, UP_NE);
            AbstractItemNodeTile node = input ? new InputItemNodeTile(pos, state) : new OutputItemNodeTile(pos, state);
            ((MultipartTE) helper.getLevel().getBlockEntity(pos)).addPart(UP_NE.getSlot(), state, node);
            HoloGuiEntity holo = panel(helper, pos, UP_NE.getSlot().name() + ":" + TAG_MAIN);
            var root = holo.getGui(player);
            for (int slot : new int[] {35, 8}) {
                ItemStack sample = new ItemStack(slot == 35 ? Items.DIAMOND : Items.GOLD_INGOT, 12);
                player.getInventory().setItem(slot, sample);
                doubleClick(root, player, holo, slotX(slot), slotY(slot));
                var filter = input ? ((InputItemNodeTile) node).getInputFilter() : ((OutputItemNodeTile) node).getOutputFilter();
                helper.assertTrue(filter.stream().anyMatch(stack -> ItemStack.matches(stack, sample)),
                        "Both nodes copy the selected inventory sample");
                helper.assertTrue(player.getInventory().getItem(slot).getCount() == 12, "Filters do not consume carried items");
            }
            for (String name : new String[] {"filter_mode", "filter_nbt", "filter_damage", "filter_tags"}) {
                IGuiComponent<?> option = root.findChild(name).orElseThrow();
                root.hit(player, holo, option.getX() + option.getW() / 2, option.getY() + option.getH() / 2);
            }
            CompoundTag saved = node.saveWithoutMetadata();
            AbstractItemNodeTile restored = input ? new InputItemNodeTile(pos, state) : new OutputItemNodeTile(pos, state);
            restored.load(saved);
            helper.assertTrue(input
                            ? ((InputItemNodeTile) restored).isInputBlacklist() && ((InputItemNodeTile) restored).isInputNbt()
                                    && ((InputItemNodeTile) restored).isInputDamage() && ((InputItemNodeTile) restored).isInputTags()
                            : ((OutputItemNodeTile) restored).isOutputBlacklist() && ((OutputItemNodeTile) restored).isOutputNbt()
                                    && ((OutputItemNodeTile) restored).isOutputDamage() && ((OutputItemNodeTile) restored).isOutputTags(),
                    "The four options must be clickable and survive a save/reload");
            saved.getCompound("Info").remove(input ? "inBlacklist" : "outBlacklist");
            restored.load(saved);
            helper.assertTrue(input ? !((InputItemNodeTile) restored).isInputBlacklist() : !((OutputItemNodeTile) restored).isOutputBlacklist(),
                    "Older nodes default to whitelist mode");
            helper.getLevel().removeBlock(pos, false);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void constructionPanelsPreserveFilteredIndicesAndTransferActions(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().setBlockAndUpdate(pos, Registration.BLUEPRINT_STORAGE.get().defaultBlockState());
        BlueprintStorageTile storage = (BlueprintStorageTile) helper.getLevel().getBlockEntity(pos);
        player.getInventory().setItem(8, BlueprintItem.makeBluePrint(new ItemStack(Registration.DISPLAY_BASE.get())));
        HoloGuiEntity holo = panel(helper, pos, TAG_MAIN);
        var root = holo.getGui(player);
        player.getInventory().setItem(9, new ItemStack(Items.DIRT, 12));
        doubleClick(root, player, holo, slotX(9), slotY(9));
        helper.assertTrue(player.getInventory().getItem(9).getCount() == 12 && storage.getItems().getStackInSlot(0).isEmpty(),
                "Double-clicking an unavailable stack never transfers it");
        doubleClick(root, player, holo, slotX(8), slotY(8));
        helper.assertTrue(player.getInventory().getItem(8).isEmpty() && !storage.getItems().getStackInSlot(0).isEmpty(),
                "Blueprints transfer from the separated hotbar to the upper machine slots");
        doubleClick(root, player, holo, 1.7, 1.4);
        helper.assertTrue(storage.getItems().getStackInSlot(0).isEmpty(), "Upper machine slots still transfer back to the player");
        storage.getItems().setStackInSlot(0, BlueprintItem.makeBluePrint(new ItemStack(Registration.DISPLAY_BASE.get())));

        player.getInventory().setItem(35, new ItemStack(Registration.INGOT_SILVER.get(), 4));
        player.getInventory().setItem(9, new ItemStack(Items.DIRT));
        BlockPos machinePos = pos.east();
        for (boolean automatic : new boolean[] {false, true}) {
            helper.getLevel().setBlockAndUpdate(machinePos, (automatic ? Registration.AUTO_CONSTRUCTOR.get() : Registration.CONSTRUCTOR.get()).defaultBlockState());
            holo = panel(helper, machinePos, automatic ? AutoConstructorTile.TAG_INGREDIENTS : TAG_MAIN);
            root = holo.getGui(player);
            IPlayerSlots inventory = (IPlayerSlots) root.findChild("playerslots").orElseThrow();
            root.hit(player, holo, slotX(9), slotY(9));
            helper.assertTrue(inventory.getSelected() == -1, "Construction inventories disable unrelated carried items");
            root.hit(player, holo, slotX(35), slotY(35));
            helper.assertTrue(inventory.getSelected() == 35, "Both Constructor inventories retain ingredient positions");
            if (automatic) {
                inventory.setSelection(9);
                root.hit(player, holo, .5, 1.7);
                helper.assertTrue(player.getInventory().getItem(9).is(Items.DIRT), "Arrow actions recheck unavailable selections");
                root.hit(player, holo, slotX(35), slotY(35));
                AutoConstructorTile machine = (AutoConstructorTile) helper.getLevel().getBlockEntity(machinePos);
                helper.assertTrue(player.getInventory().getItem(35).isEmpty() && machine.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new).getStackInSlot(0).getCount() == 4,
                        "Auto Constructor double-click transfers the actual selected ingredient");
            } else {
                helper.assertTrue(root.findChild("outputslots").orElseThrow().getY() < PLAYER_Y,
                        "Manual crafting results sit above the player inventory");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void blueprintAndIngredientPacketsIncludeUpdatesAndClearedSlots(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        for (boolean blueprints : new boolean[] {true, false}) {
            var state = (blueprints ? Registration.BLUEPRINT_STORAGE.get() : Registration.AUTO_CONSTRUCTOR.get()).defaultBlockState();
            helper.getLevel().setBlockAndUpdate(pos, state);
            var serverTile = (GenericTileEntity) helper.getLevel().getBlockEntity(pos);
            var clientTile = (GenericTileEntity) serverTile.getType().create(pos, state);
            var serverItems = (IItemHandlerModifiable) serverTile.getCapability(ForgeCapabilities.ITEM_HANDLER)
                    .orElseThrow(IllegalStateException::new);
            var clientItems = clientTile.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
            ItemStack stack = blueprints ? BlueprintItem.makeBluePrint(new ItemStack(Registration.DISPLAY_BASE.get()))
                    : new ItemStack(Registration.INGOT_SILVER.get(), 32);
            serverItems.setStackInSlot(0, stack);
            clientTile.loadClientDataFromNBT(serverTile.getUpdateTag());
            helper.assertTrue(ItemStack.matches(clientItems.getStackInSlot(0), stack), "Initial chunk data includes machine items");
            serverItems.setStackInSlot(0, ItemStack.EMPTY);
            clientTile.loadClientDataFromNBT(serverTile.getUpdatePacket().getTag());
            helper.assertTrue(clientItems.getStackInSlot(0).isEmpty(), "Later packets clear removed items from the client");
        }
        helper.succeed();
    }

    private static class InspectableInventoryPanel extends PlayerInventoryPanel {

        private InspectableInventoryPanel() {
            super("");
        }

        private ItemStack hoveredStack(Player player, double x, double y) {
            return getSelectedStack(player, x, y);
        }
    }

    private static HoloGuiEntity panel(GameTestHelper helper, BlockPos pos, String tag) {
        HoloGuiEntity holo = new HoloGuiEntity(HOLOGUI_ENTITY_TYPE.get(), helper.getLevel());
        holo.setGuiTile(pos);
        holo.setTag(tag);
        return holo;
    }

    private static double slotX(int slot) {
        return GRID_X + (slot % 9 + .5) * CELL;
    }

    private static double slotY(int slot) {
        return slot < 9 ? HOTBAR_Y + CELL / 2 : PLAYER_Y + ((slot - 9) / 9 + .5) * CELL;
    }

    private static void doubleClick(IGuiComponent<?> root, Player player, HoloGuiEntity holo, double x, double y) {
        root.hit(player, holo, x, y);
        root.hit(player, holo, x, y);
    }
}
