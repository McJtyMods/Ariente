package mcjty.ariente.entities;

import mcjty.ariente.Ariente;
import mcjty.lib.client.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class LaserRender extends Render<LaserEntity> {

    private Random random = new Random();
    private static ResourceLocation laserbeams[] = new ResourceLocation[4];
    static {
        laserbeams[0] = new ResourceLocation(Ariente.MODID, "textures/effects/negarite_laserbeam1.png");
        laserbeams[1] = new ResourceLocation(Ariente.MODID, "textures/effects/negarite_laserbeam2.png");
        laserbeams[2] = new ResourceLocation(Ariente.MODID, "textures/effects/negarite_laserbeam3.png");
        laserbeams[3] = new ResourceLocation(Ariente.MODID, "textures/effects/negarite_laserbeam4.png");
    }

    public LaserRender(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(LaserEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ONE);

        Minecraft mc = Minecraft.getInstance();
        EntityPlayerSP p = mc.player;
        double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * partialTicks;
        double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * partialTicks;
        double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * partialTicks;

//        RenderHelper.Vector start = new RenderHelper.Vector((float) x, (float) y, (float) z);

        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y, (float)z);
        GlStateManager.rotate(180.0F - entity.getSpawnYaw(), 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180.0F - entity.getSpawnPitch(), 1.0F, 0.0F, 0.0F);

//        GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
//        GlStateManager.rotate((this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

//        GlStateManager.rotate(180.0F - entityYaw, 0.0F, 1.0F, 0.0F);
//        GlStateManager.translate(-doubleX - x, -doubleY - y, -doubleZ - z);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        Vec3d lv = entity.getLookVec();

        // ----------------------------------------

        this.bindTexture(laserbeams[random.nextInt(4)]);

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

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
        buffer.pos(p1.getX(), p1.getY(), p1.getZ()).tex(0.3D, 0.3D).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
        buffer.pos(p2.getX(), p2.getY(), p2.getZ()).tex(0.7D, 0.3D).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
        buffer.pos(p3.getX(), p3.getY(), p3.getZ()).tex(0.7D, 0.7D).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
        buffer.pos(p4.getX(), p4.getY(), p4.getZ()).tex(0.3D, 0.7D).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
    }


    @Override
    protected ResourceLocation getEntityTexture(LaserEntity entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }

    public static class Factory implements IRenderFactory<LaserEntity> {

        @Override
        public Render<? super LaserEntity> createRenderFor(RenderManager manager) {
            return new LaserRender(manager);
        }

    }
}
