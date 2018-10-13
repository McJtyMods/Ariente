package mcjty.ariente.items.armor;

import mcjty.ariente.Ariente;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.hologui.api.Icons;
import mcjty.hologui.api.components.IPanel;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.ariente.gui.ModGuis;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.items.modules.ArmorModuleItem;
import mcjty.ariente.items.modules.ModuleSupport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.Pair;

import static mcjty.hologui.api.Icons.*;

public class ArmorGui {

    public static IGuiComponent<?> create(EntityPlayer player) {
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        IPanel panel = registry.panel(0, 0, 8, 8)
                .add(registry.text(0, 1, 1, 1).text("Configure armor").color(0xaaccff));
        double x = 0.5;
        double y = 2.5;
        y = createMenuEntry(registry, player, panel, x, y, EntityEquipmentSlot.HEAD, ModItems.powerSuitHelmet);
        y = createMenuEntry(registry, player, panel, x, y, EntityEquipmentSlot.CHEST, ModItems.powerSuitChest);
        y = createMenuEntry(registry, player, panel, x, y, EntityEquipmentSlot.LEGS, ModItems.powerSuitLegs);
        y = createMenuEntry(registry, player, panel, x, y, EntityEquipmentSlot.FEET, ModItems.powerSuitBoots);
        y = createMenuEntry(registry, player, panel, x, y, EntityEquipmentSlot.MAINHAND, ModItems.enhancedEnergySabreItem);

        if (y <= 2.6) {
            // No armor
            panel.add(registry.text(0, 3, 1, 1).text("No power armor!").color(0xff0000));
        }

        return panel;
    }

    public static IGuiComponent<?> createHelmetGui(EntityPlayer player) {
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        EntityEquipmentSlot slot = EntityEquipmentSlot.HEAD;

        IPanel panel = createPieceGui(registry, slot);

        createModuleEntry(registry, panel, slot, 1, 2, ModItems.moduleEnergy);
        createModuleEntry(registry, panel, slot, 5, 2, ModItems.moduleAutofeed);
        createModuleEntry(registry, panel, slot, 1, 3, ModItems.moduleArmor);
        createModuleEntry(registry, panel, slot, 5, 3, ModItems.moduleNightvision);
        createModuleEntry(registry, panel, slot, 1, 4, ModItems.moduleScramble);
//        createModuleEntry(panel, slot, 5, 4, ModItems.moduleInvisibility);

        addPowerGui(registry, slot, panel);

        return panel;
    }

    public static IGuiComponent<?> createChestGui(EntityPlayer player) {
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        EntityEquipmentSlot slot = EntityEquipmentSlot.CHEST;

        IPanel panel = createPieceGui(registry, slot);

        createModuleEntry(registry, panel, slot, 1, 2, ModItems.moduleEnergy);
        createModuleEntry(registry, panel, slot, 5, 2, ModItems.moduleAutofeed);
        createModuleEntry(registry, panel, slot, 1, 3, ModItems.moduleArmor);
        createModuleEntry(registry, panel, slot, 5, 3, ModItems.moduleForcefield);
        createModuleEntry(registry, panel, slot, 1, 4, ModItems.moduleRegeneration);
        createModuleEntry(registry, panel, slot, 5, 4, ModItems.moduleFlight);

        addPowerGui(registry, slot, panel);

        return panel;
    }

    public static IGuiComponent<?> createLegsGui(EntityPlayer player) {
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        EntityEquipmentSlot slot = EntityEquipmentSlot.LEGS;

        IPanel panel = createPieceGui(registry, slot);

        createModuleEntry(registry, panel, slot, 1, 2, ModItems.moduleEnergy);
        createModuleEntry(registry, panel, slot, 5, 2, ModItems.moduleAutofeed);
        createModuleEntry(registry, panel, slot, 1, 3, ModItems.moduleArmor);
        createModuleEntry(registry, panel, slot, 5, 3, ModItems.moduleSpeed);

        addPowerGui(registry, slot, panel);

        return panel;
    }


