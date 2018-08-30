package mcjty.ariente.entities;

import mcjty.ariente.gui.IGuiComponent;
import mcjty.ariente.gui.components.HoloPanel;
import net.minecraft.entity.player.EntityPlayer;

public class FluxLevitatorGui {

    public static IGuiComponent createGui(EntityPlayer player) {
        return new HoloPanel(0, 0, 8, 8);
    }
}
