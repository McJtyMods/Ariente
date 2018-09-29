package mcjty.ariente.apiimp.hologui;

import mcjty.ariente.Ariente;
import mcjty.ariente.config.GuiConfiguration;
import mcjty.ariente.api.hologui.IGuiComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public class HoloGuiEntityRender extends Render<HoloGuiEntity> {

    private static final ResourceLocation guiBackground1 = new ResourceLocation(Ariente.MODID, "textures/gui/hologui_blue_softwhite.png");
    private static final ResourceLocation guiBackground2 = new ResourceLocation(Ariente.MODID, "textures/gui/hologui_blue.png");
    private static final ResourceLocation guiBackground3 = new ResourceLocation(Ariente.MODID, "textures/gui/hologui_blue_sharpwhite.png");
    private static final ResourceLocation guiBackground4 = new ResourceLocation(Ariente.MODID, "textures/gui/hologui_blue_sharpblack.png");
    private static final ResourceLocation guiBackground5 = new ResourceLocation(Ariente.MODID, "textures/gui/hologui_blue_softblack.png");
    private static final ResourceLocation guiBackground6 = new ResourceLocation(Ariente.MODID, "textures/gui/hologui_gray_sharpblack.png");
    private static final ResourceLocation guiBackground7 = new ResourceLocation(Ariente.MODID, "textures/gui/hologui_gray_sharpwhite.png");
    private static final ResourceLocation guiBackground8 = new ResourceLocation(Ariente.MODID, "textures/gui/hologui_gray_softblack.png");

    public HoloGuiEntityRender(RenderManager renderManager) {
        super(renderManager);
    }

    // Option 1: blue with sharp white: hologui2
    // Option 2: just blue: hologui1
    // Option 3: blue with soft dark border: hologui4
    // Option 4: gray with soft dark: hologui7
    // Option 5: gray with sharp white: hologui6
    // Option 6: blue with sharp dark border: hologui3
    // Option 7: gray with sharp dark border: hologui5
    // Option 8: current: hologui

    @Override
    public void doRender(HoloGuiEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (entity.isRiding()) {
            return;
        }

        doActualRender(entity, x, y, z, entityYaw);
    }

    public static void doActualRender(HoloGuiEntity entity, double x, double y, double z, float entityYaw) {
        Tessellator t = Tessellator.getInstance();
        BufferBuilder builder = t.getBuffer();

        Minecraft.getMinecraft().entityRenderer.disableLightmap();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

//        renderDebugOutline(entity, t, builder);

        GlStateManager.rotate(180.0F - entityYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0, .5, 0);
        double scale = entity.isSmall() ? .6 : 1;
        GlStateManager.scale(scale, scale, scale);

        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();

        int style = GuiConfiguration.GUI_STYLE;

        if (style <= 8) {
            GlStateManager.enableBlend();
        } else {
            GlStateManager.disableBlend();
            style -= 8;
        }

        GlStateManager.color(1.0f, 1.0f, 1.0f, 0.5f);

        Minecraft mc = Minecraft.getMinecraft();

        switch (style) {
            case 1: mc.renderEngine.bindTexture(guiBackground1); break;
            case 2: mc.renderEngine.bindTexture(guiBackground2); break;
            case 3: mc.renderEngine.bindTexture(guiBackground3); break;
            case 4: mc.renderEngine.bindTexture(guiBackground4); break;
            case 5: mc.renderEngine.bindTexture(guiBackground5); break;
            case 6: mc.renderEngine.bindTexture(guiBackground6); break;
            case 7: mc.renderEngine.bindTexture(guiBackground7); break;
            case 8: mc.renderEngine.bindTexture(guiBackground8); break;
        }

        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        double min = -.5;
        double max = .5;
        renderQuad(builder, min, max, min, max);

        t.draw();

        GlStateManager.disableDepth();

        double cursorX = entity.getCursorX();
        double cursorY = entity.getCursorY();

        IGuiComponent gui = entity.getGui(Minecraft.getMinecraft().player);
        if (gui != null) {
            gui.render(Minecraft.getMinecraft().player, entity, cursorX, cursorY);
            IGuiComponent hovering = gui.findHoveringWidget(cursorX, cursorY);
            if (hovering != entity.tooltipComponent) {
                entity.tooltipComponent = hovering;
                entity.tooltipTimeout = 10;
            } else {
                if (entity.tooltipTimeout > 0) {
                    entity.tooltipTimeout--;
                } else {
                    if (hovering != null) {
                        hovering.renderTooltip(Minecraft.getMinecraft().player, entity, cursorX, cursorY);
                    }
                }
            }
        }

        if (cursorX >= 0 && cursorX <= 10 && cursorY >= 0 && cursorY <= 10) {
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            double offset = .01;
            renderQuadColor(builder, (cursorX / 10.0) - .42 - offset, (cursorX / 10.0) - .42 + offset,
                     - ((cursorY / 10) -.42 - offset),  - ((cursorY / 10) -.42 + offset),
                    60, 255, 128, 100);
            t.draw();
        }
        GlStateManager.popMatrix();


        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        Minecraft.getMinecraft().entityRenderer.enableLightmap();
    }

    private static void renderQuad(BufferBuilder builder, double minX, double maxX, double minY, double maxY) {
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
