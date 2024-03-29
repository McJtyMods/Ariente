package mcjty.ariente.items.armor;

import mcjty.ariente.Ariente;
import mcjty.ariente.gui.HelpBuilder;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.ariente.gui.ModGuis;
import mcjty.ariente.items.modules.ArmorModuleItem;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.hologui.api.StyledColor;
import mcjty.hologui.api.components.IPanel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import static mcjty.hologui.api.Icons.*;

public class ArmorGui {

    public static IGuiComponent<?> create(Player player) {
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        IPanel panel = HoloGuiTools.createPanelWithHelp(registry, entity -> entity.switchGui(ModGuis.GUI_ARMOR_HELP))
                .add(registry.text(0, 1, 1, 1).text("Configure armor").color(registry.color(StyledColor.LABEL)));

        double x = 0.5;
        double y = 2.5;
        y = createMenuEntry(registry, player, panel, x, y, EquipmentSlot.HEAD, Registration.POWERSUIT_HEAD.get());
        y = createMenuEntry(registry, player, panel, x, y, EquipmentSlot.CHEST, Registration.POWERSUIT_CHEST.get());
        y = createMenuEntry(registry, player, panel, x, y, EquipmentSlot.LEGS, Registration.POWERSUIT_LEGS.get());
        y = createMenuEntry(registry, player, panel, x, y, EquipmentSlot.FEET, Registration.POWERSUIT_FEET.get());
        y = createMenuEntry(registry, player, panel, x, y, EquipmentSlot.MAINHAND, Registration.ENHANCED_ENERGY_SABRE.get());

        if (y <= 2.6) {
            // No armor
            panel.add(registry.text(0, 3, 1, 1).text("No power armor!").color(registry.color(StyledColor.ERROR)));
        }

        return panel;
    }

    public static IGuiComponent<?> createHelpGui(Player player) {
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        return HoloGuiTools.createHelpGui(registry, HelpBuilder.create()
                .line("With this GUI you can configure")
                .line("your armor and sabre modules")
                .line("For every module you can toggle")
                .line("the module itself to install or")
                .line("deinstall it.")
                .nl()
                .line("Then you can hit the checkbox to")
                .line("actually enable the module")
                .nl()
                .line("Using the last button you can set")
                .line("a hotkey (1 to 4 defined hotkeys)"),
                entity -> entity.switchGui(ModGuis.GUI_ARMOR)
        );
    }

    public static IGuiComponent<?> createHelmetGui(Player player) {
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        EquipmentSlot slot = EquipmentSlot.HEAD;

        IPanel panel = createPieceGui(registry, slot);

        createModuleEntry(registry, panel, slot, 1, 2, Registration.MODULE_ENERGY.get());
        createModuleEntry(registry, panel, slot, 5, 2, Registration.MODULE_AUTOFEED.get());
        createModuleEntry(registry, panel, slot, 1, 3, Registration.MODULE_ARMOR.get());
        createModuleEntry(registry, panel, slot, 5, 3, Registration.MODULE_NIGHTVISION.get());
        createModuleEntry(registry, panel, slot, 1, 4, Registration.MODULE_SCRAMBLE.get());
//        createModuleEntry(panel, slot, 5, 4, ModItems.moduleInvisibility.get());

        addPowerGui(registry, slot, panel);

        return panel;
    }

    public static IGuiComponent<?> createChestGui(Player player) {
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        EquipmentSlot slot = EquipmentSlot.CHEST;

        IPanel panel = createPieceGui(registry, slot);

        createModuleEntry(registry, panel, slot, 1, 2, Registration.MODULE_ENERGY.get());
        createModuleEntry(registry, panel, slot, 5, 2, Registration.MODULE_AUTOFEED.get());
        createModuleEntry(registry, panel, slot, 1, 3, Registration.MODULE_ARMOR.get());
        createModuleEntry(registry, panel, slot, 5, 3, Registration.MODULE_FORCEFIELD.get());
        createModuleEntry(registry, panel, slot, 1, 4, Registration.MODULE_REGENERATION.get());
        createModuleEntry(registry, panel, slot, 5, 4, Registration.MODULE_FLIGHT.get());
        createModuleEntry(registry, panel, slot, 1, 5, Registration.MODULE_HOVER.get());

        addPowerGui(registry, slot, panel);

        return panel;
    }

