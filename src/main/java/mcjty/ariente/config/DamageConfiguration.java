package mcjty.ariente.config;

import net.minecraftforge.common.config.Configuration;

public class DamageConfiguration {

    private static final String CATEGORY_DAMAGE = "damage";

    public static float FORCEFIELD_DAMAGE = 19.0f;

    public static void init(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_DAMAGE, "Damage settings");

        FORCEFIELD_DAMAGE = cfg.getFloat("forcefieldDamage", CATEGORY_DAMAGE, FORCEFIELD_DAMAGE, 0.0f, 1000.0f, "Amount of damage the forcefield does when an entity touches it");
    }
}
