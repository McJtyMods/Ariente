package mcjty.ariente.entities.levitator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.math.Quaternion;
import mcjty.ariente.Ariente;
import mcjty.hologui.api.IHoloGuiEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;


public class FluxLevitatorRender extends EntityRenderer<FluxLevitatorEntity> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("ariente:textures/entity/flux_levitator.png");
    private EntityModel model = new FluxLevitatorModel();

    public FluxLevitatorRender(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn);
        this.shadowRadius = 0.5F;
    }

    @Override
    public void render(FluxLevitatorEntity entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLightIn) {
        matrixStack.pushPose();
        long i = entity.getId() * 493286711L;
        i = i * i * 4392167121L + i * 98761L;
        float fx = (((i >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float fy = (((i >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float fz = (((i >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        matrixStack.translate(fx, fy, fz);

        double interX = entity.xOld + (entity.getX() - entity.xOld) * partialTicks;
        double interY = entity.yOld + (entity.getY() - entity.yOld) * partialTicks;
        double interZ = entity.zOld + (entity.getZ() - entity.zOld) * partialTicks;
        Vec3 vec3d = entity.getPos(interX, interY, interZ);
        float pitch = entity.xRotO + (entity.getXRot() - entity.xRotO) * partialTicks;

        if (vec3d != null) {
            Vec3 vec3d1 = entity.getPosOffset(interX, interY, interZ, 0.3D);
            Vec3 vec3d2 = entity.getPosOffset(interX, interY, interZ, -0.3D);

            if (vec3d1 == null) {
                vec3d1 = vec3d;
            }

            if (vec3d2 == null) {
                vec3d2 = vec3d;
            }

            double x = vec3d.x - interX;
            double y = (vec3d1.y + vec3d2.y) / 2.0D - interY;
            double z = vec3d.z - interZ;
            Vec3 vec3d3 = vec3d2.add(-vec3d1.x, -vec3d1.y, -vec3d1.z);

            if (vec3d3.length() != 0.0D) {
                vec3d3 = vec3d3.normalize();
                entityYaw = (float) (Math.atan2(vec3d3.z, vec3d3.x) * 180.0D / Math.PI);
                pitch = (float) (Math.atan(vec3d3.y) * 73.0D);
            }

            matrixStack.translate((float) x, (float) y + 0.375F, (float) z);
        }

        matrixStack.mulPose(new Quaternion(180.0F - entityYaw, 0.0F, 1.0F, 0.0F));
        matrixStack.mulPose(new Quaternion(-pitch, 0.0F, 0.0F, 1.0F));
        float f5 = entity.getRollingAmplitude() - partialTicks;
        float f6 = entity.getDamage() - partialTicks;

        if (f6 < 0.0F) {
            f6 = 0.0F;
        }

        if (f5 > 0.0F) {
            matrixStack.mulPose(new Quaternion(Mth.sin(f5) * f5 * f6 / 10.0F * entity.getRollingDirection(), 1.0F, 0.0F, 0.0F));
        }

        // @todo 1.15
//        this.bindEntityTexture(entity);
//
//        if (this.renderOutlines) {
//            GlStateManager.enableColorMaterial();
//            GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(entity));
//        }
//
//        GlStateManager.scalef(-1.0F, -1.0F, 1.0F);
//        this.model.render(entity, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);

        IHoloGuiEntity holoGui = entity.getHoloGuiFront();
        if (holoGui != null) {
            matrixStack.scale(-1.0F, -1.0F, 1.0F);
            Ariente.guiHandler.render(holoGui, 1, 0, 0, -90);
        }
        holoGui = entity.getHoloGuiBack();
        if (holoGui != null) {
            matrixStack.scale(-1.0F, -1.0F, 1.0F);
            matrixStack.mulPose(new Quaternion(180, 1, 0, 0));
            Ariente.guiHandler.render(holoGui, 1, 0, 0, -90);
        }


        matrixStack.popPose();

        // @todo 1.15
//        if (this.renderOutlines) {
//            GlStateManager.tearDownSolidRenderingTextureCombine();
//            GlStateManager.disableColorMaterial();
//        }
//
//        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    public ResourceLocation getTextureLocation(FluxLevitatorEntity entity) {
        return TEXTURE;
    }

    public static class Factory implements EntityRendererProvider<FluxLevitatorEntity> {

        @Override
        public EntityRenderer<FluxLevitatorEntity> create(EntityRendererProvider.Context manager) {
            return new FluxLevitatorRender(manager);
        }

    }
}