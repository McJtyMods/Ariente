package mcjty.ariente.blocks.generators;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public class TankRendererTools {

    private static void p(IVertexBuilder renderer, MatrixStack stack, float x, float y, float z, float u, float v) {
        renderer
                .vertex(stack.last().pose(), x, y, z)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .uv(u, v)
                .uv2(0xf000f0)
                .normal(1, 0, 0)
                .endVertex();
    }


    public static void renderBeam(MatrixStack matrixStack, IRenderTypeBuffer buffer, ResourceLocation beam) {
        matrixStack.pushPose();

        IVertexBuilder builder = buffer.getBuffer(RenderType.translucent());

// @todo 1.15
//            GlStateManager.enableBlend();
//            GlStateManager.depthMask(false);
//            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
//            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
//            GlStateManager.disableCull();
//            GlStateManager.enableDepthTest();

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(beam);

        long ticks = (System.currentTimeMillis() / 100) % 10;
        float i1 = ticks / 10.0f;
        float i2 = i1 + .1f;

        float o = .15f;
        float height = 1;
        p(builder, matrixStack, o, height, o, 1, i2);
        p(builder, matrixStack, 1-o, height, o, 0, i2);
        p(builder, matrixStack, 1-o, 0, o, 0, i1);
        p(builder, matrixStack, o, 0, o, 1, i1);

        p(builder, matrixStack, 1-o, height, 1-o, 1, i2);
        p(builder, matrixStack, o, height, 1-o, 0, i2);
        p(builder, matrixStack, o, 0, 1-o, 0, i1);
        p(builder, matrixStack, 1-o, 0, 1-o, 1, i2);

        p(builder, matrixStack, o, height, 1-o, 1, i2);
        p(builder, matrixStack, o, height, o, 0, i2);
        p(builder, matrixStack, o, 0, o, 0, i1);
        p(builder, matrixStack, o, 0, 1-o, 1, i2);

        p(builder, matrixStack, 1-o, height, o, 1, i2);
        p(builder, matrixStack, 1-o, height, 1-o, 0, i2);
        p(builder, matrixStack, 1-o, 0, 1-o, 0, i1);
        p(builder, matrixStack, 1-o, 0, o, 1, i2);

        // @todo 1.15
//            GlStateManager.depthMask(true);
//            GlStateManager.enableLighting();
//            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);

        matrixStack.popPose();
    }
}
