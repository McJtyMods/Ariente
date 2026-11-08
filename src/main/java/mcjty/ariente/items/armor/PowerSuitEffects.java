package mcjty.ariente.items.armor;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Tracks effects supplied by the suit without removing a potion supplied by another source. */
public class PowerSuitEffects {

    private static final Map<LivingEntity, Map<Holder<MobEffect>, OwnedEffect>> EFFECTS = new WeakHashMap<>();

    public static void tick(LivingEntity entity) {
        update(entity, EquipmentSlot.HEAD, ArmorUpgradeType.NIGHTVISION, MobEffects.NIGHT_VISION, 400, 1);
        update(entity, EquipmentSlot.HEAD, ArmorUpgradeType.INVISIBILITY, MobEffects.INVISIBILITY, 200, 1);
        update(entity, EquipmentSlot.CHEST, ArmorUpgradeType.REGENERATION, MobEffects.REGENERATION, 100, 2);
    }

    private static void update(LivingEntity entity, EquipmentSlot slot, ArmorUpgradeType type, Holder<MobEffect> effect, int duration, int amplifier) {
        ItemStack stack = entity.getItemBySlot(slot);
        boolean active = entity.isAlive() && stack.is(slot == EquipmentSlot.HEAD
                ? Registration.POWERSUIT_HEAD.get() : Registration.POWERSUIT_CHEST.get())
                && ModuleSupport.hasWorkingUpgrade(stack, type);
        Map<Holder<MobEffect>, OwnedEffect> owned = EFFECTS.computeIfAbsent(entity, e -> new HashMap<>());
        OwnedEffect previous = owned.get(effect);
        MobEffectInstance current = entity.getEffect(effect);
        if (previous != null && !previous.matches(current)) {
            owned.remove(effect);
            previous = null;
        }
        if (!active) {
            if (previous != null) {
                removeOwnedEffect(entity, effect);
                owned.remove(effect);
            }
        } else if (current == null || previous != null) {
            if (current == null || current.getDuration() <= duration / 2) {
                entity.addEffect(new MobEffectInstance(effect, duration, amplifier, false, false, false));
                current = entity.getEffect(effect);
            }
            if (current != null) {
                owned.put(effect, new OwnedEffect(current, current.getDuration(), amplifier));
            }
        }
        if (owned.isEmpty()) {
            EFFECTS.remove(entity);
        }
    }

    public static void clear(LivingEntity entity) {
        Map<Holder<MobEffect>, OwnedEffect> owned = EFFECTS.remove(entity);
        if (owned != null) {
            owned.forEach((effect, previous) -> {
                if (previous.matches(entity.getEffect(effect))) {
                    removeOwnedEffect(entity, effect);
                }
            });
        }
    }

    private static void removeOwnedEffect(LivingEntity entity, Holder<MobEffect> effect) {
        // Vanilla keeps weaker effects in a hidden chain. Preserve that chain when our boost stops.
        CompoundTag tag = (CompoundTag) entity.getEffect(effect).save();
        entity.removeEffect(effect);
        if (tag.contains("hidden_effect")) {
            MobEffectInstance hidden = MobEffectInstance.load(tag.getCompound("hidden_effect"));
            if (hidden != null && hidden.getDuration() > 0) {
                entity.addEffect(hidden);
            }
        }
    }

    private record OwnedEffect(MobEffectInstance instance, int duration, int amplifier) {

        private boolean matches(MobEffectInstance current) {
            return current == instance && current.getAmplifier() == amplifier && current.getDuration() <= duration
                    && !current.isAmbient() && !current.isVisible() && !current.showIcon();
        }
    }
}
