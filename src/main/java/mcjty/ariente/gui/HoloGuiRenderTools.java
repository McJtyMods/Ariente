package mcjty.ariente.gui;

import mcjty.lib.client.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.List;

public class HoloGuiRenderTools {

    public static void renderText(double x, double y, String text, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.01, 0.01, 0.01);
        GlStateManager.rotate(180, 0, 1, 0);
        GlStateManager.rotate(180, 0, 0, 1);
        Minecraft.getMinecraft().fontRenderer.drawString(text, (int) (x * 10 - 40), (int) (y * 10 - 40), color);
        GlStateManager.popMatrix();
    }

    public static void renderImage(double x, double y, int u, int v, int w, int h, int txtw, int txth, ResourceLocation image) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.01, 0.01, 0.01);
        GlStateManager.rotate(180, 0, 1, 0);
        GlStateManager.rotate(180, 0, 0, 1);
        GlStateManager.scale(1, 1, 0.01);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        RenderHelper.drawTexturedModalRect((int) (x * 10 - 46), (int) (y * 10 - 44), u, v, w, h, txtw, txth);
        GlStateManager.popMatrix();
    }

    public static void renderBox(double x, double y, double w, double h, int color) {
        GlStateManager.pushMatrix();

        GlStateManager.scale(0.10, 0.10, 0.10);
        GlStateManager.rotate(180, 0, 1, 0);

        RenderHelper.drawFlatBox(3 - (int) x, 3 - (int) y, (int) (3 - x + w), (int) (3 - y + h), color, color);
//        RenderHelper.drawHorizontalLine((int) x, (int) y, (int) (x + w), color);
//        RenderHelper.drawHorizontalLine((int) x, (int) (y + h), (int) (x + w), color);
//        RenderHelper.drawVerticalLine((int) x, (int) y, (int) (y + h), color);
//        RenderHelper.drawVerticalLine((int) (x + w), (int) y, (int) (y + h), color);

        GlStateManager.popMatrix();
    }

    public static void renderItem(double x, double y, ItemStack stack, @Nullable ResourceLocation lightmap, boolean border) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.1, 0.1, 0.1);
        GlStateManager.translate(x * 0.95 - 3.7, 4.2 - y * 1.2, 0);
        GlStateManager.scale(1, 1, 0.1);
        if (!stack.isEmpty()) {
            RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
            IBakedModel ibakedmodel = renderItem.getItemModelWithOverrides(stack, null, null);
            renderItemModel(stack, ibakedmodel, ItemCameraTransforms.TransformType.GUI, lightmap);
            if (border) {
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder builder = tessellator.getBuffer();
                builder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
                x /= 200; x -= 0.47;
                y /= 100; y -= 0.5;

                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.glLineWidth(2.0F);
                GlStateManager.disableTexture2D();

                double z = 0.3;
                double w = 0.9;
                builder.pos(x, y, z).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
                builder.pos(x+w, y, z).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
                builder.pos(x+w, y+w, z).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
                builder.pos(x, y+w, z).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
                builder.pos(x, y, z).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
                tessellator.draw();

                GlStateManager.disableBlend();
                GlStateManager.enableTexture2D();
            }
        }
        GlStateManager.popMatrix();
    }

    private static void renderItemModel(ItemStack stack, IBakedModel bakedmodel, ItemCameraTransforms.TransformType transform, @Nullable ResourceLocation lightmap) {
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        renderItem.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        renderItem.textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        if (lightmap != null) {
            Minecraft.getMinecraft().entityRenderer.enableLightmap();
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
//            Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Ariente.MODID, "textures/gui/darken.png"));
            Minecraft.getMinecraft().getTextureManager().bindTexture(lightmap);
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        }

        GlStateManager.pushMatrix();
        // TODO: check if negative scale is a thing
        bakedmodel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(bakedmodel, transform, false);

        renderItem.renderItem(stack, bakedmodel);
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        renderItem.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        renderItem.textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();

        if (lightmap != null) {
            Minecraft.getMinecraft().entityRenderer.disableLightmap();
        }
    }


    public static void renderToolTip(ItemStack stack, int x, int y) {
        GuiUtils.preItemToolTip(stack);

        net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();

        Minecraft mc = Minecraft.getMinecraft();
        ITooltipFlag flag = mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL;
        List<String> list = stack.getTooltip(mc.player, flag);

        for (int i = 0; i < list.size(); ++i) {
            if (i == 0) {
                list.set(i, stack.getRarity().rarityColor + list.get(i));
            } else {
                list.set(i, TextFormatting.GRAY + list.get(i));
            }
        }

        FontRenderer font = stack.getItem().getFontRenderer(stack);
        FontRenderer font1 = (font == null ? mc.fontRenderer : font);
        GuiUtils.drawHoveringText(list, x, y, 600, 500, -1, font1);

        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();

        GuiUtils.postItemToolTip();
    }

}
