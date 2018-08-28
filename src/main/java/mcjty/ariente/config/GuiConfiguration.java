package mcjty.ariente.config;

import net.minecraftforge.common.config.Configuration;

public class GuiConfiguration {

    public static final String CATEGORY_GUI = "gui";

    public static int GUI_STYLE = 3;

    public static void init(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_GUI, "GUI settings");

        GUI_STYLE = cfg.getInt("guiStyle", CATEGORY_GUI, GUI_STYLE, 1, 16, "The gui style (1-8 are transparent, 9-16 are opaque)");
    }
}
