package mcjty.ariente.entities.drone;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SentinelDroneRender extends RenderLiving<SentinelDroneEntity> {
    private ResourceLocation mobTexture = new ResourceLocation("ariente:textures/entity/sentinel_drone.png");

    public SentinelDroneRender(RenderManager renderManagerIn) {
        super(renderManagerIn, new DroneModel(), 0.5F);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    @Override
    protected ResourceLocation getEntityTexture(SentinelDroneEntity entity) {
        return mobTexture;
    }

    public static final SentinelDroneRender.Factory FACTORY = new SentinelDroneRender.Factory();

    /**
     * Allows the render to do state modifications necessary before the model is rendered.
     */
    @Override
    protected void preRenderCallback(SentinelDroneEntity entitylivingbaseIn, float partialTickTime) {
//        GlStateManager.scale(1.5F, 1.5F, 1.5F);
        GlStateManager.scale(1F, 1F, 1F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static class Factory implements IRenderFactory<SentinelDroneEntity> {

        @Override
        public Render<? super SentinelDroneEntity> createRenderFor(RenderManager manager) {
            return new SentinelDroneRender(manager);
        }

    }

}