    public static IGuiComponent<?> createBootsGui(EntityPlayer player) {
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        EntityEquipmentSlot slot = EntityEquipmentSlot.FEET;

        IPanel panel = createPieceGui(registry, slot);

        createModuleEntry(registry, panel, slot, 1, 2, ModItems.moduleEnergy);
        createModuleEntry(registry, panel, slot, 5, 2, ModItems.moduleAutofeed);
        createModuleEntry(registry, panel, slot, 1, 3, ModItems.moduleArmor);
        createModuleEntry(registry, panel, slot, 5, 3, ModItems.moduleFeatherFalling);
        createModuleEntry(registry, panel, slot, 1, 4, ModItems.moduleStepassist);

        addPowerGui(registry, slot, panel);

        return panel;
    }

    public static IGuiComponent<?> createSabreGui(EntityPlayer player) {
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        EntityEquipmentSlot slot = EntityEquipmentSlot.MAINHAND;

        IPanel panel = createPieceGui(registry, slot);

        createModuleEntry(registry, panel, slot, 1, 2, ModItems.moduleEnergy);
        createModuleEntry(registry, panel, slot, 5, 2, ModItems.moduleAutofeed);
        createModuleEntry(registry, panel, slot, 1, 3, ModItems.modulePower);
        createModuleEntry(registry, panel, slot, 5, 3, ModItems.moduleInhibit);
        createModuleEntry(registry, panel, slot, 1, 4, ModItems.moduleLooting);
        createModuleEntry(registry, panel, slot, 5, 4, ModItems.moduleFire);

        addPowerGui(registry, slot, panel);

        return panel;
    }


    private static IPanel createPieceGui(IGuiComponentRegistry registry, EntityEquipmentSlot slot) {
        IPanel panel = registry.panel(0, 0, 8, 8);
        panel.add(registry.text(0, 0, 1, 1).text("Pwr").color(0xaaccff));
        panel.add(registry.number(3, 0, 1, 1)
                .color(0xffffff)
                .getter((p, h) -> calculatePowerUsage(p, slot))
                .colorGetter(p -> calculatePowerColor(p, slot)));
        panel.add(registry.text(5, 0, 1, 1).text("/").color(0xaaccff));
        panel.add(registry.number(6, 0, 1, 1)
                .color(0xffffff)
                .getter((p, h) -> calculateMaxPowerUsage(p, slot)));
        return panel;
    }

    private static void addPowerGui(IGuiComponentRegistry registry, EntityEquipmentSlot slot, IPanel panel) {
        panel
                .add(registry.stackIcon(0, 6, 1, 1).itemStack(new ItemStack(ModItems.negariteDust)))
                .add(registry.number(1, 6, 1, 1).color(0xffffff).getter((p, h) -> HoloGuiTools.countItem(p, ModItems.negariteDust)))
                .add(registry.iconButton(3, 6, 1, 1).icon(GRAY_ARROW_RIGHT).hover(WHITE_ARROW_RIGHT)
                        .hitEvent((component, p, entity1, x, y) -> toArmor(p, slot, "negarite", ModItems.negariteDust, 1)))
                .add(registry.iconButton(4, 6, 1, 1).icon(GRAY_DOUBLE_ARROW_RIGHT).hover(WHITE_DOUBLE_ARROW_RIGHT)
                        .hitEvent((component, p, entity1, x, y) -> toArmor(p, slot, "negarite", ModItems.negariteDust, 64)))
                .add(registry.number(5, 6, 1, 1).color(0xffffff).getter((p, h) -> countArmor(p, slot, "negarite")))

                .add(registry.stackIcon(0, 7, 1, 1).itemStack(new ItemStack(ModItems.posiriteDust)))
                .add(registry.number(1, 7, 1, 1).color(0xffffff).getter((p, h) -> HoloGuiTools.countItem(p, ModItems.posiriteDust)))
                .add(registry.iconButton(3, 7, 1, 1).icon(GRAY_ARROW_RIGHT).hover(WHITE_ARROW_RIGHT)
                        .hitEvent((component, p, entity1, x, y) -> toArmor(p, slot, "posirite", ModItems.posiriteDust, 1)))
                .add(registry.iconButton(4, 7, 1, 1).icon(GRAY_DOUBLE_ARROW_RIGHT).hover(WHITE_DOUBLE_ARROW_RIGHT)
                        .hitEvent((component, p, entity1, x, y) -> toArmor(p, slot, "posirite", ModItems.posiriteDust, 64)))
                .add(registry.number(5, 7, 1, 1).color(0xffffff).getter((p, h) -> countArmor(p, slot, "posirite")))
        ;
    }

