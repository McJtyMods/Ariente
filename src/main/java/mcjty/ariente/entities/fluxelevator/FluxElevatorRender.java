package mcjty.ariente.entities.fluxelevator;

import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.ariente.Ariente;
import mcjty.hologui.api.IHoloGuiEntity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.registry.IRenderFactory;



public class FluxElevatorRender extends EntityRenderer<FluxElevatorEntity> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("ariente:textures/entity/flux_levitator.png");
    private EntityModel model = new FluxElevatorModel();

    public FluxElevatorRender(EntityRendererManager renderManagerIn) {
        super(renderManagerIn);
        this.shadowSize = 0.5F;
    }

    @Override
    public void doRender(FluxElevatorEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
//        long i = entity.getEntityId() * 493286711L;
//        i = i * i * 4392167121L + i * 98761L;
//        float fx = (((i >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
//        float fy = (((i >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
//        float fz = (((i >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
//        GlStateManager.translate(fx, fy, fz);

        double interX = entity.lastTickPosX + (entity.getPosX() - entity.lastTickPosX) * partialTicks;
        double interY = entity.lastTickPosY + (entity.getPosY() - entity.lastTickPosY) * partialTicks;
        double interZ = entity.lastTickPosZ + (entity.getPosZ() - entity.lastTickPosZ) * partialTicks;
        Vec3d vec3d = entity.getPos(interX, interY, interZ);
        float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;


        if (vec3d != null) {
            Vec3d vec3d1 = entity.getPosOffset(interX, interY, interZ, 0.3D);
            Vec3d vec3d2 = entity.getPosOffset(interX, interY, interZ, -0.3D);

            if (vec3d1 == null) {
                vec3d1 = vec3d;
            }

            if (vec3d2 == null) {
                vec3d2 = vec3d;
            }

            x += vec3d.x - interX;
            y += (vec3d1.y + vec3d2.y) / 2.0D - interY;
            z += vec3d.z - interZ;
            Vec3d vec3d3 = vec3d2.add(-vec3d1.x, -vec3d1.y, -vec3d1.z);

            if (vec3d3.length() != 0.0D) {
                vec3d3 = vec3d3.normalize();
                entityYaw = (float) (Math.atan2(vec3d3.z, vec3d3.x) * 180.0D / Math.PI);
                pitch = (float) (Math.atan(vec3d3.y) * 73.0D);
            }
        }

        GlStateManager.translatef((float) x, (float) y + 0.375F, (float) z);
        GlStateManager.rotatef(180.0F - entityYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(-pitch, 0.0F, 0.0F, 1.0F);
        float f5 = 0 - partialTicks;    // @todo
        float f6 = entity.getDamage() - partialTicks;

        if (f6 < 0.0F) {
            f6 = 0.0F;
        }

        if (f5 > 0.0F) {
            GlStateManager.rotatef(MathHelper.sin(f5) * f5 * f6 / 10.0F * 0, 1.0F, 0.0F, 0.0F);  // @todo
        }

        this.bindEntityTexture(entity);

        if (this.renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(entity));
        }

        GlStateManager.scalef(-1.0F, -1.0F, 1.0F);
        this.model.render(entity, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);

        IHoloGuiEntity holoGui = entity.getHoloGui();
        if (holoGui != null) {
            GlStateManager.scalef(-1.0F, -1.0F, 1.0F);
            Ariente.guiHandler.render(holoGui, 1, 0, 0, -90);
        }

        GlStateManager.popMatrix();

        if (this.renderOutlines) {
            GlStateManager.tearDownSolidRenderingTextureCombine();
            GlStateManager.disableColorMaterial();
        }

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(FluxElevatorEntity entity) {
        return TEXTURE;
    }

    public static class Factory implements IRenderFactory<FluxElevatorEntity> {

        @Override
        public EntityRenderer<? super FluxElevatorEntity> createRenderFor(EntityRendererManager manager) {
            return new FluxElevatorRender(manager);
        }

    }
}