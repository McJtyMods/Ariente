package mcjty.ariente.items;

import mcjty.ariente.entities.MasterSoldierEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

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

}