    public static IGuiComponent<?> createLegsGui(Player player) {
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        EquipmentSlot slot = EquipmentSlot.LEGS;

        IPanel panel = createPieceGui(registry, slot);

        createModuleEntry(registry, panel, slot, 1, 2, Registration.MODULE_ENERGY.get());
        createModuleEntry(registry, panel, slot, 5, 2, Registration.MODULE_AUTOFEED.get());
        createModuleEntry(registry, panel, slot, 1, 3, Registration.MODULE_ARMOR.get());
        createModuleEntry(registry, panel, slot, 5, 3, Registration.MODULE_SPEED.get());

        addPowerGui(registry, slot, panel);

        return panel;
    }


    public static IGuiComponent<?> createBootsGui(Player player) {
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        EquipmentSlot slot = EquipmentSlot.FEET;

        IPanel panel = createPieceGui(registry, slot);

        createModuleEntry(registry, panel, slot, 1, 2, Registration.MODULE_ENERGY.get());
        createModuleEntry(registry, panel, slot, 5, 2, Registration.MODULE_AUTOFEED.get());
        createModuleEntry(registry, panel, slot, 1, 3, Registration.MODULE_ARMOR.get());
        createModuleEntry(registry, panel, slot, 5, 3, Registration.MODULE_FEATHERFALLING.get());
        createModuleEntry(registry, panel, slot, 1, 4, Registration.MODULE_STEPASSIST.get());

        addPowerGui(registry, slot, panel);

        return panel;
    }

    public static IGuiComponent<?> createSabreGui(Player player) {
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        EquipmentSlot slot = EquipmentSlot.MAINHAND;

        IPanel panel = createPieceGui(registry, slot);

        createModuleEntry(registry, panel, slot, 1, 2, Registration.MODULE_ENERGY.get());
        createModuleEntry(registry, panel, slot, 5, 2, Registration.MODULE_AUTOFEED.get());
        createModuleEntry(registry, panel, slot, 1, 3, Registration.MODULE_POWER.get());
        createModuleEntry(registry, panel, slot, 5, 3, Registration.MODULE_INHIBIT.get());
        createModuleEntry(registry, panel, slot, 1, 4, Registration.MODULE_LOOTING.get());
        createModuleEntry(registry, panel, slot, 5, 4, Registration.MODULE_FIRE.get());

        addPowerGui(registry, slot, panel);

        return panel;
    }


    private static IPanel createPieceGui(IGuiComponentRegistry registry, EquipmentSlot slot) {
        IPanel panel = registry.panel(0, 0, 8, 8);
        panel.add(registry.text(0, 0, 1, 1).text("Pwr").color(registry.color(StyledColor.LABEL)));
        panel.add(registry.number(3, 0, 1, 1)
                .color(registry.color(StyledColor.INFORMATION))
                .getter((p, h) -> calculatePowerUsage(p, slot))
                .colorGetter(p -> calculatePowerColor(p, slot)));
        panel.add(registry.text(5, 0, 1, 1).text("/").color(registry.color(StyledColor.LABEL)));
        panel.add(registry.number(6, 0, 1, 1)
                .color(registry.color(StyledColor.INFORMATION))
                .getter((p, h) -> calculateMaxPowerUsage(p, slot)));
        return panel;
    }

    private static void addPowerGui(IGuiComponentRegistry registry, EquipmentSlot slot, IPanel panel) {
        panel
                .add(registry.stackIcon(0, 6, 1, 1).itemStack(new ItemStack(Registration.DUST_NEGARITE.get())))
                .add(registry.number(1, 6, 1, 1).color(registry.color(StyledColor.INFORMATION)).getter((p, h) -> HoloGuiTools.countItem(p, Registration.DUST_NEGARITE.get())))
                .add(registry.iconButton(3, 6, 1, 1).icon(registry.image(GRAY_ARROW_RIGHT)).hover(registry.image(WHITE_ARROW_RIGHT))
                        .hitEvent((component, p, entity1, x, y) -> toArmor(p, slot, "negarite", Registration.DUST_NEGARITE.get(), 1)))
                .add(registry.iconButton(4, 6, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_RIGHT)).hover(registry.image(WHITE_DOUBLE_ARROW_RIGHT))
                        .hitEvent((component, p, entity1, x, y) -> toArmor(p, slot, "negarite", Registration.DUST_NEGARITE.get(), 64)))
                .add(registry.number(5, 6, 1, 1).color(registry.color(StyledColor.INFORMATION)).getter((p, h) -> countArmor(p, slot, "negarite")))

