package mcjty.ariente.items.modules;

import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.items.EnergyHolderItem;
import mcjty.ariente.items.EnergyLanceItem;
import mcjty.ariente.items.EnhancedEnergySabreItem;
import mcjty.ariente.setup.Registration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;

public class ModuleSupport {

    public static void receivedHotkey(Player player, int index) {
        if (player == null || index < 1 || index > 4) {
            return;
        }
        handleHotkey(player, index, EquipmentSlot.HEAD, Registration.POWERSUIT_HEAD.get());
        handleHotkey(player, index, EquipmentSlot.LEGS, Registration.POWERSUIT_LEGS.get());
        handleHotkey(player, index, EquipmentSlot.FEET, Registration.POWERSUIT_FEET.get());
        handleHotkey(player, index, EquipmentSlot.CHEST, Registration.POWERSUIT_CHEST.get());
        handleHotkey(player, index, EquipmentSlot.MAINHAND, Registration.ENHANCED_ENERGY_SABRE.get());
        handleHotkey(player, index, EquipmentSlot.MAINHAND, Registration.ENERGY_LANCE.get());
    }

    public static boolean isModularWeapon(ItemStack stack) {
        return stack.getItem() instanceof EnhancedEnergySabreItem || stack.getItem() instanceof EnergyLanceItem;
    }

    private static void handleHotkey(Player player, int index, EquipmentSlot slot, Item armorItem) {
        ItemStack armorStack = player.getItemBySlot(slot);
        if (!armorStack.isEmpty() && armorStack.getItem() == armorItem && armorStack.hasTag()) {
            CompoundTag compound = armorStack.getTag();
            for (ArmorUpgradeType type : ArmorUpgradeType.VALUES) {
                int idx = compound.getInt(type.getHotkeyKey());
                if (compound.contains(type.getModuleKey()) && idx == index) {
                    boolean on = compound.getBoolean(type.getModuleKey());
                    on = !on;
                    compound.putBoolean(type.getModuleKey(), on);
                    compound.putBoolean(type.getWorkingKey(), false);
                }
            }
        }
    }

    public static boolean hasWorkingUpgrade(ItemStack stack, ArmorUpgradeType type) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return false;
        }
        return stack.getTag().getBoolean(type.getModuleKey()) && stack.getTag().getBoolean(type.getWorkingKey());
    }

    @Nonnull
    public static Pair<Integer, Integer> getPowerUsage(ItemStack stack) {
        int power = 0;
        int maxPower = UtilityConfiguration.POWERSUIT_MAXPOWER.get();
        CompoundTag compound = stack.getTag();
        if (compound == null) {
            return Pair.of(power, maxPower);
        }
        for (ArmorUpgradeType type : ArmorUpgradeType.VALUES) {
            String key = "module_" + type.getName();
            if (compound.contains(key)) {
                boolean activated = compound.getBoolean(key);
                if (activated) {
                    if (type.getPowerUsage() > 0) {
                        power += type.getPowerUsage();
                    } else if (type.getPowerUsage() == -1) {
                        maxPower = UtilityConfiguration.POWERSUIT_MAXPOWER_OPTIMIZED.get();
                    }
                }
            }
        }
        return Pair.of(power, maxPower);
    }

    public static void clearWorkingState(ItemStack stack) {
        if (stack.hasTag()) {
            for (ArmorUpgradeType type : ArmorUpgradeType.VALUES) {
                stack.getTag().remove(type.getWorkingKey());
            }
        }
    }

    public static boolean updateWorkingState(ItemStack stack, LivingEntity entity) {
        boolean powered = managePower(stack, entity);
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            for (ArmorUpgradeType type : ArmorUpgradeType.VALUES) {
                if (tag.contains(type.getModuleKey())) {
                    tag.putBoolean(type.getWorkingKey(), powered && tag.getBoolean(type.getModuleKey()));
                } else {
                    tag.remove(type.getWorkingKey());
                }
            }
        }
        return powered;
    }

    public static boolean managePower(ItemStack stack, LivingEntity entity) {
        Pair<Integer, Integer> usage = getPowerUsage(stack);
        if (usage.getLeft() > usage.getRight()) {
            return false;
        }
        if (usage.getLeft() <= 0) {
            return true;
        }
        int ticks = usage.getRight() > UtilityConfiguration.POWERSUIT_MAXPOWER.get()
                ? UtilityConfiguration.POWERSUIT_TICKS_OPTIMIZED.get() : UtilityConfiguration.POWERSUIT_TICKS.get();
        if (entity instanceof Player player) {
            return EnergyHolderItem.consumePower(player, usage.getLeft(), ticks);
        }
        // City soldiers receive fuel from their AI rather than a player's inventory.
        CompoundTag tag = stack.getOrCreateTag();
        int power = tag.getInt("power");
        if (power <= 0) {
            if (tag.getInt("negarite") <= 0 || tag.getInt("posirite") <= 0) {
                return false;
            }
            tag.putInt("negarite", tag.getInt("negarite") - 1);
            tag.putInt("posirite", tag.getInt("posirite") - 1);
            power = Math.max(1, ticks / usage.getLeft());
        }
        tag.putInt("power", power - 1);
        return true;
    }

}
