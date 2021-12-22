package mcjty.ariente.entities;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import mcjty.ariente.Ariente;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
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

    public LaserRender(Context renderManager) {
        super(renderManager);
    }

    @Override
    public void render(LaserEntity entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLightIn) {
        GlStateManager._depthMask(false);
        GlStateManager._enableBlend();
        GlStateManager._blendFunc(GL11.GL_ONE, GL11.GL_ONE);

        Minecraft mc = Minecraft.getInstance();
        Player p = mc.player;
        double doubleX = p.xOld + (p.getX() - p.xOld) * partialTicks;
        double doubleY = p.yOld + (p.getY() - p.yOld) * partialTicks;
        double doubleZ = p.zOld + (p.getZ() - p.zOld) * partialTicks;

//        Vector3f start = new Vector3f((float) x, (float) y, (float) z);

        matrixStack.pushPose();
        // @todo 1.15
//        GlStateManager.translatef((float)x, (float)y, (float)z);
        matrixStack.mulPose(new Quaternion(180.0F - entity.getSpawnYaw(), 0.0F, 1.0F, 0.0F));
        matrixStack.mulPose(new Quaternion(180.0F - entity.getSpawnPitch(), 1.0F, 0.0F, 0.0F));

//        GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
//        GlStateManager.rotate((this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

//        GlStateManager.rotate(180.0F - entityYaw, 0.0F, 1.0F, 0.0F);
//        GlStateManager.translate(-doubleX - x, -doubleY - y, -doubleZ - z);

        Tesselator tessellator = Tesselator.getInstance();
        // @todo 1.15
//        BufferBuilder buffer = tessellator.getBuffer();

        Vec3 lv = entity.getLookAngle();

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

        matrixStack.popPose();

        GlStateManager._enableBlend();
        GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager._depthMask(true);
    }

    private static void drawQuad(Tesselator tessellator, Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4) {
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
        return InventoryMenu.BLOCK_ATLAS;
    }

    public static class Factory implements EntityRendererProvider<LaserEntity> {

        @Override
        public EntityRenderer<LaserEntity> create(Context manager) {
            return new LaserRender(manager);
        }

    }
}
