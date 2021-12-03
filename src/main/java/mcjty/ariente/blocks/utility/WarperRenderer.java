package mcjty.ariente.blocks.utility;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.ariente.Ariente;
import mcjty.lib.client.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class WarperRenderer extends TileEntityRenderer<WarperTile> {

    private ResourceLocation halo = new ResourceLocation(Ariente.MODID, "textures/blocks/machines/elevator_beam.png");
    private Random random = new Random();

    public WarperRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    private static float randomX[] = new float[]{.2f, .3f, .2f, .7f, .8f, .5f, .2f, .8f, .4f, .6f};
    private static float randomZ[] = new float[]{.3f, .2f, .8f, .3f, .7f, .6f, .4f, .5f, .2f, .3f};
    private static int randomY[] = new int[]{0, 3, 2, 1, 6, 5, 6, 8, 2, 3};

    @Override
    public void render(WarperTile te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
//        if (te.isWorking()) {
        Tessellator tessellator = Tessellator.getInstance();
        GlStateManager._pushMatrix();
//            GlStateManager.translate(x, y, z);

        GlStateManager._enableBlend();
        GlStateManager._depthMask(false);
//        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
//        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
        GlStateManager._blendFunc(GL11.GL_ONE, GL11.GL_ONE);
//        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
        GlStateManager._disableCull();
        GlStateManager._enableDepthTest();

        ResourceLocation beamIcon = halo;
        // @todo 1.15
//        bindTexture(beamIcon);

        Minecraft mc = Minecraft.getInstance();
        PlayerEntity p = mc.player;
        double doubleX = p.xOld + (p.getX() - p.xOld) * partialTicks;
        double doubleY = p.yOld + (p.getY() - p.yOld) * partialTicks;
        double doubleZ = p.zOld + (p.getZ() - p.zOld) * partialTicks;

        GlStateManager._translated(-doubleX, -doubleY, -doubleZ);

        Vector3f player = new Vector3f((float) doubleX, (float) doubleY + p.getEyeHeight(), (float) doubleZ);

        long tt = System.currentTimeMillis() / 100;

        GlStateManager._color4f(1, 1, 1, 1);

        BufferBuilder renderer = tessellator.getBuilder();
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);

        float height = 2;

        for (int i = 0; i < 10; i++) {
            int ii = i % 10;
            long ticks = (tt + randomY[ii]) % 80;
            if (ticks > 40) {
                ticks = 80 - ticks;
            }
            float i1 = ticks / 40.0f;
            float xx = te.getBlockPos().getX() + randomX[ii];
            float zz = te.getBlockPos().getZ() + randomZ[ii];
            float yy = te.getBlockPos().getY() - 1.0f + i1 + (randomY[ii] * height) / 8.0f;
            RenderHelper.drawBeam(new Vector3f(xx, yy, zz), new Vector3f(xx, yy + 4, zz), player, 0.2f);
        }

//        net.minecraft.util.math.vector.Vector3d cameraPos = net.minecraft.client.renderer.ActiveRenderInfo.getCameraPosition();
//        tessellator.getBuffer().sortVertexData((float) (player.x + doubleX), (float) (player.y + doubleY), (float) (player.z + doubleZ));
//        tessellator.getBuffer().sortVertexData((float)(cameraPos.x+doubleX), (float)(cameraPos.y+doubleY), (float)(cameraPos.z+doubleZ));
        tessellator.end();

        GlStateManager._depthMask(true);
        GlStateManager._enableLighting();
        GlStateManager._enableDepthTest();
        GlStateManager._alphaFunc(GL11.GL_GREATER, 0.1F);

        GlStateManager._popMatrix();
//        }

    }

    @Override
    public boolean shouldRenderOffScreen(WarperTile te) {
        return true;
    }

    public static void register() {
        // @todo 1.15
//        ClientRegistry.bindTileEntitySpecialRenderer(WarperTile.class, new WarperRenderer());
    }
}
