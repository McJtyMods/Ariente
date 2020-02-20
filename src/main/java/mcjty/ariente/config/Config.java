package mcjty.ariente.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {

    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    static {
        GeneralConfiguration.init(COMMON_BUILDER, CLIENT_BUILDER);
        DamageConfiguration.init(COMMON_BUILDER, CLIENT_BUILDER);
        PowerConfiguration.init(COMMON_BUILDER, CLIENT_BUILDER);
        UtilityConfiguration.init(COMMON_BUILDER, CLIENT_BUILDER);
        SoundConfiguration.init(COMMON_BUILDER, CLIENT_BUILDER);
        WorldgenConfiguration.init(COMMON_BUILDER, CLIENT_BUILDER);

        COMMON_CONFIG = COMMON_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec CLIENT_CONFIG;


    public static int SHIELD_PANEL_LIFE = 100;
}
