package mcjty.ariente.items.armor;

import mcjty.ariente.gui.HoloGuiEntity;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.ariente.gui.IGuiComponent;
import mcjty.ariente.gui.ModGuis;
import mcjty.ariente.gui.components.*;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.items.modules.ArmorModuleItem;
import mcjty.ariente.items.modules.ModuleSupport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.Pair;

public class ArmorGui {

    public static IGuiComponent create(EntityPlayer player) {
        HoloPanel panel = new HoloPanel(0, 0, 8, 8)
                .add(new HoloText(0, 1, 1, 1, "Configure armor", 0xaaccff));
        double x = 0.5;
        double y = 2.5;
        y = createMenuEntry(player, panel, x, y, EntityEquipmentSlot.HEAD, ModItems.powerSuitHelmet);
        y = createMenuEntry(player, panel, x, y, EntityEquipmentSlot.CHEST, ModItems.powerSuitChest);
        y = createMenuEntry(player, panel, x, y, EntityEquipmentSlot.LEGS, ModItems.powerSuitLegs);
        y = createMenuEntry(player, panel, x, y, EntityEquipmentSlot.FEET, ModItems.powerSuitBoots);
        y = createMenuEntry(player, panel, x, y, EntityEquipmentSlot.MAINHAND, ModItems.enhancedEnergySabreItem);

        if (y <= 2.6) {
            // No armor
            panel.add(new HoloText(0, 3, 1, 1, "No power armor!", 0xff0000));
        }

        return panel;
    }

    public static IGuiComponent createHelmetGui(EntityPlayer player) {
        EntityEquipmentSlot slot = EntityEquipmentSlot.HEAD;

        HoloPanel panel = createPieceGui(slot);

        createModuleEntry(panel, slot, 1, 2, ModItems.moduleEnergy);
        createModuleEntry(panel, slot, 5, 2, ModItems.moduleAutofeed);
        createModuleEntry(panel, slot, 1, 3, ModItems.moduleArmor);
        createModuleEntry(panel, slot, 5, 3, ModItems.moduleNightvision);
        createModuleEntry(panel, slot, 1, 4, ModItems.moduleScramble);
//        createModuleEntry(panel, slot, 5, 4, ModItems.moduleInvisibility);

        addPowerGui(slot, panel);

        return panel;
    }

    public static IGuiComponent createChestGui(EntityPlayer player) {
        EntityEquipmentSlot slot = EntityEquipmentSlot.CHEST;

        HoloPanel panel = createPieceGui(slot);

        createModuleEntry(panel, slot, 1, 2, ModItems.moduleEnergy);
        createModuleEntry(panel, slot, 5, 2, ModItems.moduleAutofeed);
        createModuleEntry(panel, slot, 1, 3, ModItems.moduleArmor);
        createModuleEntry(panel, slot, 5, 3, ModItems.moduleForcefield);
        createModuleEntry(panel, slot, 1, 4, ModItems.moduleRegeneration);
        createModuleEntry(panel, slot, 5, 4, ModItems.moduleFlight);

        addPowerGui(slot, panel);

        return panel;
    }

    public static IGuiComponent createLegsGui(EntityPlayer player) {
        EntityEquipmentSlot slot = EntityEquipmentSlot.LEGS;

        HoloPanel panel = createPieceGui(slot);

        createModuleEntry(panel, slot, 1, 2, ModItems.moduleEnergy);
        createModuleEntry(panel, slot, 5, 2, ModItems.moduleAutofeed);
        createModuleEntry(panel, slot, 1, 3, ModItems.moduleArmor);
        createModuleEntry(panel, slot, 5, 3, ModItems.moduleSpeed);

        addPowerGui(slot, panel);

        return panel;
    }


    public static IGuiComponent createBootsGui(EntityPlayer player) {
        EntityEquipmentSlot slot = EntityEquipmentSlot.FEET;

        HoloPanel panel = createPieceGui(slot);

        createModuleEntry(panel, slot, 1, 2, ModItems.moduleEnergy);
        createModuleEntry(panel, slot, 5, 2, ModItems.moduleAutofeed);
        createModuleEntry(panel, slot, 1, 3, ModItems.moduleArmor);
        createModuleEntry(panel, slot, 5, 3, ModItems.moduleFeatherFalling);
        createModuleEntry(panel, slot, 1, 4, ModItems.moduleStepassist);

        addPowerGui(slot, panel);

        return panel;
    }

    public static IGuiComponent createSabreGui(EntityPlayer player) {
        EntityEquipmentSlot slot = EntityEquipmentSlot.MAINHAND;

        HoloPanel panel = createPieceGui(slot);

        createModuleEntry(panel, slot, 1, 2, ModItems.moduleEnergy);
        createModuleEntry(panel, slot, 5, 2, ModItems.moduleAutofeed);
        createModuleEntry(panel, slot, 1, 3, ModItems.modulePower);
        createModuleEntry(panel, slot, 5, 3, ModItems.moduleInhibit);
        createModuleEntry(panel, slot, 1, 4, ModItems.moduleLooting);
        createModuleEntry(panel, slot, 5, 4, ModItems.moduleFire);

        addPowerGui(slot, panel);

        return panel;
    }



