package mcjty.ariente.entities;

import mcjty.ariente.Ariente;
import mcjty.ariente.gui.IGuiTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public class HoloGuiEntityRender extends Render<HoloGuiEntity> {

    private static final ResourceLocation guiBackground = new ResourceLocation(Ariente.MODID, "textures/gui/hologui.png");

    public HoloGuiEntityRender(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(HoloGuiEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        Tessellator t = Tessellator.getInstance();
        BufferBuilder builder = t.getBuffer();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        renderDebugOutline(entity, t, builder);

        GlStateManager.rotate(180.0F - entityYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0, .5, 0);

        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 0.5f);

        Minecraft mc = Minecraft.getMinecraft();

        mc.renderEngine.bindTexture(guiBackground);

        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        double min = -.5;
        double max = .5;
        renderQuad(builder, min, max, min, max);

        t.draw();

        GlStateManager.disableDepth();

        BlockPos pos = entity.getGuiTile();
        if (pos != null) {
            TileEntity te = entity.getEntityWorld().getTileEntity(pos);
            if (te instanceof IGuiTile) {
                IGuiTile guiTile = (IGuiTile) te;
                guiTile.renderGui(entity);
            }
        }

        double cursorX = entity.getCursorX();
        double cursorY = entity.getCursorY();
//        System.out.println("cursor = " + cursorX + "," + cursorY);
        if (cursorX >= 0 && cursorX <= 30 && cursorY >= 0 && cursorY <= 30) {
            GlStateManager.disableTexture2D();
            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            double offset = .02;
            renderQuadColor(builder, (cursorX / 10.0) - .4 - offset, (cursorX / 10.0) - .4 + offset,
                     - ((cursorY / 9.0) -.5 - offset),  - ((cursorY / 9.0) -.5 + offset),
                    0, 128, 255, 128);
            t.draw();
        }
        GlStateManager.popMatrix();


        Vec3d hit = entity.getHit();
        if (hit != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.disableTexture2D();
            builder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            double o = .02;
            double minX = hit.x - entity.posX - o;
            double minY = hit.y - entity.posY - o;
            double minZ = hit.z - entity.posZ - o;
            double maxX = hit.x - entity.posX + o;
            double maxY = hit.y - entity.posY + o;
            double maxZ = hit.z - entity.posZ + o;
            renderDebugOutline(builder, minX, minY, minZ, maxX, maxY, maxZ);
            t.draw();
            GlStateManager.popMatrix();
        }

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
    }

    private void renderQuad(BufferBuilder builder, double minX, double maxX, double minY, double maxY) {
        builder.pos(minX, minY, 0).tex(0, 0).endVertex(); //1
        builder.pos(maxX, minY, 0).tex(1, 0).endVertex();
        builder.pos(maxX, maxY, 0).tex(1, 1).endVertex();
        builder.pos(minX, maxY, 0).tex(0, 1).endVertex();
        builder.pos(minX, maxY, 0).tex(0, 1).endVertex(); //2
        builder.pos(maxX, maxY, 0).tex(1, 1).endVertex();
        builder.pos(maxX, minY, 0).tex(1, 0).endVertex();
        builder.pos(minX, minY, 0).tex(0, 0).endVertex();
    }

    private static void renderQuadColor(BufferBuilder builder, double minX, double maxX, double minY, double maxY, int r, int g, int b, int a) {
        builder.pos(minX, minY, 0).color(r, g, b, a).endVertex(); //1
        builder.pos(maxX, minY, 0).color(r, g, b, a).endVertex();
        builder.pos(maxX, maxY, 0).color(r, g, b, a).endVertex();
        builder.pos(minX, maxY, 0).color(r, g, b, a).endVertex();
        builder.pos(minX, maxY, 0).color(r, g, b, a).endVertex(); //2
        builder.pos(maxX, maxY, 0).color(r, g, b, a).endVertex();
        builder.pos(maxX, minY, 0).color(r, g, b, a).endVertex();
        builder.pos(minX, minY, 0).color(r, g, b, a).endVertex();
    }

    private void renderDebugOutline(HoloGuiEntity entity, Tessellator t, BufferBuilder builder) {
        AxisAlignedBB box = entity.getEntityBoundingBox();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        builder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        double minX = box.minX - entity.posX;
        double minY = box.minY - entity.posY;
        double minZ = box.minZ - entity.posZ;
        double maxX = box.maxX - entity.posX;
        double maxY = box.maxY - entity.posY;
        double maxZ = box.maxZ - entity.posZ;

        renderDebugOutline(builder, minX, minY, minZ, maxX, maxY, maxZ);
        t.draw();
    }

    private void renderDebugOutline(BufferBuilder builder, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        builder.pos(minX, minY, minZ).color(255, 255, 255, 128).endVertex();
        builder.pos(maxX, minY, minZ).color(255, 255, 255, 128).endVertex();

        builder.pos(minX, minY, minZ).color(255, 255, 255, 128).endVertex();
        builder.pos(minX, maxY, minZ).color(255, 255, 255, 128).endVertex();

        builder.pos(minX, minY, minZ).color(255, 255, 255, 128).endVertex();
        builder.pos(minX, minY, maxZ).color(255, 255, 255, 128).endVertex();

        builder.pos(maxX, maxY, maxZ).color(255, 0, 0, 128).endVertex();
        builder.pos(minX, maxY, maxZ).color(255, 0, 0, 128).endVertex();

        builder.pos(maxX, maxY, maxZ).color(255, 0, 0, 128).endVertex();
        builder.pos(maxX, minY, maxZ).color(255, 0, 0, 128).endVertex();

        builder.pos(maxX, maxY, maxZ).color(255, 0, 0, 128).endVertex();
        builder.pos(maxX, maxY, minZ).color(255, 0, 0, 128).endVertex();
    }

    protected float interpolateRotation(float prevYawOffset, float yawOffset, float partialTicks) {
        float f;

        for (f = yawOffset - prevYawOffset; f < -180.0F; f += 360.0F) {
            ;
        }

        while (f >= 180.0F) {
            f -= 360.0F;
        }

        return prevYawOffset + partialTicks * f;
    }


    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(HoloGuiEntity entity) {
        return null;
    }


    public static class Factory implements IRenderFactory<HoloGuiEntity> {

        @Override
        public Render<? super HoloGuiEntity> createRenderFor(RenderManager manager) {
            return new HoloGuiEntityRender(manager);
        }

    }
}
