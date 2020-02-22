package mcjty.ariente.blocks.generators;

import com.mojang.blaze3d.matrix.MatrixStack;
import mcjty.ariente.Ariente;
import mcjty.ariente.setup.Registration;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class PosiriteTankRenderer extends TileEntityRenderer<PosiriteTankTile> {

    public static final ResourceLocation POSIRITE_BEAM = new ResourceLocation(Ariente.MODID, "block/machines/posirite_beam");

    public PosiriteTankRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(PosiriteTankTile te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        if (te.isWorking()) {
            TankRendererTools.renderBeam(matrixStack, buffer, POSIRITE_BEAM);
        }

    }

    public static void register() {
        ClientRegistry.bindTileEntityRenderer(Registration.POSIRITE_TANK_TILE.get(), PosiriteTankRenderer::new);
    }
}
