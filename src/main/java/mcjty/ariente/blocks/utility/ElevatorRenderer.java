package mcjty.ariente.blocks.utility;

import mcjty.ariente.Ariente;
import mcjty.lib.client.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class ElevatorRenderer extends TileEntitySpecialRenderer<ElevatorTile> {

    private ResourceLocation halo = new ResourceLocation(Ariente.MODID, "textures/blocks/machines/elevator_beam.png");
    private Random random = new Random();

    public ElevatorRenderer() {
    }

    private static void p(BufferBuilder renderer, double x, double y, double z, double u, double v) {
        renderer.pos(x, y, z).tex(u, v).color(1.0f, 1.0f, 1.0f, 1.0f).lightmap(0, 240).endVertex();
    }


    @Override
    public void render(ElevatorTile te, double x, double y, double z, float time, int breakTime, float alpha) {
//        if (te.isWorking()) {
            Tessellator tessellator = Tessellator.getInstance();
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

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP p = mc.player;
        double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * time;
        double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * time;
        double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * time;

        RenderHelper.Vector player = new RenderHelper.Vector((float) doubleX, (float) doubleY + p.getEyeHeight(), (float) doubleZ);

        long ticks = (System.currentTimeMillis() / 100) % 10;
            float i1 = 0;//ticks / 10.0f;
            float i2 = 1;//i1 + .1f;

            GlStateManager.color(1, 1, 1, 1);

            BufferBuilder renderer = tessellator.getBuffer();
            renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

            double o = .15;
            double height = 3;

            RenderHelper.drawBeam(new RenderHelper.Vector(te.getPos().getX()-.3f, te.getPos().getY() + 0.0f, te.getPos().getZ()-.3f),
                    new RenderHelper.Vector(te.getPos().getX()+.3f, (float) (te.getPos().getY() + height), te.getPos().getZ()+.3f),
                    player, 2.0f);

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

            tessellator.draw();

            GlStateManager.depthMask(true);
            GlStateManager.enableLighting();
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);

            GlStateManager.popMatrix();
//        }

    }
}
