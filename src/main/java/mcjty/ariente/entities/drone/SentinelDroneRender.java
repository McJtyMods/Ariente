package mcjty.ariente.entities.drone;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class SentinelDroneRender extends LivingRenderer<SentinelDroneEntity, DroneModel<SentinelDroneEntity>> {
    private ResourceLocation mobTexture = new ResourceLocation("ariente:textures/entity/sentinel_drone.png");

    public SentinelDroneRender(EntityRendererManager rendererManager) {
        super(rendererManager, new DroneModel<SentinelDroneEntity>(), 0.5F);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    @Override
    public ResourceLocation getEntityTexture(SentinelDroneEntity entity) {
        return mobTexture;
    }

//    public static final SentinelDroneRender.Factory FACTORY = new SentinelDroneRender.Factory();


    @Override
    protected void preRenderCallback(SentinelDroneEntity entitylivingbaseIn, MatrixStack matrixStackIn, float partialTickTime) {
//        GlStateManager.scale(1.5F, 1.5F, 1.5F);
        GlStateManager.scalef(1F, 1F, 1F);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static final Factory FACTORY = new Factory();

    public static class Factory implements IRenderFactory<SentinelDroneEntity> {

        @Override
        public EntityRenderer<? super SentinelDroneEntity> createRenderFor(EntityRendererManager manager) {
            return new SentinelDroneRender(manager);
        }

    }

}