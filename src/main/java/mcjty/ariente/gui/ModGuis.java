package mcjty.ariente.gui;

import mcjty.ariente.Ariente;
import mcjty.ariente.items.armor.ArmorGui;

public class ModGuis {

    public static final String GUI_ARMOR = "armor";

    public static void init() {
        Ariente.instance.guiRegistry.registerGui(GUI_ARMOR, ArmorGui::create);
    }

}
