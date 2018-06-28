package mcjty.ariente.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;

public class HoloGuiRenderTools {

    public static void renderText(int x, int y, String text, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.01, 0.01, 0.01);
        GlStateManager.rotate(180, 0, 1, 0);
        GlStateManager.rotate(180, 0, 0, 1);
        Minecraft.getMinecraft().fontRenderer.drawString(text, x*10-40, y*10-40, color);
        GlStateManager.popMatrix();

    }

    public static void renderItem(int x, int y, ItemStack stack) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.1, 0.1, 0.1);
        GlStateManager.translate(x*1.3-3.3, 4.0 - y*1.3, 0);
        GlStateManager.scale(1, 1, 0.1);
        Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GUI);
        GlStateManager.popMatrix();
    }
}
