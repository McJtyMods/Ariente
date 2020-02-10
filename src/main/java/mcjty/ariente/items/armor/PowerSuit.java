package mcjty.ariente.items.armor;

import com.google.common.collect.Multimap;
import mcjty.ariente.Ariente;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.bindings.KeyBindings;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.items.modules.ModuleSupport;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class PowerSuit extends ArmorItem {

    private static final UUID[] ARMOR_EXT_MODIFIERS = new UUID[] {UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()};

    public PowerSuit(EquipmentSlotType slot) {
        super(ArmorMaterial.LEATHER, slot, new Properties().maxDamage(0).group(Ariente.setup.getTab()));
        // @todo 1.14
//        setRegistryName("powersuit_" + slot.getName());
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        return Ariente.MODID + ":textures/armor/suit.png";
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);

        if (slot == this.slot) {
            double extra = ModuleSupport.hasWorkingUpgrade(stack, ArmorUpgradeType.ARMOR) ? 4 : 0;
            multimap.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(ARMOR_EXT_MODIFIERS[slot.getIndex()],
                    "Armor extra modifier", extra, AttributeModifier.Operation.ADDITION));
        }

        return multimap;
    }

    @Nullable
    @Override
    public BipedModel getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, BipedModel _default) {
        return PowerSuitModel.getModel(entityLiving, itemStack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof LivingEntity && !world.isRemote) {
            if (itemSlot != slot.getIndex()) {
                return;
            }
            switch (slot) {
                case FEET:
                    onUpdateFeet(stack, world, (LivingEntity) entity);
                    break;
                case LEGS:
                    onUpdateLegs(stack, world, (LivingEntity) entity);
                    break;
                case CHEST:
                    onUpdateChest(stack, world, (LivingEntity) entity);
                    break;
                case HEAD:
                    onUpdateHead(stack, world, (LivingEntity) entity);
                    break;
                case MAINHAND:
                case OFFHAND:
                default:
                    break;
            }
        }
    }

    private void onUpdateFeet(ItemStack stack, World world, LivingEntity entity) {
        if (stack.isEmpty() || stack.getItem() != ModBlocks.POWERSUIT_FEET.get() || !stack.hasTag()) {
            return;
        }

        CompoundNBT compound = stack.getTag();

        if (!ModuleSupport.managePower(stack, entity)) {
            compound.putBoolean(ArmorUpgradeType.ARMOR.getWorkingKey(), false);
            compound.putBoolean(ArmorUpgradeType.FEATHERFALLING.getWorkingKey(), false);
            compound.putBoolean(ArmorUpgradeType.STEPASSIST.getWorkingKey(), false);
            return;
        }

        compound.putBoolean(ArmorUpgradeType.ARMOR.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.ARMOR.getModuleKey()));
        compound.putBoolean(ArmorUpgradeType.FEATHERFALLING.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.FEATHERFALLING.getModuleKey()));
        compound.putBoolean(ArmorUpgradeType.STEPASSIST.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.STEPASSIST.getModuleKey()));
    }

    private void onUpdateLegs(ItemStack stack, World world, LivingEntity entity) {
        if (stack.isEmpty() || stack.getItem() != ModBlocks.POWERSUIT_LEGS.get() || !stack.hasTag()) {
            return;
        }

        CompoundNBT compound = stack.getTag();

        if (!ModuleSupport.managePower(stack, entity)) {
            compound.putBoolean(ArmorUpgradeType.ARMOR.getWorkingKey(), false);
            compound.putBoolean(ArmorUpgradeType.SPEED.getWorkingKey(), false);
            return;
        }

        compound.putBoolean(ArmorUpgradeType.ARMOR.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.ARMOR.getModuleKey()));
        compound.putBoolean(ArmorUpgradeType.SPEED.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.SPEED.getModuleKey()));
    }

    private void onUpdateChest(ItemStack stack, World world, LivingEntity entity) {
        if (stack.isEmpty() || stack.getItem() != ModBlocks.POWERSUIT_CHEST.get() || !stack.hasTag()) {
            return;
        }

        CompoundNBT compound = stack.getTag();

        if (!ModuleSupport.managePower(stack, entity)) {
            compound.putBoolean(ArmorUpgradeType.ARMOR.getWorkingKey(), false);
            compound.putBoolean(ArmorUpgradeType.FLIGHT.getWorkingKey(), false);
            compound.putBoolean(ArmorUpgradeType.HOVER.getWorkingKey(), false);
            compound.putBoolean(ArmorUpgradeType.FORCEFIELD.getWorkingKey(), false);
            return;
        }

        compound.putBoolean(ArmorUpgradeType.ARMOR.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.ARMOR.getModuleKey()));
        compound.putBoolean(ArmorUpgradeType.FLIGHT.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.FLIGHT.getModuleKey()));
        compound.putBoolean(ArmorUpgradeType.HOVER.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.HOVER.getModuleKey()));
        compound.putBoolean(ArmorUpgradeType.FORCEFIELD.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.FORCEFIELD.getModuleKey()));

        if (compound.getBoolean(ArmorUpgradeType.REGENERATION.getModuleKey())) {
            EffectInstance effect = entity.getActivePotionEffect(Effects.REGENERATION);
            if (effect == null || effect.getDuration() <= 50) {
                entity.addPotionEffect(new EffectInstance(Effects.REGENERATION, 100, 2, false, false));
            }
        }
    }

    private void onUpdateHead(ItemStack stack, World world, LivingEntity entity) {
        if (stack.isEmpty() || stack.getItem() != ModBlocks.POWERSUIT_HEAD.get() || !stack.hasTag()) {
            return;
        }

        CompoundNBT compound = stack.getTag();

        if (!ModuleSupport.managePower(stack, entity)) {
            compound.putBoolean(ArmorUpgradeType.ARMOR.getWorkingKey(), false);
            return;
        }

        compound.putBoolean(ArmorUpgradeType.ARMOR.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.ARMOR.getModuleKey()));
        compound.putBoolean(ArmorUpgradeType.SCRAMBLE.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.SCRAMBLE.getModuleKey()));

        if (compound.getBoolean(ArmorUpgradeType.NIGHTVISION.getModuleKey())) {
            EffectInstance effect = entity.getActivePotionEffect(Effects.NIGHT_VISION);
            if (effect == null || effect.getDuration() <= 300) {
                entity.addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, 400, 1, false, false));
            }
        }
        if (compound.getBoolean(ArmorUpgradeType.INVISIBILITY.getModuleKey())) {
            EffectInstance effect = entity.getActivePotionEffect(Effects.INVISIBILITY);
            if (effect == null || effect.getDuration() <= 120) {
                entity.addPotionEffect(new EffectInstance(Effects.INVISIBILITY, 200, 1, false, false));
            }
        }
    }

    public static boolean hasFullArmor(LivingEntity player) {
        if (player == null) {
            return false;
        }
        if (!(player.getItemStackFromSlot(EquipmentSlotType.HEAD).getItem() instanceof PowerSuit)) {
            return false;
        }
        if (!(player.getItemStackFromSlot(EquipmentSlotType.LEGS).getItem() instanceof PowerSuit)) {
            return false;
        }
        if (!(player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() instanceof PowerSuit)) {
            return false;
        }
        if (!(player.getItemStackFromSlot(EquipmentSlotType.FEET).getItem() instanceof PowerSuit)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<ITextComponent> list, ITooltipFlag b) {
        super.addInformation(stack, world, list, b);
        list.add(new StringTextComponent("Power suit part"));
        if (KeyBindings.configureArmor != null) {
            list.add(new StringTextComponent(TextFormatting.GRAY + "Configure with: " + TextFormatting.WHITE + "key " + KeyBindings.configureArmor.getDisplayName()));
        }
        if (stack.hasTag()) {
            CompoundNBT compound = stack.getTag();
            for (ArmorUpgradeType type : ArmorUpgradeType.VALUES) {
                String key = "module_" + type.getName();
                if (compound.contains(key)) {
                    boolean activated = compound.getBoolean(key);
                    if (activated) {
                        list.add(new StringTextComponent("    " + TextFormatting.GREEN + type.getDescription() + " (on)"));
                    } else {
                        list.add(new StringTextComponent("    " + TextFormatting.GRAY + type.getDescription() + " (off)"));
                    }
                }
            }
            Pair<Integer, Integer> usage = ModuleSupport.getPowerUsage(stack);
            list.add(new StringTextComponent(TextFormatting.WHITE + "Power: " + TextFormatting.YELLOW + usage.getLeft() + " / " + usage.getRight()));

            int negarite = compound.getInt("negarite");
            int posirite = compound.getInt("posirite");
            list.add(new StringTextComponent(TextFormatting.WHITE + "Negarite: " + TextFormatting.YELLOW + negarite));
            list.add(new StringTextComponent(TextFormatting.WHITE + "Posirite: " + TextFormatting.YELLOW + posirite));
        }
    }

}
