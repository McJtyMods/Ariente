package mcjty.ariente.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {

    private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    static {
        GeneralConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        DamageConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        PowerConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        UtilityConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        SoundConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        WorldgenConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);

        SERVER_CONFIG = SERVER_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

    public static ForgeConfigSpec SERVER_CONFIG;
    public static ForgeConfigSpec CLIENT_CONFIG;


    public static int SHIELD_PANEL_LIFE = 100;
}
