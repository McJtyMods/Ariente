package mcjty.ariente.multipart;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class MultipartRenderer implements BlockEntityRenderer<MultipartTE> {

    public MultipartRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MultipartTE tile, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        for (MultipartTE.Part part : tile.getParts().values()) {
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(part.getState(), pose, buffers, light, overlay);
        }
    }
}
