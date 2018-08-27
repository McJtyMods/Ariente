package mcjty.ariente.config;

import mcjty.ariente.Ariente;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;

public class ArienteModConfigGui extends GuiConfig {

    public ArienteModConfigGui(GuiScreen parentScreen) {
        super(parentScreen, new ConfigElement(Ariente.proxy.getConfig().getCategory(GuiConfiguration.CATEGORY_GUI)).getChildElements(),
                Ariente.MODID, false, false, "Ariente Config");
    }
}
