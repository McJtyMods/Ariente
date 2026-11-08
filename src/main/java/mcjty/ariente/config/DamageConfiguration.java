package mcjty.ariente.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class DamageConfiguration {

    private static final String CATEGORY_DAMAGE = "damage";

    public static ModConfigSpec.DoubleValue FORCEFIELD_DAMAGE;

    public static void init(ModConfigSpec.Builder SERVER_BUILDER, ModConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Damage settings").push(CATEGORY_DAMAGE);

        FORCEFIELD_DAMAGE = SERVER_BUILDER
                .comment("Amount of damage the forcefield does when an entity touches it")
                .defineInRange("forcefieldDamage", 19.0, 0.0, 1000.0);

        SERVER_BUILDER.pop();
    }
}
