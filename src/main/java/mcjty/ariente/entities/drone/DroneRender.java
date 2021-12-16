package mcjty.ariente.entities.drone;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;



public class DroneRender extends LivingRenderer<DroneEntity, DroneModel<DroneEntity>> {
    private ResourceLocation mobTexture = new ResourceLocation("ariente:textures/entity/drone.png");
    private ResourceLocation mobShootingTexture = new ResourceLocation("ariente:textures/entity/drone_shooting.png");

    public DroneRender(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new DroneModel(), 0.8F);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    @Override
    public ResourceLocation getTextureLocation(DroneEntity entity) {
        return entity.isAttacking() ? mobShootingTexture : mobTexture;
    }

    public static final DroneRender.Factory FACTORY = new DroneRender.Factory();

    @Override
    protected void scale(DroneEntity entitylivingbaseIn, PoseStack matrixStackIn, float partialTickTime) {
        GlStateManager._scalef(1.5F, 1.5F, 1.5F);
        GlStateManager._color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static class Factory implements IRenderFactory<DroneEntity> {

        @Override
        public EntityRenderer<? super DroneEntity> createRenderFor(EntityRendererProvider.Context manager) {
            return new DroneRender(manager);
        }

    }

}