package mcjty.ariente.mixin;

import mcjty.ariente.entities.LanceLooting;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
abstract class EnchantmentHelperMixin {

    @Inject(method = "getItemEnchantmentLevel", at = @At("RETURN"), cancellable = true)
    private static void ariente$lanceLooting(
            Holder<Enchantment> enchantment, ItemStack item, CallbackInfoReturnable<Integer> cir) {
        if (enchantment.is(Enchantments.LOOTING))
            cir.setReturnValue(LanceLooting.level(item, cir.getReturnValue()));
    }
}
