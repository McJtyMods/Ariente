package mcjty.ariente.items.modules;

import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.items.EnergyHolderItem;
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
        handleHotkey(player, index, EquipmentSlot.HEAD, Registration.POWERSUIT_HEAD.get());
        handleHotkey(player, index, EquipmentSlot.LEGS, Registration.POWERSUIT_LEGS.get());
        handleHotkey(player, index, EquipmentSlot.FEET, Registration.POWERSUIT_FEET.get());
        handleHotkey(player, index, EquipmentSlot.CHEST, Registration.POWERSUIT_CHEST.get());
        handleHotkey(player, index, EquipmentSlot.MAINHAND, Registration.ENHANCED_ENERGY_SABRE.get());
    }

    private static void handleHotkey(Player player, int index, EquipmentSlot slot, Item armorItem) {
        ItemStack armorStack = player.getItemBySlot(slot);
        if (!armorStack.isEmpty() && armorStack.getItem() == armorItem && armorStack.hasTag()) {
            CompoundTag compound = armorStack.getTag();
            for (ArmorUpgradeType type : ArmorUpgradeType.VALUES) {
                int idx = compound.getInt(type.getHotkeyKey());
                if (idx == index) {
                    boolean on = compound.getBoolean(type.getModuleKey());
                    on = !on;
                    compound.putBoolean(type.getModuleKey(), on);
                }
            }
        }
    }

    public static boolean hasWorkingUpgrade(ItemStack stack, ArmorUpgradeType type) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return false;
        }
        return stack.getTag().getBoolean(type.getWorkingKey());
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

    public static boolean managePower(ItemStack stack, LivingEntity entity) {
        Pair<Integer, Integer> powerUsage = getPowerUsage(stack);
        if (powerUsage.getLeft() > powerUsage.getRight()) {
            // Can't work
            return false;
        }
        if (powerUsage.getLeft() <= 0) {
            // No power consumption
            return true;
        }

        CompoundTag compound = stack.getTag();

        int power = compound.getInt("power");
        if (power <= 0) {
            // We need a new negarite/posirite injection
            int negarite = compound.getInt("negarite");
            int posirite = compound.getInt("posirite");
            negarite--;
            posirite--;
            if (negarite < 0 || posirite < 0) {
                // Not enough power possible. Check if we can autofeed
                if (!checkAutofeed(entity, compound)) {
                    return false;
                } else {
                    // We autofed and immediatelly consumed it so set values to 0
                    posirite = 0;
                    negarite = 0;
                }
            }
            compound.putInt("negarite", negarite);
            compound.putInt("posirite", posirite);
            int max = UtilityConfiguration.POWERSUIT_TICKS.get();
            if (powerUsage.getRight() > UtilityConfiguration.POWERSUIT_MAXPOWER.get()) {  // If > we have an energy optimizer
                max = UtilityConfiguration.POWERSUIT_TICKS_OPTIMIZED.get();
            }
            compound.putInt("power", max / powerUsage.getLeft());
        } else {
            power--;
            compound.putInt("power", power);
        }
        return true;
    }

    private static boolean checkAutofeed(LivingEntity entity, CompoundTag compound) {
        if (compound.getBoolean(ArmorUpgradeType.AUTOFEED.getModuleKey())) {
            if (entity instanceof Player) {
                // Only auto-feed with player
                int negariteIndex = -1;
                int posiriteIndex = -1;
                Player player = (Player) entity;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack itemStack = player.getInventory().getItem(i);
                    if (itemStack.getItem() == Registration.ENERGY_HOLDER.get()) {
                        int negarite = EnergyHolderItem.count(itemStack, "negarite");
                        if (negarite > 0) {
                            negariteIndex = i;
                        }
                        int posirite = EnergyHolderItem.count(itemStack, "posirite");
                        if (posirite > 0) {
                            posiriteIndex = i;
                        }
                        if (negariteIndex != -1 && posiriteIndex != -1) {
                            break;
                        }
                    } else if (itemStack.getItem() == Registration.DUST_NEGARITE.get()) {
                        if (!itemStack.isEmpty()) {
                            negariteIndex = i;
                            if (posiriteIndex != -1) {
                                break;
                            }
                        }
                    } else if (itemStack.getItem() == Registration.DUST_POSIRITE.get()) {
                        if (!itemStack.isEmpty()) {
                            posiriteIndex = i;
                            if (negariteIndex != -1) {
                                break;
                            }
                        }
                    }
                }
                if (negariteIndex != -1 && posiriteIndex != -1) {
                    ItemStack negariteStack = player.getInventory().getItem(negariteIndex);
                    if (negariteStack.getItem() == Registration.ENERGY_HOLDER.get()) {
                        EnergyHolderItem.extractIfPossible(negariteStack, "negarite", 1);
                    } else {
                        negariteStack.shrink(1);
                    }

                    ItemStack posiriteStack = player.getInventory().getItem(posiriteIndex);
                    if (posiriteStack.getItem() == Registration.ENERGY_HOLDER.get()) {
                        EnergyHolderItem.extractIfPossible(negariteStack, "posirite", 1);
                    } else {
                        posiriteStack.shrink(1);
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