    private static HoloPanel createPieceGui(EntityEquipmentSlot slot) {
        HoloPanel panel = new HoloPanel(0, 0, 8, 8);
        panel.add(new HoloText(0, 0, 1, 1, "Pwr", 0xaaccff));
        panel.add(new HoloNumber(3, 0, 1, 1, 0xffffff, p -> calculatePowerUsage(p, slot))
            .colorGetter(p -> calculatePowerColor(p, slot)));
        panel.add(new HoloText(5, 0, 1, 1, "/", 0xaaccff));
        panel.add(new HoloNumber(6, 0, 1, 1, 0xffffff, p -> calculateMaxPowerUsage(p, slot)));
        return panel;
    }

    private static void addPowerGui(EntityEquipmentSlot slot, HoloPanel panel) {
        panel
                .add(new HoloItemStack(0, 6, 1, 1, new ItemStack(ModItems.negariteDust)))
                .add(new HoloNumber(1, 6, 1, 1, 0xffffff, p -> HoloGuiTools.countItem(p, ModItems.negariteDust)))
                .add(new HoloButton(3, 6, 1, 1).image(128, 128).hover(128+16, 128)
                        .hitEvent((component, p, entity1, x, y) -> toArmor(p, slot, "negarite", ModItems.negariteDust, 1)))
                .add(new HoloButton(4, 6, 1, 1).image(128, 128+16).hover(128+16, 128+16)
                        .hitEvent((component, p, entity1, x, y) -> toArmor(p, slot, "negarite", ModItems.negariteDust, 64)))
                .add(new HoloNumber(5, 6, 1, 1,0xffffff, p -> countArmor(p, slot, "negarite")))

                .add(new HoloItemStack(0, 7, 1, 1, new ItemStack(ModItems.posiriteDust)))
                .add(new HoloNumber(1, 7, 1, 1, 0xffffff, p -> HoloGuiTools.countItem(p, ModItems.posiriteDust)))
                .add(new HoloButton(3, 7, 1, 1).image(128, 128).hover(128+16, 128)
                        .hitEvent((component, p, entity1, x, y) -> toArmor(p, slot, "posirite", ModItems.posiriteDust, 1)))
                .add(new HoloButton(4, 7, 1, 1).image(128, 128+16).hover(128+16, 128+16)
                        .hitEvent((component, p, entity1, x, y) -> toArmor(p, slot, "posirite", ModItems.posiriteDust, 64)))
                .add(new HoloNumber(5, 7, 1, 1,0xffffff, p -> countArmor(p, slot, "posirite")))
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

    private static void createModuleEntry(HoloPanel panel, EntityEquipmentSlot slot, int xx, int yy, ArmorModuleItem module) {
        panel.add(new HoloItemStackToggle(xx, yy, 1, 1, new ItemStack(module), player -> hasModuleAndCheckPlayerToo(player, slot, module))
                .hitEvent((component, player, entity, x, y) -> toggleModuleInstall(player, slot, module)));
        panel.add(new HoloToggleIcon(xx + 1, yy, 1, 1, player -> isModuleActivated(player, slot, module))
                .image(128 + 64 + 16, 128 + 16).selected(128 + 64, 128 + 16)
                .hitEvent((component, player, entity, x, y) -> toggleActivation(player, slot, module)));
        panel.add(new HoloIcons(xx + 2, yy, 1, 1, player -> getHotkey(player, slot, module))
                .icon(128 + 64 - 16, 128 + 32)
                .icon(128 + 64, 128 + 32)
                .icon(128 + 64 + 16, 128 + 32)
                .icon(128 + 64 + 32, 128 + 32)
                .icon(128 + 64 + 48, 128 + 32)
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

    private static double createMenuEntry(EntityPlayer player, HoloPanel panel, double x, double y, EntityEquipmentSlot slot, Item armorItem) {
        ItemStack armorStack = player.getItemStackFromSlot(slot);
        if (!armorStack.isEmpty() && armorStack.getItem() == armorItem) {
            panel
                    .add(new HoloItemStack(x, y, 1, 1, new ItemStack(armorItem)))
                    .add(new HoloTextButton(2, y, 5, 1, "Configure")
                            .hitEvent((component, player1, entity, x1, y1) -> hitArmorConfigureButton(slot, component, player1, entity)));
            y++;
        }
        return y;
    }

    private static void hitArmorConfigureButton(EntityEquipmentSlot slot, IGuiComponent component, EntityPlayer player, HoloGuiEntity entity) {
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
