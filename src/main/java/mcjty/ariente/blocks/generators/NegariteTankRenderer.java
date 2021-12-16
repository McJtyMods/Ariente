package mcjty.ariente.blocks.generators;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.Ariente;
import mcjty.ariente.setup.Registration;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class NegariteTankRenderer extends TileEntityRenderer<NegariteTankTile> {

    public static final ResourceLocation NEGARITE_BEAM = new ResourceLocation(Ariente.MODID, "block/machines/negarite_beam");

    public NegariteTankRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(NegariteTankTile te, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        if (te.isWorking()) {
            TankRendererTools.renderBeam(matrixStack, buffer, NEGARITE_BEAM);
        }

    }

    public static void register() {
        ClientRegistry.bindTileEntityRenderer(Registration.NEGARITE_TANK_TILE.get(), NegariteTankRenderer::new);
    }
}
