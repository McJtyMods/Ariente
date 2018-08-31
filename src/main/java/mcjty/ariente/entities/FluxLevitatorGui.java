package mcjty.ariente.entities;

import mcjty.ariente.gui.IGuiComponent;
import mcjty.ariente.gui.components.HoloNumber;
import mcjty.ariente.gui.components.HoloPanel;
import mcjty.ariente.gui.components.HoloText;
import mcjty.ariente.gui.components.HoloTextButton;
import net.minecraft.entity.player.EntityPlayer;

public class FluxLevitatorGui {

    public static IGuiComponent createGui(EntityPlayer player) {
        return new HoloPanel(0, 0, 8, 8)
                .add(new HoloTextButton(1, 1, 4, 1, "Forward"))
                .add(new HoloTextButton(1, 2, 4, 1, "Backward"))
                .add(new HoloText(1, 4, 4, 1, "Speed:", 0xffffff))
                .add(new HoloNumber(5, 4, 1, 1, 0x00ff00, player1 -> 0))
                ;
    }
}