    private static int calculatePowerUsage(EntityPlayer player, EntityEquipmentSlot slot) {
        ItemStack stack = player.getItemStackFromSlot(slot);
        if (isValidPowerArmorPiece(stack)) {
            return 0;
        }
        return ModuleSupport.getPowerUsage(stack).getLeft();
    }

    private static boolean isValidPowerArmorPiece(ItemStack stack) {
        return stack.isEmpty() || (!(stack.getItem() instanceof PowerSuit) && stack.getItem() != ModItems.enhancedEnergySabreItem);
    }

    private static int calculatePowerColor(EntityPlayer player, EntityEquipmentSlot slot) {
        ItemStack stack = player.getItemStackFromSlot(slot);
        if (isValidPowerArmorPiece(stack)) {
            return 0;
        }
        Pair<Integer, Integer> usage = ModuleSupport.getPowerUsage(stack);
        return usage.getLeft() <= usage.getRight() ? 0xffffff : 0xff0000;
    }

    private static int calculateMaxPowerUsage(EntityPlayer player, EntityEquipmentSlot slot) {
        ItemStack stack = player.getItemStackFromSlot(slot);
        if (isValidPowerArmorPiece(stack)) {
            return 0;
        }
        return ModuleSupport.getPowerUsage(stack).getRight();
    }

    private static int countArmor(EntityPlayer player, EntityEquipmentSlot slot, String itemTag) {
        ItemStack stack = player.getItemStackFromSlot(slot);
        if (isValidPowerArmorPiece(stack)) {
            return 0;
        }
        NBTTagCompound compound = stack.getTagCompound();
        if (compound == null) {
            return 0;
        }
        return compound.getInteger(itemTag);
    }

    private static void toArmor(EntityPlayer player, EntityEquipmentSlot slot, String itemTag, Item item, int amount) {
        ItemStack stack = player.getItemStackFromSlot(slot);
        if (isValidPowerArmorPiece(stack)) {
            return;
        }
        NBTTagCompound compound = stack.getTagCompound();
        if (compound == null) {
            compound = new NBTTagCompound();
            stack.setTagCompound(compound);
        }
        int number = compound.getInteger(itemTag);
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack itemStack = player.inventory.getStackInSlot(i);
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
        compound.setInteger(itemTag, number);
    }

    private static void createModuleEntry(IGuiComponentRegistry registry, IPanel panel, EntityEquipmentSlot slot, int xx, int yy, ArmorModuleItem module) {
        panel.add(registry.stackToggle(xx, yy, 1, 1)
                .itemStack(new ItemStack(module))
                .getter(player -> hasModuleAndCheckPlayerToo(player, slot, module))
                .hitEvent((component, player, entity, x, y) -> toggleModuleInstall(player, slot, module)));
        panel.add(registry.iconToggle(xx + 1, yy, 1, 1)
                .getter(player -> isModuleActivated(player, slot, module))
                .icon(128 + 64 + 16, 128 + 16).selected(128 + 64, 128 + 16)
                .hitEvent((component, player, entity, x, y) -> toggleActivation(player, slot, module)));
        panel.add(registry.iconChoice(xx + 2, yy, 1, 1)
                .getter(player -> getHotkey(player, slot, module))
                .icon(BLUE_EMPTY_BUTTON)
                .icon(BLUE_1_BUTTON)
                .icon(BLUE_2_BUTTON)
                .icon(BLUE_3_BUTTON)
                .icon(BLUE_4_BUTTON)
                .hitEvent((component, player, entity, x, y) -> switchHotkey(player, slot, module))
        );
    }

    private static void switchHotkey(EntityPlayer player, EntityEquipmentSlot slot, ArmorModuleItem moduleItem) {
        if (!hasModule(player, slot, moduleItem)) {
            return;
        }
        ItemStack stack = player.getItemStackFromSlot(slot);
        NBTTagCompound compound = stack.getTagCompound();
        int index = compound.getInteger(moduleItem.getType().getHotkeyKey());
        index++;
        if (index >= 5) {
            index = 0;
        }
        compound.setInteger(moduleItem.getType().getHotkeyKey(), index);
    }

