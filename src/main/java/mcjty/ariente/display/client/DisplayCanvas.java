package mcjty.ariente.display.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mcjty.ariente.client.ArienteRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

/** Drawing in display pixels (64 per block), with the origin at the top left. */
public record DisplayCanvas(PoseStack poses, MultiBufferSource buffer) {

    public void fill(float x, float y, float width, float height, int argb) {
        if ((argb >>> 24) == 0 || width <= 0 || height <= 0) {
            return;
        }
        VertexConsumer vertices = buffer.getBuffer(DisplayRenderType.SURFACE);
        Matrix4f pose = poses.last().pose();
        int r = argb >> 16 & 255;
        int g = argb >> 8 & 255;
        int b = argb & 255;
        int a = argb >>> 24;
        vertices.addVertex(pose, x, y + height, 0).setColor(r, g, b, a);
        vertices.addVertex(pose, x + width, y + height, 0).setColor(r, g, b, a);
        vertices.addVertex(pose, x + width, y, 0).setColor(r, g, b, a);
        vertices.addVertex(pose, x, y, 0).setColor(r, g, b, a);
    }

    public void border(float x, float y, float width, float height, int color) {
        fill(x, y, width, .6f, color);
        fill(x, y + height - .6f, width, .6f, color);
        fill(x, y, .6f, height, color);
        fill(x + width - .6f, y, .6f, height, color);
    }

    public void text(String text, float x, float y, int rgb, float scale, int maxWidth) {
        poses.pushPose();
        poses.translate(x, y, .06);
        poses.scale(scale, scale, scale);
        Font font = Minecraft.getInstance().font;
        String fitted = font.plainSubstrByWidth(text, Math.max(1, (int) (maxWidth / scale)));
        font.drawInBatch(fitted, 0, 0, rgb, false, poses.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        poses.popPose();
    }

    public void alienGlyph(int glyph, float x, float y, float size, int rgb) {
        VertexConsumer vertices = buffer.getBuffer(DisplayRenderType.ALIEN_FONT);
        Matrix4f pose = poses.last().pose();
        float u = (glyph & 3) / 4f;
        float v = (glyph >> 2 & 3) / 4f;
        int r = rgb >> 16 & 255;
        int g = rgb >> 8 & 255;
        int b = rgb & 255;
        vertices.addVertex(pose, x, y + size, .06f).setUv(u, v + .25f).setColor(r, g, b, 255);
        vertices.addVertex(pose, x + size, y + size, .06f).setUv(u + .25f, v + .25f).setColor(r, g, b, 255);
        vertices.addVertex(pose, x + size, y, .06f).setUv(u + .25f, v).setColor(r, g, b, 255);
        vertices.addVertex(pose, x, y, .06f).setUv(u, v).setColor(r, g, b, 255);
    }

    public void item(ItemStack stack, float x, float y) {
        poses.pushPose();
        poses.translate(x + 8, y + 8, .18);
        poses.scale(16, -16, .01f);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.GUI,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poses,
                type -> buffer.getBuffer(ArienteRenderType.displayItem(type)), Minecraft.getInstance().level, 0);
        poses.popPose();
    }
}
