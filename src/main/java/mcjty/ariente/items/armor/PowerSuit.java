package mcjty.ariente.items.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import mcjty.ariente.Ariente;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.bindings.KeyBindings;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.setup.Registration;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

import net.minecraft.item.Item.Properties;

public class PowerSuit extends ArmorItem {

    private static final UUID[] ARMOR_EXT_MODIFIERS = new UUID[] {UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()};

    public PowerSuit(EquipmentSlot slot) {
        super(ArmorMaterial.LEATHER, slot, new Properties().durability(0).tab(Ariente.setup.getTab()));
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

    @Nullable
    @Override
    public BipedModel getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, BipedModel _default) {
        return PowerSuitModel.getModel(entityLiving, itemStack);
    }

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
            EffectInstance effect = entity.getEffect(Effects.REGENERATION);
            if (effect == null || effect.getDuration() <= 50) {
                entity.addEffect(new EffectInstance(Effects.REGENERATION, 100, 2, false, false));
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
            EffectInstance effect = entity.getEffect(Effects.NIGHT_VISION);
            if (effect == null || effect.getDuration() <= 300) {
                entity.addEffect(new EffectInstance(Effects.NIGHT_VISION, 400, 1, false, false));
            }
        }
        if (compound.getBoolean(ArmorUpgradeType.INVISIBILITY.getModuleKey())) {
            EffectInstance effect = entity.getEffect(Effects.INVISIBILITY);
            if (effect == null || effect.getDuration() <= 120) {
                entity.addEffect(new EffectInstance(Effects.INVISIBILITY, 200, 1, false, false));
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
    public void appendHoverText(ItemStack stack, Level world, List<TextComponent> list, TooltipFlag b) {
        super.appendHoverText(stack, world, list, b);
        list.add(new StringTextComponent("Power suit part"));
        if (KeyBindings.configureArmor != null) {
            list.add(new StringTextComponent(ChatFormatting.GRAY + "Configure with: " + ChatFormatting.WHITE + "key " + KeyBindings.configureArmor.getDisplayName()));
        }
        if (stack.hasTag()) {
            CompoundTag compound = stack.getTag();
            for (ArmorUpgradeType type : ArmorUpgradeType.VALUES) {
                String key = "module_" + type.getName();
                if (compound.contains(key)) {
                    boolean activated = compound.getBoolean(key);
                    if (activated) {
                        list.add(new StringTextComponent("    " + ChatFormatting.GREEN + type.getDescription() + " (on)"));
                    } else {
                        list.add(new StringTextComponent("    " + ChatFormatting.GRAY + type.getDescription() + " (off)"));
                    }
                }
            }
            Pair<Integer, Integer> usage = ModuleSupport.getPowerUsage(stack);
            list.add(new StringTextComponent(ChatFormatting.WHITE + "Power: " + ChatFormatting.YELLOW + usage.getLeft() + " / " + usage.getRight()));

            int negarite = compound.getInt("negarite");
            int posirite = compound.getInt("posirite");
            list.add(new StringTextComponent(ChatFormatting.WHITE + "Negarite: " + ChatFormatting.YELLOW + negarite));
            list.add(new StringTextComponent(ChatFormatting.WHITE + "Posirite: " + ChatFormatting.YELLOW + posirite));
        }
    }

}
