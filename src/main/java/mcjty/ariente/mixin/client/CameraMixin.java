package mcjty.ariente.mixin.client;

import mcjty.ariente.client.CameraAngles;
import mcjty.ariente.entities.fluxship.FluxShipClient;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
abstract class CameraMixin {

    @Shadow @Final private Vector3f forwards;
    @Shadow @Final private Vector3f up;
    @Shadow @Final private Vector3f left;

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "setup", at = @At("TAIL"))
    private void ariente$cabinCamera(
            BlockGetter level,
            Entity entity,
            boolean detached,
            boolean mirrored,
            float partialTick,
            CallbackInfo ci) {
        Camera camera = (Camera) (Object) this;
        var angles = new CameraAngles(camera, partialTick);
        FluxShipClient.cameraAngles(angles);
        if (angles.getYaw() != camera.getYRot()
                || angles.getPitch() != camera.getXRot()
                || angles.getRoll() != 0) {
            setRotation(angles.getYaw(), angles.getPitch());
            camera.rotation().rotateZ(angles.getRoll() * Mth.DEG_TO_RAD);
            forwards.set(0, 0, -1).rotate(camera.rotation());
            up.set(0, 1, 0).rotate(camera.rotation());
            left.set(-1, 0, 0).rotate(camera.rotation());
        }
    }
}
