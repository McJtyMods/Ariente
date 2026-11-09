package mcjty.ariente.blocks.utility.door;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mcjty.ariente.Ariente;
import mcjty.ariente.client.ArienteRenderType;
import mcjty.ariente.client.ArienteSpriteUploader;
import mcjty.ariente.setup.Registration;
import mcjty.lib.client.RenderBufferSource;
import mcjty.ariente.client.ExtractedBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public class DoorMarkerRenderer extends ExtractedBlockRenderer<DoorMarkerTile> {

    protected BlockEntityRendererProvider.Context context;
    public static final Identifier DOOR_MARKER_TEXTURE = Identifier.fromNamespaceAndPath(Ariente.MODID, "doormarkers");

    public DoorMarkerRenderer(BlockEntityRendererProvider.Context pContext) {
        context = pContext;
    }

    @Override
    public AABB getRenderBoundingBox(DoorMarkerTile tile) {
        return tile.getRenderBoundingBox();
    }

    @Override
    public void render(DoorMarkerTile te, float partialTicks, PoseStack matrixStack, RenderBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        BlockState state = te.getLevel().getBlockState(te.getBlockPos());
        if (state.getBlock() != Registration.DOOR_MARKER.get()) {
            return;
        }

        matrixStack.pushPose();

        Direction frontDirection = Registration.DOOR_MARKER.get().getFrontDirection(state);
        if (Direction.NORTH.equals(frontDirection) || Direction.SOUTH.equals(frontDirection)) {
            matrixStack.translate(0, 0, .5);
            matrixStack.rotate(Axis.YP.rotationDegrees(90));
        } else {
            matrixStack.translate(.5, 0, 0);
        }

        int openphase = getOpenphase(te);
        int iconIndex = te.getIconIndex();
        renderDoorSegment(matrixStack, buffer, openphase, iconIndex, combinedLightIn, combinedOverlayIn);

        matrixStack.popPose();
    }

    public static void renderDoorSegment(PoseStack matrixStack, RenderBufferSource buffer, int openphase, int iconIndex, int combinedLightIn, int combinedOverlayIn) {

        TextureAtlasSprite sprite = ArienteSpriteUploader.INSTANCE.getSprite(DOOR_MARKER_TEXTURE);

        float u = (iconIndex % 4);
        float v = (iconIndex / 4);

        float du = (sprite.getU1() - sprite.getU0()) / 8.0f;
        float dv = (sprite.getV1() - sprite.getV0()) / 4.0f;
        u = sprite.getU0() + u * du;
        v = sprite.getV0() + v * dv;

        if (openphase < 1000) {

            VertexConsumer builder = buffer.getBuffer(ArienteRenderType.ARIENTE_SOLID);

            float o = openphase / 2000.0f;
            float p = 1 - o;

            Matrix4f matrix = matrixStack.last().pose();

            vertex(builder, matrixStack, -0.1f, o, o, u, v, combinedLightIn, -1);
            vertex(builder, matrixStack, -0.1f, o, p, u+du, v, combinedLightIn, -1);
            vertex(builder, matrixStack, -0.1f, p, p, u+du, v+dv, combinedLightIn, -1);
            vertex(builder, matrixStack, -0.1f, p, o, u, v+dv, combinedLightIn, -1);

            vertex(builder, matrixStack, .1f, p, o, u, v, combinedLightIn, 1);
            vertex(builder, matrixStack, .1f, p, p, u+du, v, combinedLightIn, 1);
            vertex(builder, matrixStack, .1f, o, p, u+du, v+dv, combinedLightIn, 1);
            vertex(builder, matrixStack, .1f, o, o, u, v+dv, combinedLightIn, 1);

        }
    }

    private static void vertex(VertexConsumer builder, PoseStack poses, float x, float y, float z, float u, float v, int light, float normalX) {
        builder.addVertex(poses.last().pose(), x, y, z).setColor(255, 255, 255, 255).setUv(u, v).setLight(light)
                .setNormal(poses.last(), normalX, 0, 0);
    }

    public static int getOpenphase(DoorMarkerTile te) {
        boolean opening = te.isOpen();
        int openphase = te.getOpening();
        long t = System.currentTimeMillis();
        long last = te.getLastTime();
        if (last != -1) {
            int dt = (int) (t-last);
            if (opening) {
                openphase += dt;
                if (openphase > 1000) {
                    openphase = 1000;
                }
            } else {
                openphase -= dt;
                if (openphase < 0) {
                    openphase = 0;
                }
            }
            te.setOpening(openphase);
        }
        te.setLastTime(t);
        return openphase;
    }

    public static void register() {
        BlockEntityRenderers.register(Registration.DOOR_MARKER_TILE.get(), DoorMarkerRenderer::new);
    }
}
