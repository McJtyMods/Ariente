package mcjty.ariente.blocks.utility.autofield;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import mcjty.ariente.Ariente;
import mcjty.ariente.client.ArienteRenderType;
import mcjty.ariente.client.WorldHolograms;
import mcjty.ariente.client.ArienteSpriteUploader;
import mcjty.ariente.setup.Registration;
import mcjty.lib.client.RenderSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

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
        if (te.isRenderOutline() && WorldHolograms.begin(te)) {
            renderBeamBox(te, WorldHolograms.copy(matrixStack), box, buffer, true);
        }
        if (te.isRenderItems()) {
            renderItemTransfers(matrixStack, te, buffer);
        }
    }

    public static void renderOutline(AutoFieldTile tile, PoseStack poses, MultiBufferSource buffer) {
        renderBeamBox(tile, poses, tile.getFieldBox(), buffer, false);
    }

    private static void renderBeamBox(AutoFieldTile te, PoseStack matrixStack, AABB box, MultiBufferSource buffer, boolean deferred) {
        Minecraft mc = Minecraft.getInstance();

        TextureAtlasSprite sprite = ArienteSpriteUploader.INSTANCE.getSprite(BEAMS[(int) (te.getLevel().getGameTime() % 3)]);

        int tex = te.getBlockPos().getX();
        int tey = te.getBlockPos().getY();
        int tez = te.getBlockPos().getZ();
        Vec3 projectedView = mc.gameRenderer.getMainCamera().getPosition().add(-tex, -tey, -tez);
        Vec3 player = new Vec3((float)projectedView.x, (float)projectedView.y, (float)projectedView.z);

        float mx = (float) box.minX - tex;
        float my = (float) box.minY - tey;
        float mz = (float) box.minZ - tez;
        float px = (float) box.maxX - tex;
        float py = (float) box.maxY - tey;
        float pz = (float) box.maxZ - tez;

        // @todo move to a static final once experimentation is done
        RenderSettings settings = RenderSettings.builder()
                .width(0.1f)
                .color(128, 128, 128)
                .alpha(128)
                .brightness(100)
                .build();

        emitBeam(te, matrixStack, buffer, deferred, sprite, new Vec3(mx, my, mz), new Vec3(px, my, mz), player, settings);
        emitBeam(te, matrixStack, buffer, deferred, sprite, new Vec3(mx, my, mz), new Vec3(mx, py, mz), player, settings);
        emitBeam(te, matrixStack, buffer, deferred, sprite, new Vec3(mx, my, mz), new Vec3(mx, my, pz), player, settings);

        emitBeam(te, matrixStack, buffer, deferred, sprite, new Vec3(px, py, pz), new Vec3(mx, py, pz), player, settings);
        emitBeam(te, matrixStack, buffer, deferred, sprite, new Vec3(px, py, pz), new Vec3(px, my, pz), player, settings);
        emitBeam(te, matrixStack, buffer, deferred, sprite, new Vec3(px, py, pz), new Vec3(px, py, mz), player, settings);

        emitBeam(te, matrixStack, buffer, deferred, sprite, new Vec3(px, my, mz), new Vec3(px, py, mz), player, settings);
        emitBeam(te, matrixStack, buffer, deferred, sprite, new Vec3(px, my, mz), new Vec3(px, my, pz), player, settings);
        emitBeam(te, matrixStack, buffer, deferred, sprite, new Vec3(mx, py, mz), new Vec3(px, py, mz), player, settings);
        emitBeam(te, matrixStack, buffer, deferred, sprite, new Vec3(mx, py, mz), new Vec3(mx, py, pz), player, settings);
        emitBeam(te, matrixStack, buffer, deferred, sprite, new Vec3(mx, my, pz), new Vec3(px, my, pz), player, settings);
        emitBeam(te, matrixStack, buffer, deferred, sprite, new Vec3(mx, my, pz), new Vec3(mx, py, pz), player, settings);
    }

    private static void emitBeam(AutoFieldTile tile, PoseStack poses, MultiBufferSource buffer, boolean deferred,
                                 TextureAtlasSprite sprite, Vec3 start, Vec3 end, Vec3 camera, RenderSettings settings) {
        if (deferred) {
            Vec3 center = Vec3.atLowerCornerOf(tile.getBlockPos()).add(start.add(end).scale(.5));
            WorldHolograms.add(center, buffers -> drawBeam(poses, buffers.getBuffer(ArienteRenderType.FIELD_OUTLINE), sprite,
                    start, end, camera, settings));
        } else {
            drawBeam(poses, buffer.getBuffer(ArienteRenderType.FIELD_OUTLINE), sprite, start, end, camera, settings);
        }
    }

    private static void drawBeam(PoseStack stack, VertexConsumer vertices, TextureAtlasSprite sprite,
                                 Vec3 start, Vec3 end, Vec3 camera, RenderSettings settings) {
        Vec3 half = start.subtract(camera).cross(end.subtract(start)).normalize().scale(settings.width());
        Vec3 first = start.add(half);
        Vec3 second = end.add(half);
        Vec3 third = end.subtract(half);
        Vec3 fourth = start.subtract(half);
        Vec3 normal = second.subtract(first).cross(third.subtract(first)).normalize();
        vertex(stack, vertices, first, normal, sprite.getU0(), sprite.getV0(), settings);
        vertex(stack, vertices, second, normal, sprite.getU1(), sprite.getV0(), settings);
        vertex(stack, vertices, third, normal, sprite.getU1(), sprite.getV1(), settings);
        vertex(stack, vertices, fourth, normal, sprite.getU0(), sprite.getV1(), settings);
    }

    private static void vertex(PoseStack stack, VertexConsumer vertices, Vec3 pos, Vec3 normal,
                               float u, float v, RenderSettings settings) {
        // The outline retains the complete NEW_ENTITY layout for renderer compatibility.
        vertices.vertex(stack.last().pose(), (float) pos.x, (float) pos.y, (float) pos.z)
                .color(settings.r(), settings.g(), settings.b(), settings.a())
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(settings.brightness())
                .normal(stack.last().normal(), (float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
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
