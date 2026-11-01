package mcjty.ariente.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.items.modules.WeaponModuleSupport;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Tools;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class EnhancedEnergySabreItem extends EnergySabreItem implements ITooltipSettings {

    private final TooltipBuilder tooltipBuilder = WeaponModuleSupport.createTooltipBuilder();

    public EnhancedEnergySabreItem() {
        super();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (ModuleSupport.hasWorkingUpgrade(stack, ArmorUpgradeType.INHIBIT)) {
            WeaponModuleSupport.inhibit(target);
        }
        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> multimap = super.getOriginalAttributeModifiers(slot, stack);
        if (slot != EquipmentSlot.MAINHAND) {
            return multimap;
        }
        float factor = ModuleSupport.hasWorkingUpgrade(stack, ArmorUpgradeType.POWER) ? 2 : 1;
        return ImmutableMultimap.<Attribute, AttributeModifier>builder()
                .putAll(multimap)
                .put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", attackDamage * factor, AttributeModifier.Operation.ADDITION))
                .put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4000000953674316D, AttributeModifier.Operation.ADDITION))
                .build();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltipBuilder.makeTooltip(Tools.getId(this), stack, tooltip, flag);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean selected) {
        WeaponModuleSupport.inventoryTick(stack, level, entity, true);
    }
}
