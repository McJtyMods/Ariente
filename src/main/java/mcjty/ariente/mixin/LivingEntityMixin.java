package mcjty.ariente.mixin;

import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.items.armor.DamageContext;
import mcjty.ariente.items.armor.PersonalForcefield;
import mcjty.ariente.items.armor.PowerSuit;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.setup.Registration;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {

    @Inject(method = "getDamageAfterMagicAbsorb", at = @At("RETURN"), cancellable = true)
    private void ariente$absorbDamage(DamageSource source, float damage, CallbackInfoReturnable<Float> cir) {
        var event = new DamageContext((LivingEntity) (Object) this, source, cir.getReturnValue());
        PowerSuit.absorbEnergyDamage(event);
        PersonalForcefield.absorb(event);
        cir.setReturnValue(event.getNewDamage());
    }

    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void ariente$featherFalling(double distance, float multiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        var feet = ((LivingEntity) (Object) this).getItemBySlot(EquipmentSlot.FEET);
        if (feet.is(Registration.POWERSUIT_FEET.get()) && ModuleSupport.hasWorkingUpgrade(feet, ArmorUpgradeType.FEATHERFALLING)) {
            cir.setReturnValue(false);
        }
    }
}
