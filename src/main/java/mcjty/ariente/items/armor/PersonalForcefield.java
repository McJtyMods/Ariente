package mcjty.ariente.items.armor;

import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.network.Packets;
import mcjty.ariente.setup.Registration;
import mcjty.lib.varia.PersistentData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;

public class PersonalForcefield {

    private static final String COOLDOWN_UNTIL = "arienteForcefieldCooldownUntil";
    private static final String LAST_HIT = "arienteForcefieldLastHit";
    private static final int MIN_COOLDOWN_TICKS = 10;
    private static final float TICKS_PER_DAMAGE = 40.0f / 24.0f;

    public static boolean isActive(LivingEntity entity) {
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        return entity.isAlive() && !entity.isSpectator() && chest.is(Registration.POWERSUIT_CHEST.get())
                && ModuleSupport.hasWorkingUpgrade(chest, ArmorUpgradeType.FORCEFIELD);
    }

    public static long getCooldownUntil(LivingEntity entity) {
        return PersistentData.get(entity).getLong(COOLDOWN_UNTIL);
    }

    public static long getLastHit(LivingEntity entity) {
        return PersistentData.get(entity).getLong(LAST_HIT);
    }

    public static boolean isCoolingDown(LivingEntity entity) {
        return getCooldownUntil(entity) > entity.level().getGameTime();
    }

    public static void updateState(LivingEntity entity, long cooldownUntil, long lastHit) {
        CompoundTag tag = PersistentData.get(entity);
        tag.putLong(COOLDOWN_UNTIL, cooldownUntil);
        tag.putLong(LAST_HIT, lastHit);
    }

    public static void absorb(DamageContext event) {
        LivingEntity entity = event.getEntity();
        float damage = event.getNewDamage();
        if (entity.level().isClientSide || damage <= 0 || !isActive(entity)) {
            return;
        }
        DamageSource source = event.getSource();
        boolean coolingDown = isCoolingDown(entity);
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            absorbRanged(event, damage, coolingDown ? 0.4f : 0.8f);
        } else if (source.is(DamageTypeTags.IS_PROJECTILE) || source.getDirectEntity() instanceof Projectile
                || source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC)) {
            absorbRanged(event, damage, coolingDown ? 0.5f : 1.0f);
        } else if (!source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            event.setNewDamage(damage * 0.5f);
            if (!source.is(DamageTypes.THORNS) && source.getDirectEntity() instanceof LivingEntity attacker
                    && source.getEntity() == attacker && attacker != entity) {
                attacker.hurt(entity.damageSources().thorns(entity), damage * 0.5f);
            }
        }
    }

    private static void absorbRanged(DamageContext event, float damage, float protection) {
        LivingEntity entity = event.getEntity();
        float absorbed = damage * protection;
        event.setNewDamage(damage - absorbed);
        // A blocked 24-damage advanced drone laser needs two seconds; ordinary arrows need half a second.
        int cooldown = Math.max(MIN_COOLDOWN_TICKS, Mth.ceil(absorbed * TICKS_PER_DAMAGE));
        long now = entity.level().getGameTime();
        updateState(entity, now + cooldown, now);
        Packets.sendToPlayersTrackingEntityAndSelf(entity,
                new PacketPersonalForcefield(entity.getId(), now + cooldown, now));
    }
}
