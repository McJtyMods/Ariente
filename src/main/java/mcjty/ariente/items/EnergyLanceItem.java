package mcjty.ariente.items;

import java.util.ArrayList;

import mcjty.ariente.varia.Messages;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.List;
import java.util.function.Consumer;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.api.ISoldier;
import mcjty.ariente.entities.LaserEntity;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.items.modules.WeaponModuleSupport;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Tools;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EnergyLanceItem extends Item implements ITooltipSettings {

    public static final float DAMAGE_MULTIPLIER = 1.3F;
    public static final int SHOT_INTERVAL = 20;
    public static final float RANGE = 24.0F;

    private final TooltipBuilder tooltipBuilder = WeaponModuleSupport.createTooltipBuilder();

    public EnergyLanceItem(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    public static boolean canFire(LivingEntity shooter) {
        return shooter.getMainHandItem().getItem() instanceof EnergyLanceItem && shooter.getOffhandItem().isEmpty();
    }

    public static float getShotDamage(LivingEntity shooter) {
        // The lance has no melee modifier. Include the sabre's modifier and the wielder's base damage.
        float factor = ModuleSupport.hasWorkingUpgrade(shooter.getMainHandItem(), ArmorUpgradeType.POWER) ? 2 : 1;
        float sabreDamage = (float) shooter.getAttributeValue(Attributes.ATTACK_DAMAGE) + EnergySabreItem.BASE_DAMAGE * factor;
        if (shooter instanceof ISoldier soldier) {
            sabreDamage = Math.max(sabreDamage, soldier.getMinimumSabreDamage() * factor);
        }
        return sabreDamage * DAMAGE_MULTIPLIER;
    }

    public static boolean fire(LivingEntity shooter, Vec3 direction) {
        if (shooter.level().isClientSide() || !canFire(shooter) || direction.lengthSqr() < 1.0E-7D) {
            return false;
        }
        return shooter.level().addFreshEntity(LaserEntity.createLanceShot(shooter.level(), shooter, direction, getShotDamage(shooter)));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || !canFire(player)) {
            Messages.player(player, Component.translatable("message.ariente.energy_lance.two_handed"), true);
            return InteractionResult.FAIL;
        }
        if (player.getCooldowns().isOnCooldown(player.getItemInHand(hand))) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide() && fire(player, player.getLookAngle())) {
            player.getCooldowns().addCooldown(player.getItemInHand(hand), SHOT_INTERVAL);
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        // Keep the muzzle steady when firing instead of playing the melee swing animation.
        return InteractionResult.CONSUME;
    }

    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return !player.getOffhandItem().isEmpty();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> output, TooltipFlag flag) {
        List<Component> lines = new ArrayList<>();
        appendHoverText(stack, context, lines, flag);
        lines.forEach(output);
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext level, List<Component> tooltip, TooltipFlag flag) {
        tooltipBuilder.makeTooltip(Tools.getId(this), stack, tooltip, flag);
        tooltip.add(Component.translatable("message.ariente.energy_lance.two_handed"));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot itemSlot) {
        WeaponModuleSupport.inventoryTick(stack, level, entity, entity instanceof LivingEntity living && living.getOffhandItem().isEmpty());
    }

    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }


}
