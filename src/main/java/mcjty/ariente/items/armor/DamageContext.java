package mcjty.ariente.items.armor;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public final class DamageContext {

    private final LivingEntity entity;
    private final DamageSource source;
    private float damage;

    public DamageContext(LivingEntity entity, DamageSource source, float damage) {
        this.entity = entity;
        this.source = source;
        this.damage = damage;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public DamageSource getSource() {
        return source;
    }

    public float getNewDamage() {
        return damage;
    }

    public void setNewDamage(float damage) {
        this.damage = Math.max(0, damage);
    }
}
