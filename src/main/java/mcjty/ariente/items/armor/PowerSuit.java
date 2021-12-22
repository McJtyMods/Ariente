package mcjty.ariente.items.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import mcjty.ariente.Ariente;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.bindings.KeyBindings;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.setup.Registration;
import net.minecraft.ChatFormatting;
// @todo 1.18 import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class PowerSuit extends ArmorItem {

    private static final UUID[] ARMOR_EXT_MODIFIERS = new UUID[] {UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()};

    public PowerSuit(EquipmentSlot slot) {
        super(ArmorMaterials.LEATHER, slot, new Properties().durability(0).tab(Ariente.setup.getTab()));
        // @todo 1.14
//        setRegistryName("powersuit_" + slot.getName());
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return Ariente.MODID + ":textures/armor/suit.png";
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);

        if (slot != this.slot) {
            return multimap;
        }

        double extra = ModuleSupport.hasWorkingUpgrade(stack, ArmorUpgradeType.ARMOR) ? 4 : 0;
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = new ImmutableMultimap.Builder<Attribute, AttributeModifier>();

        builder.putAll(multimap)
            .put(Attributes.ARMOR, new AttributeModifier(ARMOR_EXT_MODIFIERS[slot.getIndex()],
                    "Armor extra modifier", extra, AttributeModifier.Operation.ADDITION));

        return builder.build();
    }

    // @todo 1.18 @Nullable
    // @todo 1.18 @Override
    // @todo 1.18 public BipedModel getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, BipedModel _default) {
    // @todo 1.18     return PowerSuitModel.getModel(entityLiving, itemStack);
    // @todo 1.18 }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof LivingEntity && !world.isClientSide) {
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

    private void onUpdateFeet(ItemStack stack, Level world, LivingEntity entity) {
        if (stack.isEmpty() || stack.getItem() != Registration.POWERSUIT_FEET.get() || !stack.hasTag()) {
            return;
        }

        CompoundTag compound = stack.getTag();

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

    private void onUpdateLegs(ItemStack stack, Level world, LivingEntity entity) {
        if (stack.isEmpty() || stack.getItem() != Registration.POWERSUIT_LEGS.get() || !stack.hasTag()) {
            return;
        }

        CompoundTag compound = stack.getTag();

        if (!ModuleSupport.managePower(stack, entity)) {
            compound.putBoolean(ArmorUpgradeType.ARMOR.getWorkingKey(), false);
            compound.putBoolean(ArmorUpgradeType.SPEED.getWorkingKey(), false);
            return;
        }

        compound.putBoolean(ArmorUpgradeType.ARMOR.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.ARMOR.getModuleKey()));
        compound.putBoolean(ArmorUpgradeType.SPEED.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.SPEED.getModuleKey()));
    }

    private void onUpdateChest(ItemStack stack, Level world, LivingEntity entity) {
        if (stack.isEmpty() || stack.getItem() != Registration.POWERSUIT_CHEST.get() || !stack.hasTag()) {
            return;
        }

        CompoundTag compound = stack.getTag();

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
            MobEffectInstance effect = entity.getEffect(MobEffects.REGENERATION);
            if (effect == null || effect.getDuration() <= 50) {
                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 2, false, false));
            }
        }
    }

    private void onUpdateHead(ItemStack stack, Level world, LivingEntity entity) {
        if (stack.isEmpty() || stack.getItem() != Registration.POWERSUIT_HEAD.get() || !stack.hasTag()) {
            return;
        }

        CompoundTag compound = stack.getTag();

        if (!ModuleSupport.managePower(stack, entity)) {
            compound.putBoolean(ArmorUpgradeType.ARMOR.getWorkingKey(), false);
            return;
        }

        compound.putBoolean(ArmorUpgradeType.ARMOR.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.ARMOR.getModuleKey()));
        compound.putBoolean(ArmorUpgradeType.SCRAMBLE.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.SCRAMBLE.getModuleKey()));

        if (compound.getBoolean(ArmorUpgradeType.NIGHTVISION.getModuleKey())) {
            MobEffectInstance effect = entity.getEffect(MobEffects.NIGHT_VISION);
            if (effect == null || effect.getDuration() <= 300) {
                entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 1, false, false));
            }
        }
        if (compound.getBoolean(ArmorUpgradeType.INVISIBILITY.getModuleKey())) {
            MobEffectInstance effect = entity.getEffect(MobEffects.INVISIBILITY);
            if (effect == null || effect.getDuration() <= 120) {
                entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 1, false, false));
            }
        }
    }

    public static boolean hasFullArmor(LivingEntity player) {
        if (player == null) {
            return false;
        }
        if (!(player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof PowerSuit)) {
            return false;
        }
        if (!(player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof PowerSuit)) {
            return false;
        }
        if (!(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof PowerSuit)) {
            return false;
        }
        if (!(player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof PowerSuit)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> list, TooltipFlag b) {
        super.appendHoverText(stack, world, list, b);
        list.add(new TextComponent("Power suit part"));
        if (KeyBindings.configureArmor != null) {
            list.add(new TextComponent(ChatFormatting.GRAY + "Configure with: " + ChatFormatting.WHITE + "key " + KeyBindings.configureArmor.getDisplayName()));
        }
        if (stack.hasTag()) {
            CompoundTag compound = stack.getTag();
            for (ArmorUpgradeType type : ArmorUpgradeType.VALUES) {
                String key = "module_" + type.getName();
                if (compound.contains(key)) {
                    boolean activated = compound.getBoolean(key);
                    if (activated) {
                        list.add(new TextComponent("    " + ChatFormatting.GREEN + type.getDescription() + " (on)"));
                    } else {
                        list.add(new TextComponent("    " + ChatFormatting.GRAY + type.getDescription() + " (off)"));
                    }
                }
            }
            Pair<Integer, Integer> usage = ModuleSupport.getPowerUsage(stack);
            list.add(new TextComponent(ChatFormatting.WHITE + "Power: " + ChatFormatting.YELLOW + usage.getLeft() + " / " + usage.getRight()));

            int negarite = compound.getInt("negarite");
            int posirite = compound.getInt("posirite");
            list.add(new TextComponent(ChatFormatting.WHITE + "Negarite: " + ChatFormatting.YELLOW + negarite));
            list.add(new TextComponent(ChatFormatting.WHITE + "Posirite: " + ChatFormatting.YELLOW + posirite));
        }
    }

}
