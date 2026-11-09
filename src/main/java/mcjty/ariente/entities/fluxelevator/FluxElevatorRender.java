package mcjty.ariente.entities.fluxelevator;

import mcjty.ariente.client.ExtractedEntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class FluxElevatorRender extends ExtractedEntityRenderer<FluxElevatorEntity> {

    private static final Identifier TEXTURE = Identifier.parse("ariente:textures/entity/flux_levitator.png");
    private final FluxElevatorModel model = new FluxElevatorModel();

    public FluxElevatorRender(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn);
        this.shadowRadius = 0.5F;
    }

    @Override
    public void render(FluxElevatorEntity entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLightIn) {
        matrixStack.pushPose();

        double interX = entity.xOld + (entity.getX() - entity.xOld) * partialTicks;
        double interY = entity.yOld + (entity.getY() - entity.yOld) * partialTicks;
        double interZ = entity.zOld + (entity.getZ() - entity.zOld) * partialTicks;
        Vec3 vec3d = entity.getPos(interX, interY, interZ);
        float pitch = entity.xRotO + (entity.getXRot()- entity.xRotO) * partialTicks;

        if (vec3d != null) {
            Vec3 vec3d1 = entity.getPosOffset(interX, interY, interZ, 0.3D);
            Vec3 vec3d2 = entity.getPosOffset(interX, interY, interZ, -0.3D);

            if (vec3d1 == null) {
                vec3d1 = vec3d;
            }

            if (vec3d2 == null) {
                vec3d2 = vec3d;
            }

            Vec3 vec3d3 = vec3d2.add(-vec3d1.x, -vec3d1.y, -vec3d1.z);

            if (vec3d3.length() != 0.0D) {
                vec3d3 = vec3d3.normalize();
                entityYaw = (float) (Math.atan2(vec3d3.z, vec3d3.x) * 180.0D / Math.PI);
                pitch = (float) (Math.atan(vec3d3.y) * 73.0D);
            }
        }

        matrixStack.translate(0, .375, 0);
        matrixStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(-pitch));
        float f5 = 0 - partialTicks;    // @todo
        float f6 = entity.getDamage() - partialTicks;

        if (f6 < 0.0F) {
            f6 = 0.0F;
        }

        if (f5 > 0.0F) {
            matrixStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(f5) * f5 * f6 / 10.0F * 0));
        }

        matrixStack.scale(-1, -1, 1);
        model.renderToBuffer(matrixStack, buffer.getBuffer(model.renderType(TEXTURE)), packedLightIn, OverlayTexture.NO_OVERLAY, -1);
        matrixStack.popPose();
    }

    @Override
    public Identifier getTextureLocation(FluxElevatorEntity entity) {
        return TEXTURE;
    }

    public static class Factory implements EntityRendererProvider<FluxElevatorEntity> {

        @Override
        public EntityRenderer<FluxElevatorEntity, ?> create(EntityRendererProvider.Context manager) {
            return new FluxElevatorRender(manager);
        }
    }
}
