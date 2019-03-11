package mcjty.ariente.config;

import mcjty.ariente.Ariente;
import mcjty.lib.thirteen.ConfigSpec;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class ConfigSetup {

    private static final ConfigSpec.Builder SERVER_BUILDER = new ConfigSpec.Builder();
    private static final ConfigSpec.Builder CLIENT_BUILDER = new ConfigSpec.Builder();

    static {
        GeneralConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        AIConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        DamageConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        PowerConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        UtilityConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        WorldgenConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        LootConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        SoundConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
    }

    public static ConfigSpec SERVER_CONFIG;
    public static ConfigSpec CLIENT_CONFIG;


    public static String[] ASSETS = new String[] {
            "/assets/ariente/citydata/city0_0.json",
            "/assets/ariente/citydata/city0_1.json",
            "/assets/ariente/citydata/city1_1.json",
            "/assets/ariente/citydata/city1_2.json",
            "/assets/ariente/citydata/city2_1.json",
            "/assets/ariente/citydata/city3_1.json",
            "/assets/ariente/citydata/city4_1.json",
            "/assets/ariente/citydata/general.json",
            "/assets/ariente/citydata/station.json",
            "$ariente/userassets.json"
    };

    public static int SHIELD_PANEL_LIFE = 100;

    public static Configuration mainConfig;

    public static void init() {
        mainConfig = new Configuration(new File(Ariente.setup.getModConfigDir().getPath(), "ariente.cfg"));
        SERVER_CONFIG = SERVER_BUILDER.build(mainConfig);
        CLIENT_CONFIG = CLIENT_BUILDER.build(mainConfig);
    }

    public static void postInit() {
        if (mainConfig.hasChanged()) {
            mainConfig.save();
        }
    }
}
