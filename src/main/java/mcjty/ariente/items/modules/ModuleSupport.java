package mcjty.ariente.items.modules;

import javax.annotation.Nonnull;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.items.EnergyHolderItem;
import mcjty.ariente.items.EnergyLanceItem;
import mcjty.ariente.items.EnhancedEnergySabreItem;
import mcjty.ariente.items.ItemData;
import mcjty.ariente.setup.Registration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

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
        if (!armorStack.isEmpty() && armorStack.getItem() == armorItem && ItemData.has(armorStack)) {
            CompoundTag compound = ItemData.get(armorStack);
            for (ArmorUpgradeType type : ArmorUpgradeType.VALUES) {
                int idx = compound.getIntOr(type.getHotkeyKey(), 0);
                if (compound.contains(type.getModuleKey()) && idx == index) {
                    boolean on = compound.getBooleanOr(type.getModuleKey(), false);
                    on = !on;
                    compound.putBoolean(type.getModuleKey(), on);
                    ItemData.set(armorStack, compound);

                    compound.putBoolean(type.getWorkingKey(), false);
                    ItemData.set(armorStack, compound);

                }
            }
        }
    }

    public static boolean hasWorkingUpgrade(ItemStack stack, ArmorUpgradeType type) {
        if (stack.isEmpty() || !ItemData.has(stack)) {
            return false;
        }
        return ItemData.get(stack).getBooleanOr(type.getModuleKey(), false) && ItemData.get(stack).getBooleanOr(type.getWorkingKey(), false);
    }

    @Nonnull
    public static Pair<Integer, Integer> getPowerUsage(ItemStack stack) {
        int power = 0;
        int maxPower = UtilityConfiguration.POWERSUIT_MAXPOWER.get();
        CompoundTag compound = ItemData.get(stack);
        if (compound == null) {
            return Pair.of(power, maxPower);
        }
        for (ArmorUpgradeType type : ArmorUpgradeType.VALUES) {
            String key = "module_" + type.getName();
            if (compound.contains(key)) {
                boolean activated = compound.getBooleanOr(key, false);
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
        if (ItemData.has(stack)) {
            for (ArmorUpgradeType type : ArmorUpgradeType.VALUES) {
                ItemData.update(stack, data -> data.remove(type.getWorkingKey()));
            }
        }
    }

    public static boolean updateWorkingState(ItemStack stack, LivingEntity entity) {
        boolean powered = managePower(stack, entity);
        CompoundTag tag = ItemData.get(stack);
        if (tag != null) {
            for (ArmorUpgradeType type : ArmorUpgradeType.VALUES) {
                if (tag.contains(type.getModuleKey())) {
                    tag.putBoolean(type.getWorkingKey(), powered && tag.getBooleanOr(type.getModuleKey(), false));
                    ItemData.set(stack, tag);

                } else {
                    tag.remove(type.getWorkingKey());
                    ItemData.set(stack, tag);

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
        CompoundTag tag = ItemData.get(stack);
        int power = tag.getIntOr("power", 0);
        if (power <= 0) {
            if (tag.getIntOr("negarite", 0) <= 0 || tag.getIntOr("posirite", 0) <= 0) {
                return false;
            }
            tag.putInt("negarite", tag.getIntOr("negarite", 0) - 1);
            ItemData.set(stack, tag);

            tag.putInt("posirite", tag.getIntOr("posirite", 0) - 1);
            ItemData.set(stack, tag);

            power = Math.max(1, ticks / usage.getLeft());
        }
        tag.putInt("power", power - 1);
        ItemData.set(stack, tag);

        return true;
    }

}
