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

public class NegariteTankRenderer extends ExtractedBlockRenderer<NegariteTankTile> {

    protected BlockEntityRendererProvider.Context context;
    public static final Identifier NEGARITE_BEAM = Identifier.fromNamespaceAndPath(Ariente.MODID, "block/machines/negarite_beam");
    public static final Identifier NEGARITE_WINDOW = Identifier.fromNamespaceAndPath(Ariente.MODID, "block/machines/negarite_tank_window");

    public NegariteTankRenderer(BlockEntityRendererProvider.Context pContext) {
        context = pContext;
    }

    @Override
    public void render(NegariteTankTile te, float partialTicks, PoseStack matrixStack, RenderBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        Vec3 cameraOffset = Minecraft.getInstance().gameRenderer.mainCamera().position()
                .subtract(Vec3.atCenterOf(te.getBlockPos()));
        TankRendererTools.renderTank(matrixStack, buffer, NEGARITE_BEAM, NEGARITE_WINDOW, te.isWorking(), te.getLevel().getGameTime(), cameraOffset, combinedLightIn);
    }

    public static void register() {
        BlockEntityRenderers.register(Registration.NEGARITE_TANK_TILE.get(), NegariteTankRenderer::new);
    }
}
