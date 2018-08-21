package mcjty.ariente.items.armor;

import mcjty.ariente.gui.HoloGuiEntity;
import mcjty.ariente.gui.IGuiComponent;
import mcjty.ariente.gui.components.HoloItemStack;
import mcjty.ariente.gui.components.HoloPanel;
import mcjty.ariente.gui.components.HoloText;
import mcjty.ariente.gui.components.HoloTextButton;
import mcjty.ariente.items.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

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
        return null;
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

    }

}
