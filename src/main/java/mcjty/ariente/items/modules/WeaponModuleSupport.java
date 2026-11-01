package mcjty.ariente.items.modules;

import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.bindings.KeyBindings;
import mcjty.ariente.entities.soldier.MasterSoldierEntity;
import mcjty.lib.builder.TooltipBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.Map;

import static mcjty.lib.builder.TooltipBuilder.*;

public class WeaponModuleSupport {

    public static TooltipBuilder createTooltipBuilder() {
        return new TooltipBuilder()
                .info(header(), key("message.ariente.shiftmessage"))
                .infoShift(header(),
                        parameter("key", stack -> KeyBindings.configureArmor != null, stack -> KeyBindings.configureArmor.getDisplayName()),
                        repeatingParameter("module", stack -> Arrays.stream(ArmorUpgradeType.values())
                                .filter(type -> stack.hasTag() && stack.getTag().contains(type.getModuleKey()))
                                .map(type -> type.getDescription() + (stack.getTag().getBoolean(type.getModuleKey()) ? " (on)" : " (off)"))),
                        parameter("power", stack -> {
                            var usage = ModuleSupport.getPowerUsage(stack);
                            return usage.getLeft() + " / " + usage.getRight();
                        }),
                        key("message.ariente.equipment.shared_power"));
    }

    public static void inventoryTick(ItemStack stack, Level level, Entity entity, boolean usable) {
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            if (usable && living.isAlive() && living.getMainHandItem() == stack) {
                ModuleSupport.updateWorkingState(stack, living);
            } else {
                ModuleSupport.clearWorkingState(stack);
            }
            updateModuleEnchantment(stack, ArmorUpgradeType.LOOTING, Enchantments.MOB_LOOTING);
            updateModuleEnchantment(stack, ArmorUpgradeType.FIRE, Enchantments.FIRE_ASPECT);
        }
    }

    public static void inhibit(LivingEntity target) {
        if (target.level().isClientSide) {
            return;
        }
        if (target instanceof MasterSoldierEntity master) {
            master.setNoregenCounter(200);
        }
        for (MobEffect effect : target.getActiveEffectsMap().keySet().toArray(MobEffect[]::new)) {
            if (effect.isBeneficial()) {
                target.removeEffect(effect);
            }
        }
    }

    private static void updateModuleEnchantment(ItemStack stack, ArmorUpgradeType type, Enchantment enchantment) {
        CompoundTag tag = stack.getOrCreateTag();
        String savedKey = "ariente_original_" + type.getName();
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        if (ModuleSupport.hasWorkingUpgrade(stack, type)) {
            if (!tag.contains(savedKey)) {
                tag.putInt(savedKey, enchantments.getOrDefault(enchantment, 0));
            }
            enchantments.put(enchantment, Math.max(3, tag.getInt(savedKey)));
        } else if (tag.contains(savedKey)) {
            // Restore only this module's enchantment, preserving other sources.
            if (enchantments.getOrDefault(enchantment, 0) == Math.max(3, tag.getInt(savedKey))) {
                if (tag.getInt(savedKey) > 0) {
                    enchantments.put(enchantment, tag.getInt(savedKey));
                } else {
                    enchantments.remove(enchantment);
                }
            }
            tag.remove(savedKey);
        }
        if (!enchantments.equals(EnchantmentHelper.getEnchantments(stack))) {
            EnchantmentHelper.setEnchantments(enchantments, stack);
        }
    }
}
