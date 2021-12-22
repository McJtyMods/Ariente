package mcjty.ariente.items.armor;

import mcjty.ariente.Ariente;
import mcjty.ariente.gui.ModGuis;
import net.minecraft.world.entity.player.Player;

public class PowerArmorConfiguration {

    public static void openConfigurationGui(Player player) {
        Ariente.guiHandler.openHoloGui(player, ModGuis.GUI_ARMOR, 1f);
    }
}
