package mcjty.ariente.blocks.generators;

import mcjty.ariente.Ariente;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class NegariteTankRenderer extends TileEntitySpecialRenderer<NegariteTankTile> {

    private ResourceLocation halo = new ResourceLocation(Ariente.MODID, "textures/blocks/machines/negarite_beam.png");
    private Random random = new Random();

    public NegariteTankRenderer() {
    }

    private static void p(BufferBuilder renderer, double x, double y, double z, double u, double v) {
        renderer.pos(x, y, z).tex(u, v).color(1.0f, 1.0f, 1.0f, 1.0f).lightmap(0, 240).endVertex();
    }


    @Override
    public void render(NegariteTankTile te, double x, double y, double z, float time, int breakTime, float alpha) {
        if (te.isWorking()) {
            Tessellator tessellator = Tessellator.getInstance();
//            GlStateManager.pushAttrib();
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);

            GlStateManager.enableBlend();
            GlStateManager.depthMask(false);
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
            GlStateManager.disableCull();
            GlStateManager.enableDepth();

            ResourceLocation beamIcon = halo;
            bindTexture(beamIcon);

            long ticks = (System.currentTimeMillis() / 100) % 10;
            float i1 = ticks / 10.0f;
            float i2 = i1 + .1f;

            GlStateManager.color(1, 1, 1, 1);

            BufferBuilder renderer = tessellator.getBuffer();
            renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

            double o = .15;
            double height = 1;
            p(renderer, o, height, o, 1, i2);
            p(renderer, 1-o, height, o, 0, i2);
            p(renderer, 1-o, 0, o, 0, i1);
            p(renderer, o, 0, o, 1, i1);

            p(renderer, 1-o, height, 1-o, 1, i2);
            p(renderer, o, height, 1-o, 0, i2);
            p(renderer, o, 0, 1-o, 0, i1);
            p(renderer, 1-o, 0, 1-o, 1, i2);

            p(renderer, o, height, 1-o, 1, i2);
            p(renderer, o, height, o, 0, i2);
            p(renderer, o, 0, o, 0, i1);
            p(renderer, o, 0, 1-o, 1, i2);

            p(renderer, 1-o, height, o, 1, i2);
            p(renderer, 1-o, height, 1-o, 0, i2);
            p(renderer, 1-o, 0, 1-o, 0, i1);
            p(renderer, 1-o, 0, o, 1, i2);

            tessellator.draw();

            GlStateManager.depthMask(true);
            GlStateManager.enableLighting();
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);

            GlStateManager.popMatrix();
//            GlStateManager.popAttrib();
        }

    }

    public static void register() {
        ClientRegistry.bindTileEntitySpecialRenderer(NegariteTankTile.class, new NegariteTankRenderer());
    }
}
