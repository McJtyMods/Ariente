package mcjty.ariente.multipart;

import mcjty.hologui.gui.HoloRenderBatch;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import mcjty.lib.client.RenderBufferSource;
import mcjty.ariente.client.ExtractedBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class MultipartRenderer extends ExtractedBlockRenderer<MultipartTE> {

    public MultipartRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MultipartTE tile, float partialTick, PoseStack pose, RenderBufferSource buffers, int light, int overlay) {
        for (MultipartTE.Part part : tile.getParts().values()) {
            var state = new BlockModelRenderState();
            Minecraft.getInstance().getModelManager().getBlockModelSet().get(part.getState()).update(state, part.getState(), BlockDisplayContext.create(), 0);
            ((HoloRenderBatch) buffers).command(pose,
                    (saved, collector) -> state.submit(saved, collector, light, overlay, 0));
        }
    }
}
