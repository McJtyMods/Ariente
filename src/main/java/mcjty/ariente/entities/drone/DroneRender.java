package mcjty.ariente.entities.drone;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;



public class DroneRender extends LivingRenderer<DroneEntity, DroneModel<DroneEntity>> {
    private ResourceLocation mobTexture = new ResourceLocation("ariente:textures/entity/drone.png");
    private ResourceLocation mobShootingTexture = new ResourceLocation("ariente:textures/entity/drone_shooting.png");

    public DroneRender(EntityRendererManager renderManagerIn) {
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
    protected void scale(DroneEntity entitylivingbaseIn, MatrixStack matrixStackIn, float partialTickTime) {
        GlStateManager._scalef(1.5F, 1.5F, 1.5F);
        GlStateManager._color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static class Factory implements IRenderFactory<DroneEntity> {

        @Override
        public EntityRenderer<? super DroneEntity> createRenderFor(EntityRendererManager manager) {
            return new DroneRender(manager);
        }

    }

}