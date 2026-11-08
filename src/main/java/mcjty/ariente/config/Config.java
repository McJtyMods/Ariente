package mcjty.ariente.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {

    private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

    static {
        DamageConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        PowerConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        UtilityConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        WorldgenConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        RecipeConfiguration.init(SERVER_BUILDER);
        DisplayConfiguration.init(CLIENT_BUILDER);

        SERVER_CONFIG = SERVER_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

    public static ModConfigSpec SERVER_CONFIG;
    public static ModConfigSpec CLIENT_CONFIG;

    public static int SHIELD_PANEL_LIFE = 100;
}
