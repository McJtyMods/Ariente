package mcjty.ariente.blocks.purifier.client;

import mcjty.ariente.client.ArienteRenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mcjty.ariente.blocks.purifier.PurifierTubeTile;
import mcjty.lib.client.RenderBufferSource;
import mcjty.ariente.client.ExtractedBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class PurifierTubeRenderer extends ExtractedBlockRenderer<PurifierTubeTile> {

    public PurifierTubeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PurifierTubeTile tube, float partialTicks, PoseStack poses, RenderBufferSource buffer, int light, int overlay) {
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
                            PoseStack poses, RenderBufferSource buffer, int light) {
        if (alpha <= .001f || stack.isEmpty()) {
            return;
        }
        poses.pushPose();
        poses.scale(scale, scale, scale);
        ArienteRenderHelper.item(poses, buffer, stack, ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY, 1, alpha);
        poses.popPose();
    }
}
