package mcjty.ariente.items.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import mcjty.ariente.Ariente;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.bindings.KeyBindings;
import mcjty.ariente.items.modules.ModuleSupport;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeMod;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class PowerSuit extends ArmorItem {

    private final EquipmentSlot slot;

    private static final UUID[] ARMOR_EXT_MODIFIERS = new UUID[] {
            UUID.fromString("3849db89-6795-4a88-b6d2-44bbba26bbd0"), UUID.fromString("3849db89-6795-4a88-b6d2-44bbba26bbd1"),
            UUID.fromString("3849db89-6795-4a88-b6d2-44bbba26bbd2"), UUID.fromString("3849db89-6795-4a88-b6d2-44bbba26bbd3")
    };

    private static final UUID STEP_MODIFIER = UUID.fromString("4fcb600f-765f-4e5d-becb-984077d32c91");

    public PowerSuit(EquipmentSlot slot) {
        super(ArmorMaterials.LEATHER, switch (slot) {
            case HEAD -> Type.HELMET;
            case CHEST -> Type.CHESTPLATE;
            case LEGS -> Type.LEGGINGS;
            case FEET -> Type.BOOTS;
            default -> throw new IllegalArgumentException("Not an armor slot: " + slot);
        }, new Properties().durability(0));
        this.slot = slot;
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

        if (slot == EquipmentSlot.FEET && ModuleSupport.hasWorkingUpgrade(stack, ArmorUpgradeType.STEPASSIST)) {
            builder.put(ForgeMod.STEP_HEIGHT_ADDITION.get(), new AttributeModifier(STEP_MODIFIER,
                    "Ariente step assist", 1.302, AttributeModifier.Operation.ADDITION));
        }
        return builder.build();
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(PowerSuitModel.EXTENSIONS);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected) {
        if (!world.isClientSide && entity instanceof LivingEntity living) {
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
    public void appendHoverText(ItemStack stack, Level world, List<Component> list, TooltipFlag b) {
        super.appendHoverText(stack, world, list, b);
        list.add(Component.literal("Power suit part"));
        list.add(Component.translatable("message.ariente.equipment.shared_power"));
        if (KeyBindings.configureArmor != null) {
            list.add(Component.literal(ChatFormatting.GRAY + "Configure with: " + ChatFormatting.WHITE + "key " + KeyBindings.configureArmor.getDisplayName()));
        }
        if (stack.hasTag()) {
            CompoundTag compound = stack.getTag();
            for (ArmorUpgradeType type : ArmorUpgradeType.VALUES) {
                String key = "module_" + type.getName();
                if (compound.contains(key)) {
                    boolean activated = compound.getBoolean(key);
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
