package mcjty.ariente.blocks.purifier.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mcjty.ariente.blocks.purifier.PurifierTubeTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.pipeline.VertexConsumerWrapper;

public class PurifierTubeRenderer implements BlockEntityRenderer<PurifierTubeTile> {

    public PurifierTubeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PurifierTubeTile tube, float partialTicks, PoseStack poses, MultiBufferSource buffer, int light, int overlay) {
        if (tube.getOre().isEmpty()) {
            return;
        }
        float fraction = tube.getFraction(partialTicks);
        float position = fraction * tube.getTubeCount();
        if (position < tube.getIndex() || position >= tube.getIndex() + 1) {
            return;
        }
        double along = position - tube.getIndex() - .5;
        double time = tube.getLevel().getGameTime() + partialTicks;
        poses.pushPose();
        poses.translate(.5 + tube.getDirection().getStepX() * along, .5 + Math.sin(time * .06) * .025,
                .5 + tube.getDirection().getStepZ() * along);
        poses.mulPose(Axis.YP.rotationDegrees((float) time));
        renderItem(tube, tube.getOre(), 1 - fraction, .75f, poses, buffer, light);
        int count = Math.min(5, tube.getRaw().getCount());
        for (int i = 0; i < count; i++) {
            poses.pushPose();
            double angle = i * Math.PI * 2 / count;
            poses.translate(Math.cos(angle) * .12 * fraction, Math.sin(angle) * .12 * fraction, .015 * i);
            renderItem(tube, tube.getRaw(), fraction, .55f, poses, buffer, light);
            poses.popPose();
        }
        poses.popPose();
    }

    private void renderItem(PurifierTubeTile tube, ItemStack stack, float alpha, float scale,
                            PoseStack poses, MultiBufferSource buffer, int light) {
        if (alpha <= .001f || stack.isEmpty()) {
            return;
        }
        poses.pushPose();
        poses.scale(scale, scale, scale);
        VertexConsumer vertices = new VertexConsumerWrapper(buffer.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS))) {

            @Override
            public VertexConsumer color(int red, int green, int blue, int opacity) {
                return super.color(red, green, blue, Math.round(opacity * alpha));
            }
        };
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, light,
                OverlayTexture.NO_OVERLAY, poses, type -> vertices, tube.getLevel(), 0);
        poses.popPose();
    }
}
