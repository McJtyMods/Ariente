package mcjty.ariente.items.modules;

import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.items.EnergyHolderItem;
import mcjty.ariente.items.ModItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;

public class ModuleSupport {
    public static void receivedHotkey(EntityPlayer player, int index) {
        handleHotkey(player, index, EntityEquipmentSlot.HEAD, ModItems.powerSuitHelmet);
        handleHotkey(player, index, EntityEquipmentSlot.LEGS, ModItems.powerSuitLegs);
        handleHotkey(player, index, EntityEquipmentSlot.FEET, ModItems.powerSuitBoots);
        handleHotkey(player, index, EntityEquipmentSlot.CHEST, ModItems.powerSuitChest);
        handleHotkey(player, index, EntityEquipmentSlot.MAINHAND, ModItems.enhancedEnergySabreItem);
    }

    private static void handleHotkey(EntityPlayer player, int index, EntityEquipmentSlot slot, Item armorItem) {
        ItemStack armorStack = player.getItemStackFromSlot(slot);
        if (!armorStack.isEmpty() && armorStack.getItem() == armorItem && armorStack.hasTagCompound()) {
            NBTTagCompound compound = armorStack.getTagCompound();
            for (ArmorUpgradeType type : ArmorUpgradeType.VALUES) {
                int idx = compound.getInteger(type.getHotkeyKey());
                if (idx == index) {
                    boolean on = compound.getBoolean(type.getModuleKey());
                    on = !on;
                    compound.setBoolean(type.getModuleKey(), on);
                }
            }
        }
    }

    public static boolean hasWorkingUpgrade(ItemStack stack, ArmorUpgradeType type) {
        if (stack.isEmpty() || !stack.hasTagCompound()) {
            return false;
        }
        return stack.getTagCompound().getBoolean(type.getWorkingKey());
    }

    @Nonnull
    public static Pair<Integer, Integer> getPowerUsage(ItemStack stack) {
        int power = 0;
        int maxPower = UtilityConfiguration.POWERSUIT_MAXPOWER.get();
        NBTTagCompound compound = stack.getTagCompound();
        if (compound == null) {
            return Pair.of(power, maxPower);
        }
        for (ArmorUpgradeType type : ArmorUpgradeType.VALUES) {
            String key = "module_" + type.getName();
            if (compound.hasKey(key)) {
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

    public static boolean managePower(ItemStack stack, EntityLivingBase entity) {
        Pair<Integer, Integer> powerUsage = getPowerUsage(stack);
        if (powerUsage.getLeft() > powerUsage.getRight()) {
            // Can't work
            return false;
        }
        if (powerUsage.getLeft() <= 0) {
            // No power consumption
            return true;
        }

        NBTTagCompound compound = stack.getTagCompound();

        int power = compound.getInteger("power");
        if (power <= 0) {
            // We need a new negarite/posirite injection
            int negarite = compound.getInteger("negarite");
            int posirite = compound.getInteger("posirite");
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
            compound.setInteger("negarite", negarite);
            compound.setInteger("posirite", posirite);
            int max = UtilityConfiguration.POWERSUIT_TICKS.get();
            if (powerUsage.getRight() > UtilityConfiguration.POWERSUIT_MAXPOWER.get()) {  // If > we have an energy optimizer
                max = UtilityConfiguration.POWERSUIT_TICKS_OPTIMIZED.get();
            }
            compound.setInteger("power", max / powerUsage.getLeft());
        } else {
            power--;
            compound.setInteger("power", power);
        }
        return true;
    }

    private static boolean checkAutofeed(EntityLivingBase entity, NBTTagCompound compound) {
        if (compound.getBoolean(ArmorUpgradeType.AUTOFEED.getModuleKey())) {
            if (entity instanceof EntityPlayer) {
                // Only auto-feed with player
                int negariteIndex = -1;
                int posiriteIndex = -1;
                EntityPlayer player = (EntityPlayer) entity;
                for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                    ItemStack itemStack = player.inventory.getStackInSlot(i);
                    if (itemStack.getItem() == ModItems.energyHolderItem) {
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
                    } else if (itemStack.getItem() == ModItems.negariteDust) {
                        if (!itemStack.isEmpty()) {
                            negariteIndex = i;
                            if (posiriteIndex != -1) {
                                break;
                            }
                        }
                    } else if (itemStack.getItem() == ModItems.posiriteDust) {
                        if (!itemStack.isEmpty()) {
                            posiriteIndex = i;
                            if (negariteIndex != -1) {
                                break;
                            }
                        }
                    }
                }
                if (negariteIndex != -1 && posiriteIndex != -1) {
                    ItemStack negariteStack = player.inventory.getStackInSlot(negariteIndex);
                    if (negariteStack.getItem() == ModItems.energyHolderItem) {
                        EnergyHolderItem.extractIfPossible(negariteStack, "negarite", 1);
                    } else {
                        negariteStack.shrink(1);
                    }

                    ItemStack posiriteStack = player.inventory.getStackInSlot(posiriteIndex);
                    if (posiriteStack.getItem() == ModItems.energyHolderItem) {
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
