package mcjty.ariente.mixin.client;

import mcjty.ariente.client.FabricClientEvents;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void ariente$attack(CallbackInfoReturnable<Boolean> cir) {
        if (FabricClientEvents.input(true)) cir.setReturnValue(false);
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void ariente$continueAttack(boolean attack, CallbackInfo ci) {
        if (attack && FabricClientEvents.input(true)) ci.cancel();
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void ariente$use(CallbackInfo ci) {
        if (FabricClientEvents.input(false)) ci.cancel();
    }
}
