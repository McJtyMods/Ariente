package mcjty.ariente.items;

import java.util.ArrayList;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
import java.util.List;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.items.modules.WeaponModuleSupport;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Tools;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class EnhancedEnergySabreItem extends EnergySabreItem implements ITooltipSettings , AttributeItem {

    private final TooltipBuilder tooltipBuilder = WeaponModuleSupport.createTooltipBuilder();

    public EnhancedEnergySabreItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (ModuleSupport.hasWorkingUpgrade(stack, ArmorUpgradeType.INHIBIT)) {
            WeaponModuleSupport.inhibit(target);
        }
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        float factor = ModuleSupport.hasWorkingUpgrade(stack, ArmorUpgradeType.POWER) ? 2 : 1;
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, attackDamage * factor, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.4000000953674316D, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> output, TooltipFlag flag) {
        List<Component> lines = new ArrayList<>();
        appendHoverText(stack, context, lines, flag);
        lines.forEach(output);
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext level, List<Component> tooltip, TooltipFlag flag) {

        tooltipBuilder.makeTooltip(Tools.getId(this), stack, tooltip, flag);
    }

    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot itemSlot) {
        WeaponModuleSupport.inventoryTick(stack, level, entity, true);
    }
}
