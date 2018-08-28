package mcjty.ariente.gui;

import mcjty.ariente.Ariente;
import mcjty.ariente.items.armor.ArmorGui;

public class ModGuis {

    public static final String GUI_ARMOR = "armor";
    public static final String GUI_ARMOR_HELMET = "armor_helmet";
    public static final String GUI_ARMOR_CHEST = "armor_chest";
    public static final String GUI_ARMOR_LEGS = "armor_legs";
    public static final String GUI_ARMOR_BOOTS = "armor_boots";
    public static final String GUI_ARMOR_SABRE = "armor_sabre";

    public static void init() {
        Ariente.instance.guiRegistry.registerGui(GUI_ARMOR, ArmorGui::create);
        Ariente.instance.guiRegistry.registerGui(GUI_ARMOR_HELMET, ArmorGui::createHelmetGui);
        Ariente.instance.guiRegistry.registerGui(GUI_ARMOR_CHEST, ArmorGui::createChestGui);
        Ariente.instance.guiRegistry.registerGui(GUI_ARMOR_LEGS, ArmorGui::createLegsGui);
        Ariente.instance.guiRegistry.registerGui(GUI_ARMOR_BOOTS, ArmorGui::createBootsGui);
        Ariente.instance.guiRegistry.registerGui(GUI_ARMOR_SABRE, ArmorGui::createSabreGui);
    }

}
