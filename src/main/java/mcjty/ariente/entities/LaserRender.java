package mcjty.ariente.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.ariente.Ariente;
import mcjty.lib.client.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class LaserRender extends EntityRenderer<LaserEntity> {

    private Random random = new Random();
    private static ResourceLocation laserbeams[] = new ResourceLocation[4];
    static {
        laserbeams[0] = new ResourceLocation(Ariente.MODID, "textures/effects/negarite_laserbeam1.png");
        laserbeams[1] = new ResourceLocation(Ariente.MODID, "textures/effects/negarite_laserbeam2.png");
        laserbeams[2] = new ResourceLocation(Ariente.MODID, "textures/effects/negarite_laserbeam3.png");
        laserbeams[3] = new ResourceLocation(Ariente.MODID, "textures/effects/negarite_laserbeam4.png");
    }

    public LaserRender(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(LaserEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLightIn) {
        GlStateManager._depthMask(false);
        GlStateManager._enableBlend();
        GlStateManager._blendFunc(GL11.GL_ONE, GL11.GL_ONE);

        Minecraft mc = Minecraft.getInstance();
        PlayerEntity p = mc.player;
        double doubleX = p.xOld + (p.getX() - p.xOld) * partialTicks;
        double doubleY = p.yOld + (p.getY() - p.yOld) * partialTicks;
        double doubleZ = p.zOld + (p.getZ() - p.zOld) * partialTicks;

//        Vector3f start = new Vector3f((float) x, (float) y, (float) z);

        GlStateManager._pushMatrix();
        // @todo 1.15
//        GlStateManager.translatef((float)x, (float)y, (float)z);
        GlStateManager._rotatef(180.0F - entity.getSpawnYaw(), 0.0F, 1.0F, 0.0F);
        GlStateManager._rotatef(180.0F - entity.getSpawnPitch(), 1.0F, 0.0F, 0.0F);

//        GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
//        GlStateManager.rotate((this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

//        GlStateManager.rotate(180.0F - entityYaw, 0.0F, 1.0F, 0.0F);
//        GlStateManager.translate(-doubleX - x, -doubleY - y, -doubleZ - z);

        Tessellator tessellator = Tessellator.getInstance();
        // @todo 1.15
//        BufferBuilder buffer = tessellator.getBuffer();

        Vector3d lv = entity.getLookAngle();

        // ----------------------------------------

        // @todo 1.15
//        this.bindTexture(laserbeams[random.nextInt(4)]);
//        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LIGHTMAP_COLOR);

        float size = .05f;
        int length = 2;
        drawQuad(tessellator,
                new Vector3f(-size, -size, 0),
                new Vector3f(-size, -size, length),
                new Vector3f(-size, size, length),
                new Vector3f(-size, size, 0)
        );
        drawQuad(tessellator,
                new Vector3f(size, size, 0),
                new Vector3f(size, size, length),
                new Vector3f(size, -size, length),
                new Vector3f(size, -size, 0)
        );
        drawQuad(tessellator,
                new Vector3f(-size, size, 0),
                new Vector3f(-size, size, length),
                new Vector3f(size, size, length),
                new Vector3f(size, size, 0)
        );
        drawQuad(tessellator,
                new Vector3f(size, -size, 0),
                new Vector3f(size, -size, length),
                new Vector3f(-size, -size, length),
                new Vector3f(-size, -size, 0)
        );
        drawQuad(tessellator,
                new Vector3f(size, -size, 0),
                new Vector3f(-size, -size, 0),
                new Vector3f(-size, size, 0),
                new Vector3f(size, size, 0)
        );
        drawQuad(tessellator,
                new Vector3f(-size, -size, length),
                new Vector3f(size, -size, length),
                new Vector3f(size, size, length),
                new Vector3f(-size, size, length)
        );

//        RenderHelper.drawBeam(
//                start,
//                end,
//                player, .1f);

        tessellator.end();

        GlStateManager._popMatrix();

        GlStateManager._enableBlend();
        GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager._depthMask(true);
    }

    private static void drawQuad(Tessellator tessellator, Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4) {
        int brightness = 240;
        int b1 = brightness >> 16 & 65535;
        int b2 = brightness & 65535;

        BufferBuilder buffer = tessellator.getBuilder();
        buffer.vertex(p1.x(), p1.y(), p1.z()).uv(0.3f, 0.3f).uv2(b1, b2).color(255, 255, 255, 128).endVertex();
        buffer.vertex(p2.x(), p2.y(), p2.z()).uv(0.7f, 0.3f).uv2(b1, b2).color(255, 255, 255, 128).endVertex();
        buffer.vertex(p3.x(), p3.y(), p3.z()).uv(0.7f, 0.7f).uv2(b1, b2).color(255, 255, 255, 128).endVertex();
        buffer.vertex(p4.x(), p4.y(), p4.z()).uv(0.3f, 0.7f).uv2(b1, b2).color(255, 255, 255, 128).endVertex();
    }


    @Override
    public ResourceLocation getTextureLocation(LaserEntity entity) {
        return AtlasTexture.LOCATION_BLOCKS;
    }

    public static class Factory implements IRenderFactory<LaserEntity> {

        @Override
        public EntityRenderer<? super LaserEntity> createRenderFor(EntityRendererManager manager) {
            return new LaserRender(manager);
        }

    }
}
