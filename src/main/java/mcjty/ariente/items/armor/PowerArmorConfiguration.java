package mcjty.ariente.items.armor;

import mcjty.ariente.Ariente;
import mcjty.ariente.gui.ModGuis;
import net.minecraft.entity.player.EntityPlayer;

public class PowerArmorConfiguration {

    public static void openConfigurationGui(EntityPlayer player) {
        Ariente.guiHandler.openHoloGui(player, ModGuis.GUI_ARMOR, 1f);
    }
}
