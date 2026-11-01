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
        CompoundTag tag = stack.getOrCreateTag();
        int power = tag.getInt("power");
        if (power <= 0) {
            if (!consumeFuelPair(entity, tag)) {
                return false;
            }
            int ticks = usage.getRight() > UtilityConfiguration.POWERSUIT_MAXPOWER.get()
                    ? UtilityConfiguration.POWERSUIT_TICKS_OPTIMIZED.get() : UtilityConfiguration.POWERSUIT_TICKS.get();
            power = Math.max(1, ticks / usage.getLeft());
        }
        tag.putInt("power", power - 1);
        return true;
    }

    private static boolean consumeFuelPair(LivingEntity entity, CompoundTag tag) {
        boolean needNegarite = tag.getInt("negarite") <= 0;
        boolean needPosirite = tag.getInt("posirite") <= 0;
        int negariteSlot = -1;
        int posiriteSlot = -1;
        Player player = entity instanceof Player p ? p : null;
        if (needNegarite || needPosirite) {
            if (player == null || !tag.getBoolean(ArmorUpgradeType.AUTOFEED.getModuleKey())) {
                return false;
            }
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack fuel = player.getInventory().getItem(i);
                if (needNegarite && negariteSlot < 0 && hasFuel(fuel, Registration.DUST_NEGARITE.get(), "negarite")) {
                    negariteSlot = i;
                }
                if (needPosirite && posiriteSlot < 0 && hasFuel(fuel, Registration.DUST_POSIRITE.get(), "posirite")) {
                    posiriteSlot = i;
                }
            }
            // Locate both missing components before changing either inventory or local fuel.
            if ((needNegarite && negariteSlot < 0) || (needPosirite && posiriteSlot < 0)) {
                return false;
            }
        }
        if (needNegarite) {
            extractFuel(player.getInventory().getItem(negariteSlot), "negarite");
        } else {
            tag.putInt("negarite", tag.getInt("negarite") - 1);
        }
        if (needPosirite) {
            extractFuel(player.getInventory().getItem(posiriteSlot), "posirite");
        } else {
            tag.putInt("posirite", tag.getInt("posirite") - 1);
        }
        return true;
    }

    private static boolean hasFuel(ItemStack stack, Item dust, String key) {
        return !stack.isEmpty() && (stack.is(dust)
                || (stack.is(Registration.ENERGY_HOLDER.get()) && EnergyHolderItem.count(stack, key) > 0));
    }

    private static void extractFuel(ItemStack stack, String key) {
        if (stack.is(Registration.ENERGY_HOLDER.get())) {
            EnergyHolderItem.extractIfPossible(stack, key, 1);
        } else {
            stack.shrink(1);
        }
    }
}
