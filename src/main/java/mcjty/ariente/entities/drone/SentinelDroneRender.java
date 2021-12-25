package mcjty.ariente.entities.drone;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class SentinelDroneRender extends LivingEntityRenderer<SentinelDroneEntity, DroneModel<SentinelDroneEntity>> {
    private ResourceLocation mobTexture = new ResourceLocation("ariente:textures/entity/sentinel_drone.png");

    public SentinelDroneRender(Context context) {
        super(context, new DroneModel<SentinelDroneEntity>(context.bakeLayer(DroneModel.MODEL)), 0.5F);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    @Override
    public ResourceLocation getTextureLocation(SentinelDroneEntity entity) {
        return mobTexture;
    }

    @Override
    protected void scale(SentinelDroneEntity entitylivingbaseIn, PoseStack matrixStackIn, float partialTickTime) {
        matrixStackIn.scale(1F, 1F, 1F);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

}