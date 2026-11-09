package mcjty.ariente.mixin.client;

import mcjty.ariente.display.client.LiveCameraRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
abstract class LevelRendererMixin {

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void ariente$updateLiveCameras(CallbackInfo ci) {
        LiveCameraRenderer.beginFrame();
    }
}
