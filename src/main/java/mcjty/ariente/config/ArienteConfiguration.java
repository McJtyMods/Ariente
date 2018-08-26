package mcjty.ariente.config;

import net.minecraftforge.common.config.Configuration;

public class ArienteConfiguration {

    public static String[] ASSETS = new String[] {
            "/assets/ariente/citydata/city0_1.json",
            "/assets/ariente/citydata/city1_1.json",
            "/assets/ariente/citydata/city1_2.json",
            "/assets/ariente/citydata/city2_1.json",
            "/assets/ariente/citydata/general.json",
            "$ariente/userassets.json"
    };

    public static int SHIELD_PANEL_LIFE = 100;

    public static void init(Configuration cfg) {
        GeneralConfiguration.init(cfg);
        AIConfiguration.init(cfg);
        DamageConfiguration.init(cfg);
        PowerConfiguration.init(cfg);
        UtilityConfiguration.init(cfg);
        WorldgenConfiguration.init(cfg);
        LootConfiguration.init(cfg);
        SoundConfiguration.init(cfg);
    }

}
