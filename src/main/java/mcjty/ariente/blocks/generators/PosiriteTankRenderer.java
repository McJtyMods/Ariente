package mcjty.ariente.blocks.generators;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.Ariente;
import mcjty.ariente.setup.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class PosiriteTankRenderer implements BlockEntityRenderer<PosiriteTankTile> {

    protected BlockEntityRendererProvider.Context context;
    public static final ResourceLocation POSIRITE_BEAM = ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "block/machines/posirite_beam");
    public static final ResourceLocation POSIRITE_WINDOW = ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "block/machines/posirite_tank_window");

    public PosiriteTankRenderer(BlockEntityRendererProvider.Context pContext) {
        context = pContext;
    }

    @Override
    public void render(PosiriteTankTile te, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        Vec3 cameraOffset = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition()
                .subtract(Vec3.atCenterOf(te.getBlockPos()));
        TankRendererTools.renderWorldTank(matrixStack, POSIRITE_BEAM, POSIRITE_WINDOW, te.isWorking(), te.getLevel().getGameTime(), cameraOffset, combinedLightIn);
    }

    public static void register() {
        BlockEntityRenderers.register(Registration.POSIRITE_TANK_TILE.get(), PosiriteTankRenderer::new);
    }
}
