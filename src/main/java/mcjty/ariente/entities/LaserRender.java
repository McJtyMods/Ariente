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
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ONE);

        Minecraft mc = Minecraft.getInstance();
        PlayerEntity p = mc.player;
        double doubleX = p.lastTickPosX + (p.getPosX() - p.lastTickPosX) * partialTicks;
        double doubleY = p.lastTickPosY + (p.getPosY() - p.lastTickPosY) * partialTicks;
        double doubleZ = p.lastTickPosZ + (p.getPosZ() - p.lastTickPosZ) * partialTicks;

//        RenderHelper.Vector start = new RenderHelper.Vector((float) x, (float) y, (float) z);

        GlStateManager.pushMatrix();
        // @todo 1.15
//        GlStateManager.translatef((float)x, (float)y, (float)z);
        GlStateManager.rotatef(180.0F - entity.getSpawnYaw(), 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(180.0F - entity.getSpawnPitch(), 1.0F, 0.0F, 0.0F);

//        GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
//        GlStateManager.rotate((this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

//        GlStateManager.rotate(180.0F - entityYaw, 0.0F, 1.0F, 0.0F);
//        GlStateManager.translate(-doubleX - x, -doubleY - y, -doubleZ - z);

        Tessellator tessellator = Tessellator.getInstance();
        // @todo 1.15
//        BufferBuilder buffer = tessellator.getBuffer();

        Vector3d lv = entity.getLookVec();

        // ----------------------------------------

        // @todo 1.15
//        this.bindTexture(laserbeams[random.nextInt(4)]);
//        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LIGHTMAP_COLOR);

        float size = .05f;
        int length = 2;
        drawQuad(tessellator,
                new RenderHelper.Vector(-size, -size, 0),
                new RenderHelper.Vector(-size, -size, length),
                new RenderHelper.Vector(-size, size, length),
                new RenderHelper.Vector(-size, size, 0)
        );
        drawQuad(tessellator,
                new RenderHelper.Vector(size, size, 0),
                new RenderHelper.Vector(size, size, length),
                new RenderHelper.Vector(size, -size, length),
                new RenderHelper.Vector(size, -size, 0)
        );
        drawQuad(tessellator,
                new RenderHelper.Vector(-size, size, 0),
                new RenderHelper.Vector(-size, size, length),
                new RenderHelper.Vector(size, size, length),
                new RenderHelper.Vector(size, size, 0)
        );
        drawQuad(tessellator,
                new RenderHelper.Vector(size, -size, 0),
                new RenderHelper.Vector(size, -size, length),
                new RenderHelper.Vector(-size, -size, length),
                new RenderHelper.Vector(-size, -size, 0)
        );
        drawQuad(tessellator,
                new RenderHelper.Vector(size, -size, 0),
                new RenderHelper.Vector(-size, -size, 0),
                new RenderHelper.Vector(-size, size, 0),
                new RenderHelper.Vector(size, size, 0)
        );
        drawQuad(tessellator,
                new RenderHelper.Vector(-size, -size, length),
                new RenderHelper.Vector(size, -size, length),
                new RenderHelper.Vector(size, size, length),
                new RenderHelper.Vector(-size, size, length)
        );

//        RenderHelper.drawBeam(
//                start,
//                end,
//                player, .1f);

        tessellator.draw();

        GlStateManager.popMatrix();

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(true);
    }

    private static void drawQuad(Tessellator tessellator, RenderHelper.Vector p1, RenderHelper.Vector p2, RenderHelper.Vector p3, RenderHelper.Vector p4) {
        int brightness = 240;
        int b1 = brightness >> 16 & 65535;
        int b2 = brightness & 65535;

        BufferBuilder buffer = tessellator.getBuffer();
        buffer.pos(p1.getX(), p1.getY(), p1.getZ()).tex(0.3f, 0.3f).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
        buffer.pos(p2.getX(), p2.getY(), p2.getZ()).tex(0.7f, 0.3f).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
        buffer.pos(p3.getX(), p3.getY(), p3.getZ()).tex(0.7f, 0.7f).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
        buffer.pos(p4.getX(), p4.getY(), p4.getZ()).tex(0.3f, 0.7f).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
    }


    @Override
    public ResourceLocation getEntityTexture(LaserEntity entity) {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }

    public static class Factory implements IRenderFactory<LaserEntity> {

        @Override
        public EntityRenderer<? super LaserEntity> createRenderFor(EntityRendererManager manager) {
            return new LaserRender(manager);
        }

    }
}
