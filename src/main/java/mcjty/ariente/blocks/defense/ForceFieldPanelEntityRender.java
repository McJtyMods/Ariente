package mcjty.ariente.blocks.defense;

import mcjty.ariente.Ariente;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import javax.annotation.Nullable;

public class ForceFieldPanelEntityRender extends Render<ForceFieldPanelEntity> {

    private static final ResourceLocation guiBackground = new ResourceLocation(Ariente.MODID, "textures/entity/forcefield.png");

    public ForceFieldPanelEntityRender(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(ForceFieldPanelEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
//        Tessellator t = Tessellator.getInstance();
//        BufferBuilder builder = t.getBuffer();
//
//        Minecraft.getMinecraft().entityRenderer.disableLightmap();
//
//        GlStateManager.pushMatrix();
//        GlStateManager.translate(x, y, z);
//
//        GlStateManager.enableTexture2D();
//        GlStateManager.disableLighting();
//        GlStateManager.enableBlend();
//        GlStateManager.depthMask(false);
//        GlStateManager.color(1.0f, 1.0f, 1.0f, 0.2f);
//
//        Minecraft mc = Minecraft.getMinecraft();
//
//        mc.renderEngine.bindTexture(guiBackground);
//
//        builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX);
//
//        int index = entity.getIndex();
//        Triangle triangle = PentakisDodecahedron.getTriangle(index);
//
//        float scale = entity.getScale();
//        Vec3d offs = triangle.getMid().scale(scale);
////        GlStateManager.translate(offs.x, offs.y, offs.z);
//
//        Vec3d a = triangle.getA().scale(scale).subtract(offs);
//        Vec3d b = triangle.getB().scale(scale).subtract(offs);
//        Vec3d c = triangle.getC().scale(scale).subtract(offs);
//
//        builder.pos(a.x, a.y, a.z).tex(0, 0).endVertex();
//        builder.pos(b.x, b.y, b.z).tex(1, 0).endVertex();
//        builder.pos(c.x, c.y, c.z).tex(0, 1).endVertex();
//
//        builder.pos(c.x, c.y, c.z).tex(0, 1).endVertex();
//        builder.pos(b.x, b.y, b.z).tex(1, 0).endVertex();
//        builder.pos(a.x, a.y, a.z).tex(0, 0).endVertex();
//
//        t.draw();
//
//
//        GlStateManager.popMatrix();
//        GlStateManager.enableTexture2D();
//        GlStateManager.depthMask(true);
//        GlStateManager.enableLighting();
//        Minecraft.getMinecraft().entityRenderer.enableLightmap();
    }

    @Override
    public boolean shouldRender(ForceFieldPanelEntity livingEntity, ICamera camera, double camX, double camY, double camZ) {
        boolean b = super.shouldRender(livingEntity, camera, camX, camY, camZ);
        return b;
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(ForceFieldPanelEntity entity) {
        return null;
    }


    public static class Factory implements IRenderFactory<ForceFieldPanelEntity> {

        @Override
        public Render<? super ForceFieldPanelEntity> createRenderFor(RenderManager manager) {
            return new ForceFieldPanelEntityRender(manager);
        }

    }
}
