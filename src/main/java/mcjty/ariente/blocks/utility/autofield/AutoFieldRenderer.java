package mcjty.ariente.blocks.utility.autofield;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import mcjty.ariente.Ariente;
import mcjty.ariente.client.ArienteSpriteUploader;
import net.minecraft.client.renderer.RenderType;
import mcjty.ariente.setup.Registration;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.client.RenderSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

import java.util.Random;

public class AutoFieldRenderer implements BlockEntityRenderer<AutoFieldTile> {

    public static final ResourceLocation BEAMS[] = new ResourceLocation[3];
    static {
        BEAMS[0] = new ResourceLocation(Ariente.MODID, "autobeam1");
        BEAMS[1] = new ResourceLocation(Ariente.MODID, "autobeam2");
        BEAMS[2] = new ResourceLocation(Ariente.MODID, "autobeam3");
    }

    private Random random = new Random();
    protected BlockEntityRendererProvider.Context context;

    public AutoFieldRenderer(BlockEntityRendererProvider.Context pContext) {
        context = pContext;
    }

    @Override
    public void render(AutoFieldTile te, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        AABB box = te.getFieldBox();
        if (box == null) {
            return;
        }
        if (te.isRenderOutline()) {
            renderBeamBox(te, matrixStack, box, buffer);
        }
        if (te.isRenderItems()) {
            renderItemTransfers(matrixStack, te, buffer);
        }
    }

    private void renderBeamBox(AutoFieldTile te, PoseStack matrixStack, AABB box, MultiBufferSource buffer) {
        Minecraft mc = Minecraft.getInstance();

        TextureAtlasSprite sprite = ArienteSpriteUploader.INSTANCE.getSprite(BEAMS[(int) (te.getLevel().getGameTime() % 3)]);

        int tex = te.getBlockPos().getX();
        int tey = te.getBlockPos().getY();
        int tez = te.getBlockPos().getZ();
        Vec3 projectedView = mc.gameRenderer.getMainCamera().getPosition().add(-tex, -tey, -tez);
        Vec3 player = new Vec3((float)projectedView.x, (float)projectedView.y, (float)projectedView.z);

        VertexConsumer builder = buffer.getBuffer(RenderType.entityTranslucent(ArienteSpriteUploader.ARIENTE_ATLAS));

        float mx = (float) box.minX - tex;
        float my = (float) box.minY - tey;
        float mz = (float) box.minZ - tez;
        float px = (float) box.maxX - tex;
        float py = (float) box.maxY - tey;
        float pz = (float) box.maxZ - tez;

        Matrix4f matrix = matrixStack.last().pose();
        // @todo move to a static final once experimentation is done
        RenderSettings settings = RenderSettings.builder()
                .width(0.1f)
                .color(255, 255, 255)
                .alpha(128)
                .brightness(100)
                .build();

        RenderHelper.drawBeam(matrixStack, builder, sprite, new Vec3(mx, my, mz), new Vec3(px, my, mz), player, settings);
        RenderHelper.drawBeam(matrixStack, builder, sprite, new Vec3(mx, my, mz), new Vec3(mx, py, mz), player, settings);
        RenderHelper.drawBeam(matrixStack, builder, sprite, new Vec3(mx, my, mz), new Vec3(mx, my, pz), player, settings);

        RenderHelper.drawBeam(matrixStack, builder, sprite, new Vec3(px, py, pz), new Vec3(mx, py, pz), player, settings);
        RenderHelper.drawBeam(matrixStack, builder, sprite, new Vec3(px, py, pz), new Vec3(px, my, pz), player, settings);
        RenderHelper.drawBeam(matrixStack, builder, sprite, new Vec3(px, py, pz), new Vec3(px, py, mz), player, settings);

        RenderHelper.drawBeam(matrixStack, builder, sprite, new Vec3(px, my, mz), new Vec3(px, py, mz), player, settings);
        RenderHelper.drawBeam(matrixStack, builder, sprite, new Vec3(px, my, mz), new Vec3(px, my, pz), player, settings);
        RenderHelper.drawBeam(matrixStack, builder, sprite, new Vec3(mx, py, mz), new Vec3(px, py, mz), player, settings);
        RenderHelper.drawBeam(matrixStack, builder, sprite, new Vec3(mx, py, mz), new Vec3(mx, py, pz), player, settings);
        RenderHelper.drawBeam(matrixStack, builder, sprite, new Vec3(mx, my, pz), new Vec3(px, my, pz), player, settings);
        RenderHelper.drawBeam(matrixStack, builder, sprite, new Vec3(mx, my, pz), new Vec3(mx, py, pz), player, settings);
    }

    private void renderItemTransfers(PoseStack matrixStack, AutoFieldTile te, MultiBufferSource buffers) {
        te.clientRequestRenderInfo();

        TransferRender[] transferRenders = te.getTransferRenders();
        for (int i = 0 ; i < transferRenders.length ; i++) {
            TransferRender render = transferRenders[i];
            if (render == null) {
                if (random.nextInt(50) == 1) {
                    AutoFieldRenderInfo renderInfo = te.getClientRenderInfo();
                    if (renderInfo != null) {
                        AutoFieldRenderInfo.TransferPath path = renderInfo.getRandomPath();
                        if (path != null) {
                            AutoFieldRenderInfo.Transfer transfer = renderInfo.getRandomTransfer(path);
                            if (transfer != null) {
                                transferRenders[i] = new TransferRender(path, transfer, te.getBlockPos());
                            }
                        }
                    }
                }
            } else {
                if (!render.render(matrixStack, buffers)) {
                    transferRenders[i] = null;
                }
            }
        }
    }

    @Override
    public boolean shouldRenderOffScreen(AutoFieldTile te) {
        return true;
    }

    public static void register() {
        BlockEntityRenderers.register(Registration.AUTOFIELD_TILE.get(), AutoFieldRenderer::new);
    }
}