                .add(registry.stackIcon(0, 7, 1, 1).itemStack(new ItemStack(Registration.DUST_POSIRITE.get())))
                .add(registry.number(1, 7, 1, 1).color(registry.color(StyledColor.INFORMATION)).getter((p, h) -> HoloGuiTools.countItem(p, Registration.DUST_POSIRITE.get())))
                .add(registry.iconButton(3, 7, 1, 1).icon(registry.image(GRAY_ARROW_RIGHT)).hover(registry.image(WHITE_ARROW_RIGHT))
                        .hitEvent((component, p, entity1, x, y) -> toArmor(p, slot, "posirite", Registration.DUST_POSIRITE.get(), 1)))
                .add(registry.iconButton(4, 7, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_RIGHT)).hover(registry.image(WHITE_DOUBLE_ARROW_RIGHT))
                        .hitEvent((component, p, entity1, x, y) -> toArmor(p, slot, "posirite", Registration.DUST_POSIRITE.get(), 64)))
                .add(registry.number(5, 7, 1, 1).color(registry.color(StyledColor.INFORMATION)).getter((p, h) -> countArmor(p, slot, "posirite")))
        ;
    }

    private static int calculatePowerUsage(Player player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        if (isValidPowerArmorPiece(stack)) {
            return 0;
        }
        return ModuleSupport.getPowerUsage(stack).getLeft();
    }

    private static boolean isValidPowerArmorPiece(ItemStack stack) {
        return stack.isEmpty() || (!(stack.getItem() instanceof PowerSuit) && stack.getItem() != Registration.ENHANCED_ENERGY_SABRE.get());
    }

    private static int calculatePowerColor(Player player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        if (isValidPowerArmorPiece(stack)) {
            return 0;
        }
        Pair<Integer, Integer> usage = ModuleSupport.getPowerUsage(stack);
        return usage.getLeft() <= usage.getRight() ? 0xffffff : 0xff0000;
    }

    private static int calculateMaxPowerUsage(Player player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        if (isValidPowerArmorPiece(stack)) {
            return 0;
        }
        return ModuleSupport.getPowerUsage(stack).getRight();
    }

    private static int countArmor(Player player, EquipmentSlot slot, String itemTag) {
        ItemStack stack = player.getItemBySlot(slot);
        if (isValidPowerArmorPiece(stack)) {
            return 0;
        }
        CompoundTag compound = stack.getTag();
        if (compound == null) {
            return 0;
        }
        return compound.getInt(itemTag);
    }

    private static void toArmor(Player player, EquipmentSlot slot, String itemTag, Item item, int amount) {
        ItemStack stack = player.getItemBySlot(slot);
        if (isValidPowerArmorPiece(stack)) {
            return;
        }
        CompoundTag compound = stack.getOrCreateTag();
        int number = compound.getInt(itemTag);
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack itemStack = player.getInventory().getItem(i);
            if (itemStack.getItem() == item) {
                int n = Math.min(amount, itemStack.getCount());
                itemStack.shrink(n);
                number += n;
                amount -= n;
                if (amount <= 0) {
                    break;
                }
            }
        }
        compound.putInt(itemTag, number);
    }

    private static void createModuleEntry(IGuiComponentRegistry registry, IPanel panel, EquipmentSlot slot, int xx, int yy, ArmorModuleItem module) {
        panel.add(registry.stackToggle(xx, yy-.2, 1, 1)
                .itemStack(new ItemStack(module))
                .getter(player -> hasModuleAndCheckPlayerToo(player, slot, module))
                .hitEvent((component, player, entity, x, y) -> toggleModuleInstall(player, slot, module)));
        panel.add(registry.iconToggle(xx + 1, yy-.2, 1, 1)
                .getter(player -> isModuleActivated(player, slot, module))
                .icon(registry.image(128 + 64 + 16, 128 + 16)).selected(registry.image(128 + 64, 128 + 16))
                .hitEvent((component, player, entity, x, y) -> toggleActivation(player, slot, module)));
        panel.add(registry.iconChoice(xx + 2, yy-.2, 1, 1)
                .getter(player -> getHotkey(player, slot, module))
                .addImage(registry.image(BLUE_EMPTY_BUTTON))
                .addImage(registry.image(BLUE_1_BUTTON))
                .addImage(registry.image(BLUE_2_BUTTON))
                .addImage(registry.image(BLUE_3_BUTTON))
                .addImage(registry.image(BLUE_4_BUTTON))
                .hitEvent((component, player, entity, x, y) -> switchHotkey(player, slot, module))
        );
    }

    private static void switchHotkey(Player player, EquipmentSlot slot, ArmorModuleItem moduleItem) {
        if (!hasModule(player, slot, moduleItem)) {
            return;
        }
        ItemStack stack = player.getItemBySlot(slot);
        CompoundTag compound = stack.getTag();
        int index = compound.getInt(moduleItem.getType().getHotkeyKey());
        index++;
        if (index >= 5) {
            index = 0;
        }
        compound.putInt(moduleItem.getType().getHotkeyKey(), index);
    }

    private static Integer getHotkey(Player player, EquipmentSlot slot, ArmorModuleItem moduleItem) {
        if (!hasModule(player, slot, moduleItem)) {
            return 0;
        }
        ItemStack stack = player.getItemBySlot(slot);
        CompoundTag compound = stack.getTag();
        return compound.getInt(moduleItem.getType().getHotkeyKey());
    }

    private static Boolean isModuleActivated(Player player, EquipmentSlot slot, ArmorModuleItem moduleItem) {
        if (!hasModule(player, slot, moduleItem)) {
            return false;
        }
        ItemStack stack = player.getItemBySlot(slot);
        CompoundTag compound = stack.getTag();
        return compound.getBoolean(moduleItem.getType().getModuleKey());
    }

    private static Boolean hasModuleAndCheckPlayerToo(Player player, EquipmentSlot slot, ArmorModuleItem moduleItem) {
        if (hasModule(player, slot, moduleItem)) {
            return true;
        }
        int i = findModule(player, slot, moduleItem);
        if (i == -1) {
            return null;    // There is no module in the armor and the player doesn't have one either
        }
        return false;       // Player has a module. It can be installed
    }

    private static Boolean hasModule(Player player, EquipmentSlot slot, ArmorModuleItem moduleItem) {
        ItemStack stack = player.getItemBySlot(slot);
        if (stack.isEmpty()) {
            return false;
        }
        if (!stack.hasTag()) {
            return false;
        }
        CompoundTag compound = stack.getTag();
        return compound.contains(moduleItem.getType().getModuleKey());
    }

    private static void toggleActivation(Player player, EquipmentSlot slot, ArmorModuleItem moduleItem) {
        if (!hasModule(player, slot, moduleItem)) {
            return;
        }
        ItemStack stack = player.getItemBySlot(slot);
        CompoundTag compound = stack.getTag();
        String key = moduleItem.getType().getModuleKey();
        compound.putBoolean(key, !compound.getBoolean(key));
    }

    private static int findModule(Player player, EquipmentSlot slot, ArmorModuleItem moduleItem) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack moduleStack = player.getInventory().getItem(i);
            if (moduleStack.getItem() == moduleItem) {
                return i;
            }
        }
        return -1;
    }

    private static void toggleModuleInstall(Player player, EquipmentSlot slot, ArmorModuleItem moduleItem) {
        if (!hasModule(player, slot, moduleItem)) {
            // Module is not available. Install it if possible
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) {
                return;
            }

            int i = findModule(player, slot, moduleItem);
            if (i == -1) {
                return;
            }
            ItemStack moduleStack = player.getInventory().getItem(i);
            ItemStack splitted = moduleStack.split(1);
            CompoundTag compound = stack.getOrCreateTag();
            compound.putBoolean(moduleItem.getType().getModuleKey(), false);
            return;
        } else {
            // Remove installed module
            ItemStack module = new ItemStack(moduleItem);
            if (!player.getInventory().add(module)) {
                player.spawnAtLocation(module, 1.05f);
            }
            ItemStack stack = player.getItemBySlot(slot);
            CompoundTag compound = stack.getTag();
            String key = moduleItem.getType().getModuleKey();
            compound.remove(key);
        }
    }

    private static double createMenuEntry(IGuiComponentRegistry registry, Player player, IPanel panel, double x, double y, EquipmentSlot slot, Item armorItem) {
        ItemStack armorStack = player.getItemBySlot(slot);
        if (!armorStack.isEmpty() && armorStack.getItem() == armorItem) {
            panel
                    .add(registry.stackIcon(x, y, 1, 1).itemStack(new ItemStack(armorItem)))
                    .add(registry.button(2, y, 5, 1)
                            .text("Configure")
                            .hitEvent((component, player1, entity, x1, y1) -> hitArmorConfigureButton(slot, component, player1, entity)));
            y++;
        }
        return y;
    }

    private static void hitArmorConfigureButton(EquipmentSlot slot, IGuiComponent component, Player player, IHoloGuiEntity entity) {
        switch (slot) {
            case FEET:
                entity.switchGui(ModGuis.GUI_ARMOR_BOOTS);
                break;
            case LEGS:
                entity.switchGui(ModGuis.GUI_ARMOR_LEGS);
                break;
            case CHEST:
                entity.switchGui(ModGuis.GUI_ARMOR_CHEST);
                break;
            case HEAD:
                entity.switchGui(ModGuis.GUI_ARMOR_HELMET);
                break;
            case MAINHAND:
                entity.switchGui(ModGuis.GUI_ARMOR_SABRE);
                break;
            default:
                break;
        }
    }

}
