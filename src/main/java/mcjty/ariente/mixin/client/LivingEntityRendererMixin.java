package mcjty.ariente.mixin.client;

import mcjty.ariente.items.EnergyLanceItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.world.entity.HumanoidArm;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.entities.fluxship.FluxShipPilotRender;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
abstract class LivingEntityRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void ariente$pilot(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
        FluxShipPilotRender.extract(entity, state);
        if (state instanceof ArmedEntityRenderState armed
                && entity.getMainHandItem().getItem() instanceof EnergyLanceItem
                && EnergyLanceItem.canFire(entity)) {
            if (entity.getMainArm() == HumanoidArm.RIGHT) {
                armed.rightArmPose = HumanoidModel.ArmPose.CROSSBOW_HOLD;
            } else {
                armed.leftArmPose = HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }
        }
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("HEAD"))
    private void ariente$beforePilot(LivingEntityRenderState state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        FluxShipPilotRender.beforeLiving(state, poses);
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("RETURN"))
    private void ariente$afterPilot(LivingEntityRenderState state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        FluxShipPilotRender.afterLiving(state, poses);
    }
}
