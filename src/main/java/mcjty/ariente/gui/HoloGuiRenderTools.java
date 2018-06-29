package mcjty.ariente.gui;

import mcjty.lib.client.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;

public class HoloGuiRenderTools {

    public static void renderText(double x, double y, String text, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.01, 0.01, 0.01);
        GlStateManager.rotate(180, 0, 1, 0);
        GlStateManager.rotate(180, 0, 0, 1);
        Minecraft.getMinecraft().fontRenderer.drawString(text, (int)(x*10-40), (int)(y*10-40), color);
        GlStateManager.popMatrix();
    }

    public static void renderBox(double x, double y, double w, double h, int color) {
        GlStateManager.pushMatrix();

        GlStateManager.scale(0.10, 0.10, 0.10);
        GlStateManager.rotate(180, 0, 1, 0);

        RenderHelper.drawFlatBox(3 - (int) x, 3 - (int) y,  (int) (3 - x + w), (int) (3 - y + h), color, color);
//        RenderHelper.drawHorizontalLine((int) x, (int) y, (int) (x + w), color);
//        RenderHelper.drawHorizontalLine((int) x, (int) (y + h), (int) (x + w), color);
//        RenderHelper.drawVerticalLine((int) x, (int) y, (int) (y + h), color);
//        RenderHelper.drawVerticalLine((int) (x + w), (int) y, (int) (y + h), color);

        GlStateManager.popMatrix();
    }

    public static void renderItem(double x, double y, ItemStack stack) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.1, 0.1, 0.1);
        GlStateManager.translate(x*1.3-3.3, 4.0 - y*1.3, 0);
        GlStateManager.scale(1, 1, 0.1);
        Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GUI);
        GlStateManager.popMatrix();
    }
}
