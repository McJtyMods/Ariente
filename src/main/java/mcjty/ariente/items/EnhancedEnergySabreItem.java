package mcjty.ariente.items;

import mcjty.ariente.entities.MasterSoldierEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class EnhancedEnergySabreItem extends EnergySabreItem {

    public EnhancedEnergySabreItem(String name) {
        super(name);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        super.hitEntity(stack, target, attacker);
        if (target instanceof MasterSoldierEntity) {
            ((MasterSoldierEntity) target).setNoregenCounter(200);
        }
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(TextFormatting.GOLD + "This energy sabre is");
        tooltip.add(TextFormatting.GOLD + "extra effective against");
        tooltip.add(TextFormatting.GOLD + "Master Soldiers");
    }
}