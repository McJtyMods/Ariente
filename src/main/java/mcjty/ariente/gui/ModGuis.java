package mcjty.ariente.gui;

import mcjty.ariente.Ariente;
import mcjty.ariente.entities.levitator.FluxLevitatorGui;
import mcjty.ariente.items.armor.ArmorGui;

public class ModGuis {

    public static final String GUI_ARMOR = "armor";
    public static final String GUI_ARMOR_HELMET = "armor_helmet";
    public static final String GUI_ARMOR_CHEST = "armor_chest";
    public static final String GUI_ARMOR_LEGS = "armor_legs";
    public static final String GUI_ARMOR_BOOTS = "armor_boots";
    public static final String GUI_ARMOR_SABRE = "armor_sabre";
    public static final String GUI_LEVITATOR = "levitator";

    public static void init() {
        Ariente.guiHandler.getGuiRegistry().registerGui(GUI_ARMOR, ArmorGui::create);
        Ariente.guiHandler.getGuiRegistry().registerGui(GUI_ARMOR_HELMET, ArmorGui::createHelmetGui);
        Ariente.guiHandler.getGuiRegistry().registerGui(GUI_ARMOR_CHEST, ArmorGui::createChestGui);
        Ariente.guiHandler.getGuiRegistry().registerGui(GUI_ARMOR_LEGS, ArmorGui::createLegsGui);
        Ariente.guiHandler.getGuiRegistry().registerGui(GUI_ARMOR_BOOTS, ArmorGui::createBootsGui);
        Ariente.guiHandler.getGuiRegistry().registerGui(GUI_ARMOR_SABRE, ArmorGui::createSabreGui);
        Ariente.guiHandler.getGuiRegistry().registerGui(GUI_LEVITATOR, FluxLevitatorGui::createGui);
    }

}
