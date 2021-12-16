package mcjty.ariente.blocks.utility.door;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import mcjty.ariente.Ariente;
import mcjty.ariente.client.ArienteRenderType;
import mcjty.ariente.client.ArienteSpriteUploader;
import mcjty.ariente.setup.Registration;
import mcjty.lib.client.RenderHelper;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class DoorMarkerRenderer extends TileEntityRenderer<DoorMarkerTile> {

    public static final ResourceLocation DOOR_MARKER_TEXTURE = new ResourceLocation(Ariente.MODID, "doormarkers");

    public DoorMarkerRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(DoorMarkerTile te, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        BlockState state = te.getLevel().getBlockState(te.getBlockPos());
        if (state.getBlock() != Registration.DOOR_MARKER.get()) {
            return;
        }

        matrixStack.pushPose();

        Direction frontDirection = Registration.DOOR_MARKER.get().getFrontDirection(state);
        if (Direction.NORTH.equals(frontDirection) || Direction.SOUTH.equals(frontDirection)) {
            matrixStack.translate(0, 0, .5);
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        } else {
            matrixStack.translate(.5, 0, 0);
        }

        int openphase = getOpenphase(te);
        int iconIndex = te.getIconIndex();
        renderDoorSegment(matrixStack, buffer, openphase, iconIndex, combinedLightIn, combinedOverlayIn);

        matrixStack.popPose();
    }

    public static void renderDoorSegment(PoseStack matrixStack, MultiBufferSource buffer, int openphase, int iconIndex, int combinedLightIn, int combinedOverlayIn) {

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

            RenderHelper.vt(builder, matrix, -0.1f, o, o, u, v, combinedLightIn, combinedOverlayIn, 255, 255, 255, 255);
            RenderHelper.vt(builder, matrix, -0.1f, o, p, u+du, v, combinedLightIn, combinedOverlayIn, 255, 255, 255, 255);
            RenderHelper.vt(builder, matrix, -0.1f, p, p, u+du, v+dv, combinedLightIn, combinedOverlayIn, 255, 255, 255, 255);
            RenderHelper.vt(builder, matrix, -0.1f, p, o, u, v+dv, combinedLightIn, combinedOverlayIn, 255, 255, 255, 255);

            RenderHelper.vt(builder, matrix, .1f, p, o, u, v, combinedLightIn, combinedOverlayIn, 255, 255, 255, 255);
            RenderHelper.vt(builder, matrix, .1f, p, p, u+du, v, combinedLightIn, combinedOverlayIn, 255, 255, 255, 255);
            RenderHelper.vt(builder, matrix, .1f, o, p, u+du, v+dv, combinedLightIn, combinedOverlayIn, 255, 255, 255, 255);
            RenderHelper.vt(builder, matrix, .1f, o, o, u, v+dv, combinedLightIn, combinedOverlayIn, 255, 255, 255, 255);

//            for (int yy = 1; yy < 10; yy++) {
//                if (getWorld().isAirBlock(te.getPos().up(yy))) {
//                    buffer.pos(-.1, yy + o, o).tex(u, v).endVertex();
//                    buffer.pos(-.1, yy + o, p).tex(u+duv, v).endVertex();
//                    buffer.pos(-.1, yy + p, p).tex(u+duv, v+duv).endVertex();
//                    buffer.pos(-.1, yy + p, o).tex(u, v+duv).endVertex();
//
//                    buffer.pos(.1, yy + p, o).tex(u, v).endVertex();
//                    buffer.pos(.1, yy + p, p).tex(u+duv, v).endVertex();
//                    buffer.pos(.1, yy + o, p).tex(u+duv, v+duv).endVertex();
//                    buffer.pos(.1, yy + o, o).tex(u, v+duv).endVertex();
//                } else {
//                    break;
//                }
//            }
        }
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
        ClientRegistry.bindTileEntityRenderer(Registration.DOOR_MARKER_TILE.get(), DoorMarkerRenderer::new);
    }
}
