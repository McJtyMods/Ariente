package mcjty.ariente.blocks.generators;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.Ariente;
import mcjty.ariente.setup.Registration;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;

public class NegariteTankRenderer implements BlockEntityRenderer<NegariteTankTile> {

    protected BlockEntityRendererProvider.Context context;
    public static final ResourceLocation NEGARITE_BEAM = new ResourceLocation(Ariente.MODID, "block/machines/negarite_beam");

    public NegariteTankRenderer(BlockEntityRendererProvider.Context pContext) {
        context = pContext;
    }

    @Override
    public void render(NegariteTankTile te, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        if (te.isWorking()) {
            TankRendererTools.renderBeam(matrixStack, buffer, NEGARITE_BEAM);
        }

    }

    public static void register() {
        BlockEntityRenderers.register(Registration.NEGARITE_TANK_TILE.get(), NegariteTankRenderer::new);
    }
}
