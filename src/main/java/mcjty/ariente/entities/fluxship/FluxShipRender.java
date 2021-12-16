package mcjty.ariente.entities.fluxship;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;



public class FluxShipRender extends EntityRenderer<FluxShipEntity> {

    public static final ResourceLocation TEXTURE = new ResourceLocation("ariente:entity/flux_ship");

    // @todo 1.15
//    private static final ModelHandle HANDLE = ModelHandle.of(new ResourceLocation(Ariente.MODID, "entity/flux_ship.obj"))
//            .vertexFormat(DefaultVertexFormats.POSITION_TEX_LMAP_COLOR)
//            .replace("#None", TEXTURE.toString());

    public FluxShipRender(Context renderManagerIn) {
        super(renderManagerIn);
        this.shadowRadius = 0.5F;
    }


    // @todo 1.15
//    @Override
//    public void doRender(FluxShipEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
//        float scale = 1.0f;
//
//        GlStateManager.disableLighting();
//
//        GlStateManager.pushMatrix();
//        GlStateManager.translatef((float) x, (float) y, (float) z);
//        GlStateManager.enableRescaleNormal();
//        GlStateManager.scalef(scale, scale, scale);
//
//        bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
//
//        HANDLE.render();
//
//        GlStateManager.disableRescaleNormal();
//        GlStateManager.popMatrix();
//
//        GlStateManager.enableLighting();
//
//        super.doRender(entity, x, y, z, entityYaw, partialTicks);
//    }

    @Override
    public ResourceLocation getTextureLocation(FluxShipEntity entity) {
        return TEXTURE;
    }

    public static class Factory implements IRenderFactory<FluxShipEntity> {

        @Override
        public EntityRenderer<? super FluxShipEntity> createRenderFor(Context manager) {
            return new FluxShipRender(manager);
        }

    }
}