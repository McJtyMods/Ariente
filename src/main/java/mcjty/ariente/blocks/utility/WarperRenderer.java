package mcjty.ariente.blocks.utility;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mcjty.ariente.display.client.CameraDisplayRenderer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import mcjty.ariente.Ariente;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.client.ArienteRenderType;
import mcjty.ariente.client.BeamRenderTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.resources.ResourceLocation;

public class WarperRenderer implements BlockEntityRenderer<WarperTile> {

    public static final ResourceLocation HALO = new ResourceLocation(Ariente.MODID, "block/machines/warper_beam");

    protected BlockEntityRendererProvider.Context context;

    public WarperRenderer(BlockEntityRendererProvider.Context pContext) {
            context = pContext;
    }

    private static float randomX[] = new float[]{.2f, .3f, .2f, .7f, .8f, .5f, .2f, .8f, .4f, .6f};
    private static float randomZ[] = new float[]{.3f, .2f, .8f, .3f, .7f, .6f, .4f, .5f, .2f, .3f};
    private static int randomY[] = new int[]{0, 3, 2, 1, 6, 5, 6, 8, 2, 3};

    @Override
    public void render(WarperTile te, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        if (te.getDestination() != null) {
            if (Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceToSqr(Vec3.atCenterOf(te.getBlockPos())) > 32 * 32) {
                return;
            }
            matrixStack.pushPose();
            // Keep the pad and its approach visible below a compact, walk-through hologram.
            matrixStack.translate(.5, 2, .5);
            matrixStack.mulPose(Axis.YP.rotationDegrees(((te.getLevel().getGameTime() % 720) + partialTicks) * .5f));
            CameraDisplayRenderer.renderMiniature(matrixStack, buffer, te, te.getDestinationScene(), 1.6f, 210);
            matrixStack.popPose();
            return;
        }
        ResourceLocation beamIcon = HALO;

        Minecraft mc = Minecraft.getInstance();
        int tex = te.getBlockPos().getX();
        int tey = te.getBlockPos().getY();
        int tez = te.getBlockPos().getZ();
        Vec3 projectedView = mc.gameRenderer.getMainCamera().getPosition().add(-tex, -tey, -tez);
        Vec3 player = new Vec3((float)projectedView.x, (float)projectedView.y, (float)projectedView.z);

        long tt = System.currentTimeMillis() / 100;

        float height = 2;

        VertexConsumer builder = buffer.getBuffer(ArienteRenderType.BEAM);
        TextureAtlasSprite sprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(beamIcon);

        for (int i = 0; i < 10; i++) {
            int ii = i % 10;
            long ticks = (tt + randomY[ii]) % 80;
            if (ticks > 40) {
                ticks = 80 - ticks;
            }
            float i1 = ticks / 40.0f;
            float xx = randomX[ii];
            float zz = randomZ[ii];
            float yy = - 1.0f + i1 + (randomY[ii] * height) / 8.0f;
            BeamRenderTools.drawBeam(matrixStack, builder, sprite, new Vec3(xx, yy, zz), new Vec3(xx, yy + 4, zz), player, 0.2f);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(WarperTile te) {
        return true;
    }

    public static void register() {
        BlockEntityRenderers.register(Registration.WARPER_TILE.get(), WarperRenderer::new);
    }
}
