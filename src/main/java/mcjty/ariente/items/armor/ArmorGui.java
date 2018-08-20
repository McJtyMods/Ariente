package mcjty.ariente.items.armor;

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
        ItemStack helmet = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        double x = 0.5;
        double y = 2.5;
        if (!helmet.isEmpty() && helmet.getItem() == ModItems.powerSuitHelmet) {
            panel
                    .add(new HoloItemStack(x, y, 1, 1, new ItemStack(ModItems.powerSuitHelmet)))
                    .add(new HoloTextButton(2, y, 5, 1, "Configure"));
            y++;
        }
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (!chest.isEmpty() && chest.getItem() == ModItems.powerSuitChest) {
            panel
                    .add(new HoloItemStack(x, y, 1, 1, new ItemStack(ModItems.powerSuitChest)))
                    .add(new HoloTextButton(2, y, 5, 1, "Configure"));
            y++;
        }
        ItemStack legs = player.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
        if (!legs.isEmpty() && legs.getItem() == ModItems.powerSuitLegs) {
            panel
                    .add(new HoloItemStack(x, y, 1, 1, new ItemStack(ModItems.powerSuitLegs)))
                    .add(new HoloTextButton(2, y, 5, 1, "Configure"));
            y++;
        }
        ItemStack boots = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
        if (!boots.isEmpty() && boots.getItem() == ModItems.powerSuitBoots) {
            panel
                    .add(new HoloItemStack(x, y, 1, 1, new ItemStack(ModItems.powerSuitBoots)))
                    .add(new HoloTextButton(2, y, 5, 1, "Configure"));
            y++;
        }

        if (y <= 2.6) {
            // No armor
            panel.add(new HoloText(0, 3, 1, 1, "No power armor!", 0xff0000));
        }

        return panel;
    }

}
