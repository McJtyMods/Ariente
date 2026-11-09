package mcjty.ariente.items.armor;

import java.util.ArrayList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;

import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.*;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import mcjty.ariente.Ariente;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.bindings.KeyBindings;
import mcjty.ariente.items.ItemData;
import mcjty.ariente.items.modules.ModuleSupport;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.apache.commons.lang3.tuple.Pair;

public class PowerSuit extends Item {

    private final EquipmentSlot slot;

    private static final UUID[] ARMOR_EXT_MODIFIERS = new UUID[] {
            UUID.fromString("3849db89-6795-4a88-b6d2-44bbba26bbd0"), UUID.fromString("3849db89-6795-4a88-b6d2-44bbba26bbd1"),
            UUID.fromString("3849db89-6795-4a88-b6d2-44bbba26bbd2"), UUID.fromString("3849db89-6795-4a88-b6d2-44bbba26bbd3")
    };

    private static final UUID STEP_MODIFIER = UUID.fromString("4fcb600f-765f-4e5d-becb-984077d32c91");

    public PowerSuit(Item.Properties properties, EquipmentSlot slot) {
        super(properties.stacksTo(1).component(DataComponents.EQUIPPABLE,
                Equippable.builder(slot).setDamageOnHurt(false)
                        .setAsset(ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.parse("ariente:powersuit"))).build()));
        this.slot = slot;
    }

    public boolean canBeDepleted() {
        // ArmorItem replaces zero durability with the material's default, so disable wear explicitly.
        return false;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 0;
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        boolean upgraded = ModuleSupport.hasWorkingUpgrade(stack, ArmorUpgradeType.ARMOR);
        EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(slot);
        Identifier id = Identifier.fromNamespaceAndPath(Ariente.MODID, "armor_" + slot.getName());
        ItemAttributeModifiers modifiers = super.getDefaultAttributeModifiers(stack)
                .withModifierAdded(Attributes.ARMOR, new AttributeModifier(id, upgraded ? 6 : 0, AttributeModifier.Operation.ADD_VALUE), group)
                .withModifierAdded(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(id, upgraded ? 4 : 0, AttributeModifier.Operation.ADD_VALUE), group);
        if (slot == EquipmentSlot.FEET && ModuleSupport.hasWorkingUpgrade(stack, ArmorUpgradeType.STEPASSIST)) {
            modifiers = modifiers.withModifierAdded(Attributes.STEP_HEIGHT,
                    new AttributeModifier(Identifier.fromNamespaceAndPath(Ariente.MODID, "step_assist"), 1.302, AttributeModifier.Operation.ADD_VALUE), group);
        }
        return modifiers;
    }

    public static void absorbEnergyDamage(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        if (entity.level().isClientSide() || event.getNewDamage() <= 0 || !source.is(DamageTypeTags.BYPASSES_ARMOR)
                || !(source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC)
                || source.is(DamageTypeTags.IS_PROJECTILE) || source.getDirectEntity() instanceof Projectile)) {
            return;
        }
        int upgrades = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                ItemStack stack = entity.getItemBySlot(slot);
                if (stack.getItem() instanceof PowerSuit suit && suit.slot == slot
                        && ModuleSupport.hasWorkingUpgrade(stack, ArmorUpgradeType.ARMOR)) {
                    upgrades++;
                }
            }
        }
        // Each working piece absorbs 20%; a full suit leaves 20% for the forcefield to handle.
        if (upgrades > 0) {
            event.setNewDamage(event.getNewDamage() * (5 - upgrades) / 5.0f);
        }
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(PowerSuitModel.EXTENSIONS);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, EquipmentSlot itemSlot) {
        if (!world.isClientSide() && entity instanceof LivingEntity living) {
            if (living.isAlive() && living.getItemBySlot(slot) == stack) {
                ModuleSupport.updateWorkingState(stack, living);
            } else {
                ModuleSupport.clearWorkingState(stack);
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> output, TooltipFlag flag) {
        List<Component> lines = new ArrayList<>();
        appendHoverText(stack, context, lines, flag);
        lines.forEach(output);
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext world, List<Component> list, TooltipFlag b) {

        list.add(Component.literal("Power suit part"));
        list.add(Component.translatable("message.ariente.equipment.shared_power"));
        if (KeyBindings.configureArmor != null) {
            list.add(Component.literal(ChatFormatting.GRAY + "Configure with: " + ChatFormatting.WHITE + "key " + KeyBindings.configureArmor.getDisplayName()));
        }
        if (ItemData.has(stack)) {
            CompoundTag compound = ItemData.get(stack);
            for (ArmorUpgradeType type : ArmorUpgradeType.VALUES) {
                String key = "module_" + type.getName();
                if (compound.contains(key)) {
                    boolean activated = compound.getBooleanOr(key, false);
                    if (activated) {
                        list.add(Component.literal("    " + ChatFormatting.GREEN + type.getDescription() + " (on)"));
                    } else {
                        list.add(Component.literal("    " + ChatFormatting.GRAY + type.getDescription() + " (off)"));
                    }
                }
            }
            Pair<Integer, Integer> usage = ModuleSupport.getPowerUsage(stack);
            list.add(Component.literal(ChatFormatting.WHITE + "Power: " + ChatFormatting.YELLOW + usage.getLeft() + " / " + usage.getRight()));
        }
    }

}
