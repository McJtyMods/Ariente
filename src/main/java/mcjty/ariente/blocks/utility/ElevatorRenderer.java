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

    private static float randomX[] = new float[]{.2f, .3f, .2f, .7f, .8f, .5f, .2f, .8f, .4f, .6f};
    private static float randomZ[] = new float[]{.3f, .2f, .8f, .3f, .7f, .6f, .4f, .5f, .2f, .3f};
    private static int randomY[] = new int[]{0, 3, 2, 1, 6, 5, 6, 8, 2, 3};

    @Override
    public void render(ElevatorTile te, double x, double y, double z, float time, int breakTime, float alpha) {
//        if (te.isWorking()) {
        Tessellator tessellator = Tessellator.getInstance();
        GlStateManager.pushMatrix();
//            GlStateManager.translate(x, y, z);

        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);
//        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
//        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
        GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ONE);
//        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
        GlStateManager.disableCull();
        GlStateManager.enableDepth();

        ResourceLocation beamIcon = halo;
        bindTexture(beamIcon);

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP p = mc.player;
        double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * time;
        double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * time;
        double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * time;

        GlStateManager.translate(-doubleX, -doubleY, -doubleZ);

        RenderHelper.Vector player = new RenderHelper.Vector((float) doubleX, (float) doubleY + p.getEyeHeight(), (float) doubleZ);

        long tt = System.currentTimeMillis() / 100;

        GlStateManager.color(1, 1, 1, 1);

        BufferBuilder renderer = tessellator.getBuffer();
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

        float height = te.getHeight();

        for (int i = 0; i < 5 + te.getHeight() / 2; i++) {
            int ii = i % 10;
            long ticks = (tt + randomY[ii]) % 80;
            if (ticks > 40) {
                ticks = 80 - ticks;
            }
            float i1 = ticks / 40.0f;
            float xx = te.getPos().getX() + randomX[ii];
            float zz = te.getPos().getZ() + randomZ[ii];
            float yy = te.getPos().getY() - 1.0f + i1 + (randomY[ii] * height) / 8.0f;
            RenderHelper.drawBeam(new RenderHelper.Vector(xx, yy, zz), new RenderHelper.Vector(xx, yy + 4, zz), player, 0.2f);
        }

//        net.minecraft.util.math.Vec3d cameraPos = net.minecraft.client.renderer.ActiveRenderInfo.getCameraPosition();
//        tessellator.getBuffer().sortVertexData((float) (player.x + doubleX), (float) (player.y + doubleY), (float) (player.z + doubleZ));
//        tessellator.getBuffer().sortVertexData((float)(cameraPos.x+doubleX), (float)(cameraPos.y+doubleY), (float)(cameraPos.z+doubleZ));
        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);

        GlStateManager.popMatrix();
//        }

    }

    @Override
    public boolean isGlobalRenderer(ElevatorTile te) {
        return true;
    }
}
