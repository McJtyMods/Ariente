package mcjty.ariente.gui;

import mcjty.ariente.Ariente;
import mcjty.ariente.entities.levitator.FluxLevitatorGui;
import mcjty.ariente.items.EnergyHolderItem;
import mcjty.ariente.items.armor.ArmorGui;

public class ModGuis {

    public static final String GUI_ARMOR = "ariente.armor";
    public static final String GUI_ARMOR_HELMET = "ariente.armor_helmet";
    public static final String GUI_ARMOR_CHEST = "ariente.armor_chest";
    public static final String GUI_ARMOR_LEGS = "ariente.armor_legs";
    public static final String GUI_ARMOR_BOOTS = "ariente.armor_boots";
    public static final String GUI_ARMOR_SABRE = "ariente.armor_sabre";
    public static final String GUI_ARMOR_HELP = "ariente.armor_help";
    public static final String GUI_LEVITATOR = "ariente.levitator";
    public static final String GUI_ENERGY_HOLDER = "ariente.energy_holder";
    public static final String GUI_ENERGY_HOLDER_HOLD = "ariente.energy_holder_help";

    public static void init() {
        Ariente.guiHandler.getGuiRegistry().registerGui(GUI_ARMOR, ArmorGui::create);
        Ariente.guiHandler.getGuiRegistry().registerGui(GUI_ARMOR_HELMET, ArmorGui::createHelmetGui);
        Ariente.guiHandler.getGuiRegistry().registerGui(GUI_ARMOR_CHEST, ArmorGui::createChestGui);
        Ariente.guiHandler.getGuiRegistry().registerGui(GUI_ARMOR_LEGS, ArmorGui::createLegsGui);
        Ariente.guiHandler.getGuiRegistry().registerGui(GUI_ARMOR_BOOTS, ArmorGui::createBootsGui);
        Ariente.guiHandler.getGuiRegistry().registerGui(GUI_ARMOR_SABRE, ArmorGui::createSabreGui);
        Ariente.guiHandler.getGuiRegistry().registerGui(GUI_ARMOR_HELP, ArmorGui::createHelpGui);
        Ariente.guiHandler.getGuiRegistry().registerGui(GUI_LEVITATOR, FluxLevitatorGui::createGui);
        Ariente.guiHandler.getGuiRegistry().registerGui(GUI_ENERGY_HOLDER, EnergyHolderItem::createGui);
        Ariente.guiHandler.getGuiRegistry().registerGui(GUI_ENERGY_HOLDER_HOLD, EnergyHolderItem::createHelpGui);
    }

}
