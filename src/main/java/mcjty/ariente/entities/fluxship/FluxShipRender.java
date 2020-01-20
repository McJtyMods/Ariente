package mcjty.ariente.entities.fluxship;

import mcjty.ariente.Ariente;
import mcjty.ariente.renderer.ModelHandle;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;



@SideOnly(Side.CLIENT)
public class FluxShipRender extends Render<FluxShipEntity> {

    public static final ResourceLocation TEXTURE = new ResourceLocation("ariente:entity/flux_ship");

    private static final ModelHandle HANDLE = ModelHandle.of(new ResourceLocation(Ariente.MODID, "entity/flux_ship.obj"))
            .vertexFormat(DefaultVertexFormats.POSITION_TEX_LMAP_COLOR)
            .replace("#None", TEXTURE.toString());

    public FluxShipRender(RenderManager renderManagerIn) {
        super(renderManagerIn);
        this.shadowSize = 0.5F;
    }

    @Override
    public void doRender(FluxShipEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        float scale = 1.0f;

        GlStateManager.disableLighting();

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(scale, scale, scale);

        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        HANDLE.render();

        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();

        GlStateManager.enableLighting();

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(FluxShipEntity entity) {
        return TEXTURE;
    }

    public static class Factory implements IRenderFactory<FluxShipEntity> {

        @Override
        public Render<? super FluxShipEntity> createRenderFor(RenderManager manager) {
            return new FluxShipRender(manager);
        }

    }
}