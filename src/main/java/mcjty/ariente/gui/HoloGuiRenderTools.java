package mcjty.ariente.gui;

import mcjty.lib.client.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

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
        GlStateManager.scale(1, 1, 0.05);
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

    public static void renderItem(double x, double y, ItemStack stack, @Nullable ResourceLocation lightmap) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.1, 0.1, 0.1);
        GlStateManager.translate(x * 0.95 - 3.7, 4.2 - y * 1.2, 0);
        GlStateManager.scale(1, 1, 0.1);
        if (!stack.isEmpty()) {
            RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
            IBakedModel ibakedmodel = renderItem.getItemModelWithOverrides(stack, null, null);
            renderItemModel(stack, ibakedmodel, ItemCameraTransforms.TransformType.GUI, lightmap);
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

}
