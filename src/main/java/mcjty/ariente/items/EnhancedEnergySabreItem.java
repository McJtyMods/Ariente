package mcjty.ariente.items;

import com.google.common.collect.Multimap;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.bindings.KeyBindings;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.entities.soldier.MasterSoldierEntity;
import mcjty.ariente.items.modules.ModuleSupport;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;

public class EnhancedEnergySabreItem extends EnergySabreItem {

    public EnhancedEnergySabreItem() {
        super();
        // @todo 1.14 custom properties?
//        setMaxDamage(0);
    }

    @Override
    public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hitEntity(stack, target, attacker);
        if (ModuleSupport.hasWorkingUpgrade(stack, ArmorUpgradeType.INHIBIT)) {
            if (target instanceof MasterSoldierEntity) {
                ((MasterSoldierEntity) target).setNoregenCounter(200);
            }
            removeGoodEffects(target);
        }
        return true;
    }

    private void removeGoodEffects(LivingEntity target) {
        if (target.getEntityWorld().isRemote) {
            return;
        }
        Iterator<EffectInstance> iterator = target.getActivePotionMap().values().iterator();

        Set<Effect> potionsToRemove = new HashSet<>();
        while (iterator.hasNext()) {
            EffectInstance effect = iterator.next();
            if (effect.getPotion().isBeneficial()) {
                potionsToRemove.add(effect.getPotion());
            }
        }
        for (Effect potion : potionsToRemove) {
            target.removePotionEffect(potion);
        }
    }


    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = super.getOriginalAttributeModifiers(slot, stack);

        if (slot == EquipmentSlotType.MAINHAND) {
            float factor = ModuleSupport.hasWorkingUpgrade(stack, ArmorUpgradeType.POWER) ? 2 : 1;
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", this.attackDamage * factor, AttributeModifier.Operation.ADDITION));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, AttributeModifier.Operation.ADDITION));
        }

        return multimap;
    }


    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> list, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, list, flagIn);
        list.add(new StringTextComponent(TextFormatting.GOLD + "Modular energy sabre"));
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

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof LivingEntity && !world.isRemote) {
            if (itemSlot != EquipmentSlotType.MAINHAND.getIndex()) {
                return;
            }
            onUpdateSabre(stack, world, (LivingEntity) entity);
        }
    }

    private void onUpdateSabre(ItemStack stack, World world, LivingEntity entity) {
        if (stack.isEmpty() || stack.getItem() != ModBlocks.ENHANCED_ENERGY_SABRE.get() || !stack.hasTag()) {
            return;
        }

        CompoundNBT compound = stack.getTag();

        if (!ModuleSupport.managePower(stack, entity)) {
            compound.putBoolean(ArmorUpgradeType.INHIBIT.getWorkingKey(), false);
            compound.putBoolean(ArmorUpgradeType.POWER.getWorkingKey(), false);
            int lootingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING, stack);
            int fireLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, stack);
            if (lootingLevel > 0 || fireLevel > 0) {
                EnchantmentHelper.setEnchantments(Collections.emptyMap(), stack);
            }
            return;
        }

        compound.putBoolean(ArmorUpgradeType.INHIBIT.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.INHIBIT.getModuleKey()));
        compound.putBoolean(ArmorUpgradeType.POWER.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.POWER.getModuleKey()));

        Map<Enchantment, Integer> ench = new HashMap<>();
        if (compound.getBoolean(ArmorUpgradeType.LOOTING.getModuleKey())) {
            ench.put(Enchantments.LOOTING, 3);
        }
        if (compound.getBoolean(ArmorUpgradeType.FIRE.getModuleKey())) {
            ench.put(Enchantments.FIRE_ASPECT, 3);
        }
        EnchantmentHelper.setEnchantments(ench, stack);
    }


}