package mcjty.ariente.items.armor;

import mcjty.ariente.Ariente;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.items.modules.ArmorUpgradeType;
import mcjty.lib.McJtyRegister;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class PowerSuit extends ItemArmor {

    public PowerSuit(EntityEquipmentSlot slot) {
        super(ArmorMaterial.LEATHER, 0, slot);
        setRegistryName("powersuit_" + slot.getName());
        setUnlocalizedName(Ariente.MODID + ".powersuit_" + slot.getName());
        setCreativeTab(Ariente.creativeTab);
        McJtyRegister.registerLater(this, Ariente.instance);
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return Ariente.MODID + ":textures/armor/suit.png";
    }

    @Nullable
    @Override
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {
        return PowerSuitModel.getModel(entityLiving, itemStack);
    }

    public static void receivedHotkey(EntityPlayer player, int index) {
        handleHotkey(player, index, EntityEquipmentSlot.HEAD, ModItems.powerSuitHelmet);
        handleHotkey(player, index, EntityEquipmentSlot.LEGS, ModItems.powerSuitLegs);
        handleHotkey(player, index, EntityEquipmentSlot.FEET, ModItems.powerSuitBoots);
        handleHotkey(player, index, EntityEquipmentSlot.CHEST, ModItems.powerSuitChest);
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

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof EntityLivingBase && !world.isRemote) {
            if (itemSlot != armorType.getIndex()) {
                return;
            }
            switch (armorType) {
                case FEET:
                    onUpdateFeet(stack, world, (EntityLivingBase) entity);
                    break;
                case LEGS:
                    onUpdateLegs(stack, world, (EntityLivingBase) entity);
                    break;
                case CHEST:
                    onUpdateChest(stack, world, (EntityLivingBase) entity);
                    break;
                case HEAD:
                    onUpdateHead(stack, world, (EntityLivingBase) entity);
                    break;
                case MAINHAND:
                case OFFHAND:
                default:
                    break;
            }
        }
    }

    private void onUpdateFeet(ItemStack stack, World world, EntityLivingBase entity) {
        if (stack.isEmpty() || stack.getItem() != ModItems.powerSuitBoots || !stack.hasTagCompound()) {
            return;
        }

        if (!managePower(stack)) {
            return;
        }

        NBTTagCompound compound = stack.getTagCompound();
        }

    private void onUpdateLegs(ItemStack stack, World world, EntityLivingBase entity) {
        if (stack.isEmpty() || stack.getItem() != ModItems.powerSuitLegs || !stack.hasTagCompound()) {
            return;
        }

        if (!managePower(stack)) {
            return;
        }

        NBTTagCompound compound = stack.getTagCompound();

        if (compound.getBoolean(ArmorUpgradeType.SPEED.getModuleKey())) {
            PotionEffect effect = entity.getActivePotionEffect(MobEffects.SPEED);
            if (effect == null || effect.getDuration() <= 100) {
                entity.addPotionEffect(new PotionEffect(MobEffects.SPEED, 200, 1, false, false));
            }
        }
    }

    private void onUpdateChest(ItemStack stack, World world, EntityLivingBase entity) {
        if (stack.isEmpty() || stack.getItem() != ModItems.powerSuitChest || !stack.hasTagCompound()) {
            return;
        }

        if (!managePower(stack)) {
            return;
        }

        NBTTagCompound compound = stack.getTagCompound();

        if (compound.getBoolean(ArmorUpgradeType.REGENERATION.getModuleKey())) {
            PotionEffect effect = entity.getActivePotionEffect(MobEffects.REGENERATION);
            if (effect == null || effect.getDuration() <= 100) {
                entity.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 200, 1, false, false));
            }
        }
    }

    private void onUpdateHead(ItemStack stack, World world, EntityLivingBase entity) {
        if (stack.isEmpty() || stack.getItem() != ModItems.powerSuitHelmet || !stack.hasTagCompound()) {
            return;
        }

        if (!managePower(stack)) {
            return;
        }

        NBTTagCompound compound = stack.getTagCompound();

        if (compound.getBoolean(ArmorUpgradeType.NIGHTVISION.getModuleKey())) {
            PotionEffect effect = entity.getActivePotionEffect(MobEffects.NIGHT_VISION);
            if (effect == null || effect.getDuration() <= 200) {
                entity.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 400, 1, false, false));
            }
        }
        if (compound.getBoolean(ArmorUpgradeType.INVISIBILITY.getModuleKey())) {
            PotionEffect effect = entity.getActivePotionEffect(MobEffects.INVISIBILITY);
            if (effect == null || effect.getDuration() <= 120) {
                entity.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 200, 1, false, false));
            }
        }
    }

    private boolean managePower(ItemStack stack) {
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
                // Not enough power possible
                return false;
            }
            compound.setInteger("negarite", negarite);
            compound.setInteger("posirite", posirite);
            compound.setInteger("power", 5000 / powerUsage.getLeft());    // @todo configurable
        } else {
            power--;
            compound.setInteger("power", power);
        }
        return true;
    }

    public static boolean hasFullArmor(EntityPlayer player) {
        if (player == null) {
            return false;
        }
        if (!(player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() instanceof PowerSuit)) {
            return false;
        }
        if (!(player.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() instanceof PowerSuit)) {
            return false;
        }
        if (!(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof PowerSuit)) {
            return false;
        }
        if (!(player.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() instanceof PowerSuit)) {
            return false;
        }
        return true;
    }

    @Nonnull
    public static Pair<Integer, Integer> getPowerUsage(ItemStack stack) {
        int power = 0;
        int maxPower = 100;
        NBTTagCompound compound = stack.getTagCompound();
        if (compound == null) {
            return Pair.of(0, 0);
        }
        for (ArmorUpgradeType type : ArmorUpgradeType.VALUES) {
            String key = "module_" + type.getName();
            if (compound.hasKey(key)) {
                boolean activated = compound.getBoolean(key);
                if (activated) {
                    if (type.getPowerUsage() > 0) {
                        power += type.getPowerUsage();
                    } else if (type.getPowerUsage() == -1) {
                        maxPower = 130;
                    }
                }
            }
        }
        return Pair.of(power, maxPower);
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag b) {
        super.addInformation(stack, world, list, b);
        list.add("Power suit part");
        if (stack.hasTagCompound()) {
            NBTTagCompound compound = stack.getTagCompound();
            for (ArmorUpgradeType type : ArmorUpgradeType.VALUES) {
                String key = "module_" + type.getName();
                if (compound.hasKey(key)) {
                    boolean activated = compound.getBoolean(key);
                    if (activated) {
                        list.add("    " + TextFormatting.GREEN + type.getDescription() + " (on)");
                    } else {
                        list.add("    " + TextFormatting.GRAY + type.getDescription() + " (off)");
                    }
                }
            }
            Pair<Integer, Integer> usage = getPowerUsage(stack);
            list.add(TextFormatting.WHITE + "Power: " + TextFormatting.YELLOW + usage.getLeft() + " / " + usage.getRight());

            int negarite = compound.getInteger("negarite");
            int posirite = compound.getInteger("posirite");
            list.add(TextFormatting.WHITE + "Negarite: " + TextFormatting.YELLOW + negarite);
            list.add(TextFormatting.WHITE + "Posirite: " + TextFormatting.YELLOW + posirite);
        }
    }

}
