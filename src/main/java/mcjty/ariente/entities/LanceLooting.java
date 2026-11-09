package mcjty.ariente.entities;

import net.minecraft.server.level.ServerLevel;

import mcjty.ariente.Ariente;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;

/** Retains the fired lance's Looting level during the synchronous death-loot roll. */
@EventBusSubscriber(modid = Ariente.MODID)
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

    @SubscribeEvent
    public static void enchantmentLevel(GetEnchantmentLevelEvent event) {
        Shot shot = ACTIVE.get();
        if (shot != null && event.getStack() == shot.weapon() && event.isTargetting(Enchantments.LOOTING)) {
            event.getHolder(Enchantments.LOOTING).ifPresent(enchantment ->
                    event.getEnchantments().upgrade(enchantment, shot.level()));
        }
    }

    private record Shot(ItemStack weapon, int level) {
    }
}
