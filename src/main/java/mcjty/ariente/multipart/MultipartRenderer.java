package mcjty.ariente.multipart;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.client.ArienteRenderType;
import mcjty.ariente.setup.Registration;
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
            if (part.getState().is(Registration.FIELD_MARKER.get())) {
                // World lightmap only: item/entity diffuse lights rotate with the view.
                Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(pose.last(), buffers.getBuffer(ArienteRenderType.FIELD_MARKER), part.getState(), Minecraft.getInstance().getBlockRenderer().getBlockModel(part.getState()), 1, 1, 1, light, overlay);
            } else {
                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(part.getState(), pose, buffers, light, overlay);
            }
        }
    }
}
