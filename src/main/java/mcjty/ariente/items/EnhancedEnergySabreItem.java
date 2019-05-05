package mcjty.ariente.items;

import com.google.common.collect.Multimap;
import mcjty.ariente.bindings.KeyBindings;
import mcjty.ariente.entities.soldier.MasterSoldierEntity;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.items.modules.ModuleSupport;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnhancedEnergySabreItem extends EnergySabreItem {

    public EnhancedEnergySabreItem(String name) {
        super(name);
        setMaxDamage(0);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        super.hitEntity(stack, target, attacker);
        if (ModuleSupport.hasWorkingUpgrade(stack, ArmorUpgradeType.INHIBIT) && target instanceof MasterSoldierEntity) {
            ((MasterSoldierEntity) target).setNoregenCounter(200);
        }
        return true;
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = super.getOriginalAttributeModifiers(slot, stack);

        if (slot == EntityEquipmentSlot.MAINHAND) {
            float factor = ModuleSupport.hasWorkingUpgrade(stack, ArmorUpgradeType.POWER) ? 2 : 1;
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", this.attackDamage * factor, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, 0));
        }

        return multimap;
    }


    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> list, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, list, flagIn);
        list.add(TextFormatting.GOLD + "Modular energy sabre");
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

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof EntityLivingBase && !world.isRemote) {
            if (itemSlot != EntityEquipmentSlot.MAINHAND.getIndex()) {
                return;
            }
            onUpdateSabre(stack, world, (EntityLivingBase) entity);
        }
    }

    private void onUpdateSabre(ItemStack stack, World world, EntityLivingBase entity) {
        if (stack.isEmpty() || stack.getItem() != ModItems.enhancedEnergySabreItem || !stack.hasTagCompound()) {
            return;
        }

        NBTTagCompound compound = stack.getTagCompound();

        if (!ModuleSupport.managePower(stack, entity)) {
            compound.setBoolean(ArmorUpgradeType.INHIBIT.getWorkingKey(), false);
            compound.setBoolean(ArmorUpgradeType.POWER.getWorkingKey(), false);
            int lootingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING, stack);
            int fireLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, stack);
            if (lootingLevel > 0 || fireLevel > 0) {
                EnchantmentHelper.setEnchantments(Collections.emptyMap(), stack);
            }
            return;
        }

        compound.setBoolean(ArmorUpgradeType.INHIBIT.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.INHIBIT.getModuleKey()));
        compound.setBoolean(ArmorUpgradeType.POWER.getWorkingKey(), compound.getBoolean(ArmorUpgradeType.POWER.getModuleKey()));

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