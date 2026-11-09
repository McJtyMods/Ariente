package mcjty.ariente.entities;

import net.minecraft.server.level.ServerLevel;

import mcjty.ariente.Ariente;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

/** Retains the fired lance's Looting level during the synchronous death-loot roll. */
public final class LanceLooting {

    private static final ThreadLocal<Shot> ACTIVE = new ThreadLocal<>();

    private LanceLooting() {
    }

    public static boolean hurt(Entity target, DamageSource source, float damage, int looting) {
        Shot previous = ACTIVE.get();
        try {
            if (source.getEntity() instanceof LivingEntity owner && looting > 0) {
                ACTIVE.set(new Shot(owner.getMainHandItem(), looting));
            }
            return target.hurtServer((ServerLevel) target.level(), source, damage);
        } finally {
            if (previous == null) {
                ACTIVE.remove();
            } else {
                ACTIVE.set(previous);
            }
        }
    }

    public static int level(ItemStack stack, int original) {
        Shot shot = ACTIVE.get();
        return shot != null && stack == shot.weapon() ? Math.max(original, shot.level()) : original;
    }

    private record Shot(ItemStack weapon, int level) {
    }
}