    private static Integer getHotkey(EntityPlayer player, EntityEquipmentSlot slot, ArmorModuleItem moduleItem) {
        if (!hasModule(player, slot, moduleItem)) {
            return 0;
        }
        ItemStack stack = player.getItemStackFromSlot(slot);
        NBTTagCompound compound = stack.getTagCompound();
        return compound.getInteger(moduleItem.getType().getHotkeyKey());
    }

    private static Boolean isModuleActivated(EntityPlayer player, EntityEquipmentSlot slot, ArmorModuleItem moduleItem) {
        if (!hasModule(player, slot, moduleItem)) {
            return false;
        }
        ItemStack stack = player.getItemStackFromSlot(slot);
        NBTTagCompound compound = stack.getTagCompound();
        return compound.getBoolean(moduleItem.getType().getModuleKey());
    }

    private static Boolean hasModuleAndCheckPlayerToo(EntityPlayer player, EntityEquipmentSlot slot, ArmorModuleItem moduleItem) {
        if (hasModule(player, slot, moduleItem)) {
            return true;
        }
        int i = findModule(player, slot, moduleItem);
        if (i == -1) {
            return null;    // There is no module in the armor and the player doesn't have one either
        }
        return false;       // Player has a module. It can be installed
    }

    private static Boolean hasModule(EntityPlayer player, EntityEquipmentSlot slot, ArmorModuleItem moduleItem) {
        ItemStack stack = player.getItemStackFromSlot(slot);
        if (stack.isEmpty()) {
            return false;
        }
        if (!stack.hasTagCompound()) {
            return false;
        }
        NBTTagCompound compound = stack.getTagCompound();
        return compound.hasKey(moduleItem.getType().getModuleKey());
    }

    private static void toggleActivation(EntityPlayer player, EntityEquipmentSlot slot, ArmorModuleItem moduleItem) {
        if (!hasModule(player, slot, moduleItem)) {
            return;
        }
        ItemStack stack = player.getItemStackFromSlot(slot);
        NBTTagCompound compound = stack.getTagCompound();
        String key = moduleItem.getType().getModuleKey();
        compound.setBoolean(key, !compound.getBoolean(key));
    }

    private static int findModule(EntityPlayer player, EntityEquipmentSlot slot, ArmorModuleItem moduleItem) {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack moduleStack = player.inventory.getStackInSlot(i);
            if (moduleStack.getItem() == moduleItem) {
                return i;
            }
        }
        return -1;
    }

    private static void toggleModuleInstall(EntityPlayer player, EntityEquipmentSlot slot, ArmorModuleItem moduleItem) {
        if (!hasModule(player, slot, moduleItem)) {
            // Module is not available. Install it if possible
            ItemStack stack = player.getItemStackFromSlot(slot);
            if (stack.isEmpty()) {
                return;
            }

            int i = findModule(player, slot, moduleItem);
            if (i == -1) {
                return;
            }
            ItemStack moduleStack = player.inventory.getStackInSlot(i);
            ItemStack splitted = moduleStack.splitStack(1);
            if (!stack.hasTagCompound()) {
                stack.setTagCompound(new NBTTagCompound());
            }
            NBTTagCompound compound = stack.getTagCompound();
            compound.setBoolean(moduleItem.getType().getModuleKey(), false);
            return;
        } else {
            // Remove installed module
            ItemStack module = new ItemStack(moduleItem);
            if (!player.inventory.addItemStackToInventory(module)) {
                player.entityDropItem(module, 1.05f);
            }
            ItemStack stack = player.getItemStackFromSlot(slot);
            NBTTagCompound compound = stack.getTagCompound();
            String key = moduleItem.getType().getModuleKey();
            compound.removeTag(key);
        }
    }

    private static double createMenuEntry(IGuiComponentRegistry registry, EntityPlayer player, IPanel panel, double x, double y, EntityEquipmentSlot slot, Item armorItem) {
        ItemStack armorStack = player.getItemStackFromSlot(slot);
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

    private static void hitArmorConfigureButton(EntityEquipmentSlot slot, IGuiComponent component, EntityPlayer player, IHoloGuiEntity entity) {
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
