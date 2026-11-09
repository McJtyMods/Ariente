package mcjty.ariente.blocks.generators;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.Ariente;
import mcjty.ariente.setup.Registration;
import net.minecraft.client.Minecraft;
import mcjty.lib.client.RenderBufferSource;
import mcjty.ariente.client.ExtractedBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class PosiriteTankRenderer extends ExtractedBlockRenderer<PosiriteTankTile> {

    protected BlockEntityRendererProvider.Context context;
    public static final Identifier POSIRITE_BEAM = Identifier.fromNamespaceAndPath(Ariente.MODID, "block/machines/posirite_beam");
    public static final Identifier POSIRITE_WINDOW = Identifier.fromNamespaceAndPath(Ariente.MODID, "block/machines/posirite_tank_window");

    public PosiriteTankRenderer(BlockEntityRendererProvider.Context pContext) {
        context = pContext;
    }

    @Override
    public void render(PosiriteTankTile te, float partialTicks, PoseStack matrixStack, RenderBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        Vec3 cameraOffset = Minecraft.getInstance().gameRenderer.mainCamera().position()
                .subtract(Vec3.atCenterOf(te.getBlockPos()));
        TankRendererTools.renderTank(matrixStack, buffer, POSIRITE_BEAM, POSIRITE_WINDOW, te.isWorking(), te.getLevel().getGameTime(), cameraOffset, combinedLightIn);
    }

    public static void register() {
        BlockEntityRenderers.register(Registration.POSIRITE_TANK_TILE.get(), PosiriteTankRenderer::new);
    }
}
