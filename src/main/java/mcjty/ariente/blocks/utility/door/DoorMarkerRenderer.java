package mcjty.ariente.blocks.utility.door;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mcjty.ariente.Ariente;
import mcjty.ariente.setup.Registration;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class DoorMarkerRenderer extends TileEntityRenderer<DoorMarkerTile> {

    public static ResourceLocation halo = new ResourceLocation(Ariente.MODID, "textures/gui/guielements.png");

    public DoorMarkerRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(DoorMarkerTile te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        BlockState state = te.getWorld().getBlockState(te.getPos());
        if (state.getBlock() != Registration.DOOR_MARKER.get()) {
            return;
        }

        matrixStack.push();

        // @todo 1.15
//        Direction frontDirection = ModBlocks.doorMarkerBlock.get().getFrontDirection(state);
//        if (Direction.NORTH.equals(frontDirection) || Direction.SOUTH.equals(frontDirection)) {
//            GlStateManager.translated(x, y, z+.5);
//            GlStateManager.rotatef(90, 0, 1, 0);
//        } else {
//            GlStateManager.translated(x + .5, y, z);
//        }
//
//        bindTexture(halo);

        int openphase = getOpenphase(te);
        int iconIndex = te.getIconIndex();
        renderDoorSegment(matrixStack, buffer, openphase, iconIndex);

        matrixStack.pop();
    }

    public static void renderDoorSegment(MatrixStack matrixStack, IRenderTypeBuffer buffer, int openphase, int iconIndex) {
        // @todo 1.15
//        GlStateManager.disableBlend();
//        GlStateManager.enableDepthTest();
//        GlStateManager.depthMask(true);
//        GlStateManager.enableTexture();
//
//        GlStateManager.enableLighting();
//        Minecraft.getInstance().gameRenderer.getLightTexture().enableLightmap();
        // @todo figure out why entities cause this to flicker if the TE is rendered in pass 0 instead of pass 1


        float u = (4 + (iconIndex % 4));
        float v = (12 + (iconIndex / 4));

        u = (u*16) / 256.0f;
        v = (v*16) / 256.0f;
        float duv = 16.0f / 256.0f;

        if (openphase < 1000) {

            IVertexBuilder builder = buffer.getBuffer(RenderType.solid());

            float o = openphase / 2000.0f;
            float p = 1 - o;

            Matrix4f matrix = matrixStack.getLast().getPositionMatrix();
            builder.pos(matrix, -0.1f, o, o).tex(u, v).endVertex();
            builder.pos(matrix, -0.1f, o, p).tex(u+duv, v).endVertex();
            builder.pos(matrix, -0.1f, p, p).tex(u+duv, v+duv).endVertex();
            builder.pos(matrix, -0.1f, p, o).tex(u, v+duv).endVertex();

            builder.pos(matrix, .1f, p, o).tex(u, v).endVertex();
            builder.pos(matrix, .1f, p, p).tex(u+duv, v).endVertex();
            builder.pos(matrix, .1f, o, p).tex(u+duv, v+duv).endVertex();
            builder.pos(matrix, .1f, o, o).tex(u, v+duv).endVertex();

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
