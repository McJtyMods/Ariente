package mcjty.ariente.blocks.defense;

import mcjty.ariente.Ariente;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public class ForceFieldPanelEntityRender extends Render<ForceFieldPanelEntity> {

    private static final ResourceLocation guiBackground = new ResourceLocation(Ariente.MODID, "textures/gui/hologui.png");

    public ForceFieldPanelEntityRender(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(ForceFieldPanelEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        Tessellator t = Tessellator.getInstance();
        BufferBuilder builder = t.getBuffer();

        Minecraft.getMinecraft().entityRenderer.disableLightmap();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 0.5f);

        Minecraft mc = Minecraft.getMinecraft();

        mc.renderEngine.bindTexture(guiBackground);

        builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX);

        int index = entity.getIndex();
        PentakisDodecahedron.Triangle triangle = PentakisDodecahedron.getTriangle(index);

        Vec3d a = triangle.getA().scale(10);
        Vec3d b = triangle.getB().scale(10);
        Vec3d c = triangle.getC().scale(10);

        builder.pos(a.x, a.y, a.z).tex(0, 0).endVertex();
        builder.pos(b.x, b.y, b.z).tex(1, 0).endVertex();
        builder.pos(c.x, c.y, c.z).tex(1, 1).endVertex();

        builder.pos(c.x, c.y, c.z).tex(1, 1).endVertex();
        builder.pos(b.x, b.y, b.z).tex(1, 0).endVertex();
        builder.pos(a.x, a.y, a.z).tex(0, 0).endVertex();

        t.draw();


        GlStateManager.popMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        Minecraft.getMinecraft().entityRenderer.enableLightmap();
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
