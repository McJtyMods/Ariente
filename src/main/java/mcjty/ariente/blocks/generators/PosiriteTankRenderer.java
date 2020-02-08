package mcjty.ariente.blocks.generators;

import com.mojang.blaze3d.matrix.MatrixStack;
import mcjty.ariente.Ariente;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;

import java.util.Random;

public class PosiriteTankRenderer extends TileEntityRenderer<PosiriteTankTile> {

    private ResourceLocation halo = new ResourceLocation(Ariente.MODID, "textures/blocks/machines/posirite_beam.png");
    private Random random = new Random();

    public PosiriteTankRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    private static void p(BufferBuilder renderer, double x, double y, double z, double u, double v) {
        renderer.pos(x, y, z).tex((float)u, (float)v).color(1.0f, 1.0f, 1.0f, 1.0f).lightmap(0, 240).endVertex();
    }

    @Override
    public void render(PosiriteTankTile te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
// @todo 1.15
        //        if (te.isWorking()) {
//            Tessellator tessellator = Tessellator.getInstance();
//            GlStateManager.pushMatrix();
//            GlStateManager.translated(x, y, z);
//
//            GlStateManager.enableBlend();
//            GlStateManager.depthMask(false);
//            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
//            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
//            GlStateManager.disableCull();
//            GlStateManager.enableDepthTest();
//
//            ResourceLocation beamIcon = halo;
//            bindTexture(beamIcon);
//
//            long ticks = (System.currentTimeMillis() / 100) % 10;
//            float i1 = ticks / 10.0f;
//            float i2 = i1 + .1f;
//
//            GlStateManager.color4f(1, 1, 1, 1);
//
//            BufferBuilder renderer = tessellator.getBuffer();
//            renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
//
//            double o = .15;
//            double height = 1;
//            p(renderer, o, height, o, 1, i2);
//            p(renderer, 1-o, height, o, 0, i2);
//            p(renderer, 1-o, 0, o, 0, i1);
//            p(renderer, o, 0, o, 1, i1);
//
//            p(renderer, 1-o, height, 1-o, 1, i2);
//            p(renderer, o, height, 1-o, 0, i2);
//            p(renderer, o, 0, 1-o, 0, i1);
//            p(renderer, 1-o, 0, 1-o, 1, i2);
//
//            p(renderer, o, height, 1-o, 1, i2);
//            p(renderer, o, height, o, 0, i2);
//            p(renderer, o, 0, o, 0, i1);
//            p(renderer, o, 0, 1-o, 1, i2);
//
//            p(renderer, 1-o, height, o, 1, i2);
//            p(renderer, 1-o, height, 1-o, 0, i2);
//            p(renderer, 1-o, 0, 1-o, 0, i1);
//            p(renderer, 1-o, 0, o, 1, i2);
//
//            tessellator.draw();
//
//            GlStateManager.depthMask(true);
//            GlStateManager.enableLighting();
//            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
//
//            GlStateManager.popMatrix();
//        }
//
    }

    public static void register() {
        // @todo 1.15
//        ClientRegistry.bindTileEntitySpecialRenderer(PosiriteTankTile.class, new PosiriteTankRenderer());
    }
}
