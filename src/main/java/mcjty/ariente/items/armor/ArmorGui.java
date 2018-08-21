package mcjty.ariente.items.armor;

import mcjty.ariente.gui.HoloGuiEntity;
import mcjty.ariente.gui.IGuiComponent;
import mcjty.ariente.gui.ModGuis;
import mcjty.ariente.gui.components.*;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.items.modules.ArmorModuleItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

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

        if (y <= 2.6) {
            // No armor
            panel.add(new HoloText(0, 3, 1, 1, "No power armor!", 0xff0000));
        }

        return panel;
    }

    public static IGuiComponent createHelmetGui(EntityPlayer player) {
        // Modules:
        //   - Energy Optimizer
        //   - Increase Armor
        //   - Night vision
        //   - Invisibility
        HoloPanel panel = new HoloPanel(0, 0, 8, 8);
        panel.add(new HoloText(0, 0, 1, 1, "Pwr", 0xaaccff));
        panel.add(new HoloNumber(3, 0, 1, 1, 0xffffff, () -> 30));
        panel.add(new HoloText(5, 0, 1, 1, "/", 0xaaccff));
        panel.add(new HoloNumber(6, 0, 1, 1, 0xffffff, () -> 200));

        EntityEquipmentSlot slot = EntityEquipmentSlot.HEAD;
        createModuleEntry(panel, slot, 1, 2, ModItems.moduleEnergy);
        createModuleEntry(panel, slot, 4, 2, ModItems.moduleArmor);
        createModuleEntry(panel, slot, 1, 3, ModItems.moduleNightvision);
        createModuleEntry(panel, slot, 4, 3, ModItems.moduleInvisibility);
        createModuleEntry(panel, slot, 1, 4, ModItems.moduleScramble);
        return panel;
    }

    private static void createModuleEntry(HoloPanel panel, EntityEquipmentSlot slot, int xx, int yy, ArmorModuleItem module) {
        panel.add(new HoloItemStackButton(xx, yy, 1, 1, new ItemStack(module), player -> hasModuleAndCheckPlayerToo(player, slot, module))
                .hitEvent((component, player, entity, x, y) -> toggleModuleInstall(player, slot, module)));
        panel.add(new HoloToggleIcon(xx + 1, yy, 1, 1, player -> isModuleActivated(player, slot, module))
                .image(128 + 64 + 16, 128 + 16).selected(128 + 64, 128 + 16)
                .hitEvent((component, player, entity, x, y) -> toggleActivation(player, slot, module)));
    }

    private static Boolean isModuleActivated(EntityPlayer player, EntityEquipmentSlot slot, ArmorModuleItem moduleItem) {
        if (!hasModule(player, slot, moduleItem)) {
            return false;
        }
        ItemStack stack = player.getItemStackFromSlot(slot);
        NBTTagCompound compound = stack.getTagCompound();
        return compound.getBoolean(getModuleKey(moduleItem));
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
        return compound.hasKey(getModuleKey(moduleItem));
    }

    private static void toggleActivation(EntityPlayer player, EntityEquipmentSlot slot, ArmorModuleItem moduleItem) {
        if (!hasModule(player, slot, moduleItem)) {
            return;
        }
        ItemStack stack = player.getItemStackFromSlot(slot);
        NBTTagCompound compound = stack.getTagCompound();
        String key = getModuleKey(moduleItem);
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
            compound.setBoolean(getModuleKey(moduleItem), false);
            return;
        } else {
            // Remove installed module
            ItemStack module = new ItemStack(moduleItem);
            if (!player.inventory.addItemStackToInventory(module)) {
                player.entityDropItem(module, 1.05f);
            }
            ItemStack stack = player.getItemStackFromSlot(slot);
            NBTTagCompound compound = stack.getTagCompound();
            String key = getModuleKey(moduleItem);
            compound.removeTag(key);
        }
    }

    private static String getModuleKey(ArmorModuleItem moduleItem) {
        return "module_" + moduleItem.getType().getName();
    }

    public static IGuiComponent createChestGui(EntityPlayer player) {
        // Modules:
        //   - Energy Optimizer
        //   - Increase Armor
        //   - Forcefield (requires full suit)
        //   - Regeneration
        //   - Flight
        return null;
    }

    public static IGuiComponent createLegsGui(EntityPlayer player) {
        // Modules:
        //   - Energy Optimizer
        //   - Increase Armor
        //   - Speed
        return null;
    }

    public static IGuiComponent createBootsGui(EntityPlayer player) {
        // Modules:
        //   - Energy Optimizer
        //   - Increase Armor
        //   - Feather falling
        return null;
    }


    private static double createMenuEntry(EntityPlayer player, HoloPanel panel, double x, double y, EntityEquipmentSlot slot, PowerSuit armorItem) {
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
                break;
            case LEGS:
                break;
            case CHEST:
                break;
            case HEAD:
                entity.switchGui(ModGuis.GUI_ARMOR_HELMET);
                break;
            default:
                break;
        }
    }

}
