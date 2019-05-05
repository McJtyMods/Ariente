package mcjty.ariente.items.armor;

import com.google.common.collect.Multimap;
import mcjty.ariente.Ariente;
import mcjty.ariente.bindings.KeyBindings;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.lib.McJtyRegister;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class PowerSuit extends ItemArmor {

    private static final UUID[] ARMOR_EXT_MODIFIERS = new UUID[] {UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()};

    public PowerSuit(EntityEquipmentSlot slot) {
        super(ArmorMaterial.LEATHER, 0, slot);
        setRegistryName("powersuit_" + slot.getName());
        setUnlocalizedName(Ariente.MODID + ".powersuit_" + slot.getName());
        setMaxDamage(0);
        setCreativeTab(Ariente.setup.getTab());
        McJtyRegister.registerLater(this, Ariente.instance);
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return Ariente.MODID + ":textures/armor/suit.png";
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);

        if (slot == this.armorType) {
            double extra = ModuleSupport.hasWorkingUpgrade(stack, ArmorUpgradeType.ARMOR) ? 4 : 0;
            multimap.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(ARMOR_EXT_MODIFIERS[armorType.getIndex()],
                    "Armor extra modifier", extra, 0));
        }

        return multimap;
    }

    @Nullable
    @Override
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {
        return PowerSuitModel.getModel(entityLiving, itemStack);
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

        NBTTagCompound compound = stack.getTagCompound();

        if (!ModuleSupport.managePower(stack, entity)) {
            compound.setBoolean(ArmorUpgradeType.ARMOR.getWorkingKey(), false);
            compound.setBoolean(ArmorUpgradeType.FEATHERFALLING.getWorkingKey(), false);
            compound.setBoolean(ArmorUpgradeType.STEPASSIST.getWorkingKey(), false);
            return;
        }

        compound.setBoolean(ArmorUpgradeType.ARMOR.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.ARMOR.getModuleKey()));
        compound.setBoolean(ArmorUpgradeType.FEATHERFALLING.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.FEATHERFALLING.getModuleKey()));
        compound.setBoolean(ArmorUpgradeType.STEPASSIST.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.STEPASSIST.getModuleKey()));
    }

    private void onUpdateLegs(ItemStack stack, World world, EntityLivingBase entity) {
        if (stack.isEmpty() || stack.getItem() != ModItems.powerSuitLegs || !stack.hasTagCompound()) {
            return;
        }

        NBTTagCompound compound = stack.getTagCompound();

        if (!ModuleSupport.managePower(stack, entity)) {
            compound.setBoolean(ArmorUpgradeType.ARMOR.getWorkingKey(), false);
            return;
        }

        compound.setBoolean(ArmorUpgradeType.ARMOR.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.ARMOR.getModuleKey()));

        if (compound.getBoolean(ArmorUpgradeType.SPEED.getModuleKey())) {
            PotionEffect effect = entity.getActivePotionEffect(MobEffects.SPEED);
            if (effect == null || effect.getDuration() <= 50) {
                entity.addPotionEffect(new PotionEffect(MobEffects.SPEED, 100, 2, false, false));
            }
        }
    }

    private void onUpdateChest(ItemStack stack, World world, EntityLivingBase entity) {
        if (stack.isEmpty() || stack.getItem() != ModItems.powerSuitChest || !stack.hasTagCompound()) {
            return;
        }

        NBTTagCompound compound = stack.getTagCompound();

        if (!ModuleSupport.managePower(stack, entity)) {
            compound.setBoolean(ArmorUpgradeType.ARMOR.getWorkingKey(), false);
            compound.setBoolean(ArmorUpgradeType.FLIGHT.getWorkingKey(), false);
            compound.setBoolean(ArmorUpgradeType.FORCEFIELD.getWorkingKey(), false);
            return;
        }

        compound.setBoolean(ArmorUpgradeType.ARMOR.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.ARMOR.getModuleKey()));
        compound.setBoolean(ArmorUpgradeType.FLIGHT.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.FLIGHT.getModuleKey()));
        compound.setBoolean(ArmorUpgradeType.FORCEFIELD.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.FORCEFIELD.getModuleKey()));

        if (compound.getBoolean(ArmorUpgradeType.REGENERATION.getModuleKey())) {
            PotionEffect effect = entity.getActivePotionEffect(MobEffects.REGENERATION);
            if (effect == null || effect.getDuration() <= 50) {
                entity.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 100, 2, false, false));
            }
        }
    }

    private void onUpdateHead(ItemStack stack, World world, EntityLivingBase entity) {
        if (stack.isEmpty() || stack.getItem() != ModItems.powerSuitHelmet || !stack.hasTagCompound()) {
            return;
        }

        NBTTagCompound compound = stack.getTagCompound();

        if (!ModuleSupport.managePower(stack, entity)) {
            compound.setBoolean(ArmorUpgradeType.ARMOR.getWorkingKey(), false);
            return;
        }

        compound.setBoolean(ArmorUpgradeType.ARMOR.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.ARMOR.getModuleKey()));
        compound.setBoolean(ArmorUpgradeType.SCRAMBLE.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.SCRAMBLE.getModuleKey()));

        if (compound.getBoolean(ArmorUpgradeType.NIGHTVISION.getModuleKey())) {
            PotionEffect effect = entity.getActivePotionEffect(MobEffects.NIGHT_VISION);
            if (effect == null || effect.getDuration() <= 300) {
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

    public static boolean hasFullArmor(EntityLivingBase player) {
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

    @Override
    public boolean hasEffect(ItemStack stack) {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag b) {
        super.addInformation(stack, world, list, b);
        list.add("Power suit part");
        if (KeyBindings.configureArmor != null) {
            list.add(TextFormatting.GRAY + "Configure with: " + TextFormatting.WHITE + "key " + KeyBindings.configureArmor.getDisplayName());
        }
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
            Pair<Integer, Integer> usage = ModuleSupport.getPowerUsage(stack);
            list.add(TextFormatting.WHITE + "Power: " + TextFormatting.YELLOW + usage.getLeft() + " / " + usage.getRight());

            int negarite = compound.getInteger("negarite");
            int posirite = compound.getInteger("posirite");
            list.add(TextFormatting.WHITE + "Negarite: " + TextFormatting.YELLOW + negarite);
            list.add(TextFormatting.WHITE + "Posirite: " + TextFormatting.YELLOW + posirite);
        }
    }

}
