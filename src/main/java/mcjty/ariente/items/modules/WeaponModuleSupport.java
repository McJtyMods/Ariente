package mcjty.ariente.items.modules;

import java.util.Arrays;
import java.util.List;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.api.IInhibitable;
import mcjty.ariente.bindings.KeyBindings;
import mcjty.ariente.items.ItemData;
import mcjty.lib.builder.TooltipBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import static mcjty.lib.builder.TooltipBuilder.*;

public class WeaponModuleSupport {

    public static TooltipBuilder createTooltipBuilder() {
        return new TooltipBuilder()
                .info(header(), key("message.ariente.shiftmessage"))
                .infoShift(header(),
                        parameter("key", stack -> KeyBindings.configureArmor != null, stack -> KeyBindings.configureArmor.getDisplayName()),
                        repeatingParameter("module", stack -> Arrays.stream(ArmorUpgradeType.values())
                                .filter(type -> ItemData.has(stack) && ItemData.get(stack).contains(type.getModuleKey()))
                                .map(type -> type.getDescription() + (ItemData.get(stack).getBooleanOr(type.getModuleKey(), false) ? " (on)" : " (off)"))),
                        parameter("power", stack -> {
                            var usage = ModuleSupport.getPowerUsage(stack);
                            return usage.getLeft() + " / " + usage.getRight();
                        }),
                        key("message.ariente.equipment.shared_power"));
    }

    public static void inventoryTick(ItemStack stack, Level level, Entity entity, boolean usable) {
        if (!level.isClientSide() && entity instanceof LivingEntity living) {
            if (usable && living.isAlive() && living.getMainHandItem() == stack) {
                ModuleSupport.updateWorkingState(stack, living);
            } else {
                ModuleSupport.clearWorkingState(stack);
            }
            updateModuleEnchantment(level, stack, ArmorUpgradeType.LOOTING, Enchantments.LOOTING);
            updateModuleEnchantment(level, stack, ArmorUpgradeType.FIRE, Enchantments.FIRE_ASPECT);
        }
    }

    public static void inhibit(LivingEntity target) {
        if (target.level().isClientSide()) {
            return;
        }
        if (target instanceof IInhibitable inhibitable) {
            inhibitable.inhibitRegeneration(200);
        }
        for (Holder<MobEffect> effect : List.copyOf(target.getActiveEffectsMap().keySet())) {
            if (effect.value().isBeneficial()) {
                target.removeEffect(effect);
            }
        }
    }

    private static void updateModuleEnchantment(Level level, ItemStack stack, ArmorUpgradeType type, ResourceKey<Enchantment> key) {
        Holder<Enchantment> enchantment = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
        CompoundTag tag = ItemData.get(stack);
        String savedKey = "ariente_original_" + type.getName();
        ItemEnchantments original = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(original);
        if (ModuleSupport.hasWorkingUpgrade(stack, type)) {
            if (!tag.contains(savedKey)) {
                tag.putInt(savedKey, enchantments.getLevel(enchantment));
                ItemData.set(stack, tag);

            }
            enchantments.set(enchantment, Math.max(3, tag.getIntOr(savedKey, 0)));
        } else if (tag.contains(savedKey)) {
            // Restore only this module's enchantment, preserving other sources.
            if (enchantments.getLevel(enchantment) == Math.max(3, tag.getIntOr(savedKey, 0))) {
                if (tag.getIntOr(savedKey, 0) > 0) {
                    enchantments.set(enchantment, tag.getIntOr(savedKey, 0));
                } else {
                    enchantments.set(enchantment, 0);
                }
            }
            tag.remove(savedKey);
            ItemData.set(stack, tag);

        }
        if (!enchantments.toImmutable().equals(original)) {
            stack.set(DataComponents.ENCHANTMENTS, enchantments.toImmutable());
        }
    }
}
