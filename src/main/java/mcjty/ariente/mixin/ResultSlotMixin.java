package mcjty.ariente.mixin;

import mcjty.ariente.recipes.BlueprintExchangeEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
abstract class ResultSlotMixin {

    @Inject(method = "onTake", at = @At("HEAD"))
    private void ariente$crafted(Player player, ItemStack stack, CallbackInfo ci) {
        BlueprintExchangeEvents.crafted(player, stack);
    }
}
