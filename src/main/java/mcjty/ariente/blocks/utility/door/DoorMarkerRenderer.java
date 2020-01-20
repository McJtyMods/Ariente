package mcjty.ariente.blocks.utility.door;

import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.opengl.GL11;

public class DoorMarkerRenderer extends TileEntityRenderer<DoorMarkerTile> {

    public static ResourceLocation halo = new ResourceLocation(Ariente.MODID, "textures/gui/guielements.png");

    public DoorMarkerRenderer() {
    }

    @Override
    public void render(DoorMarkerTile te, double x, double y, double z, float partialTicks, int destroyStage) {
        BlockState state = getWorld().getBlockState(te.getPos());
        if (state.getBlock() != ModBlocks.doorMarkerBlock.get()) {
            return;
        }

        GlStateManager.pushMatrix();

        Direction frontDirection = ModBlocks.doorMarkerBlock.get().getFrontDirection(state);
        if (Direction.NORTH.equals(frontDirection) || Direction.SOUTH.equals(frontDirection)) {
            GlStateManager.translated(x, y, z+.5);
            GlStateManager.rotatef(90, 0, 1, 0);
        } else {
            GlStateManager.translated(x + .5, y, z);
        }

        bindTexture(halo);

        int openphase = getOpenphase(te);
        int iconIndex = te.getIconIndex();
        renderDoorSegment(openphase, iconIndex);

        GlStateManager.popMatrix();
    }

    public static void renderDoorSegment(int openphase, int iconIndex) {
        Tessellator tessellator = Tessellator.getInstance();
        //        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableBlend();
        GlStateManager.enableDepthTest();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture();

//        GlStateManager.disableLighting();
//        Minecraft.getInstance().entityRenderer.disableLightmap();
        GlStateManager.enableLighting();
//        GlStateManager.enableRescaleNormal();
        Minecraft.getInstance().gameRenderer.enableLightmap();
//        int light = Minecraft.getInstance().world.getCombinedLight(new BlockPos(MathHelper.floor(te.getPos().getX()), MathHelper.floor(te.getPos().getY()), MathHelper.floor(te.getPos().getZ())), 0);
//        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)(light & 0xFFFF), (float)((light >> 16) & 0xFFFF));

//        GlStateManager.enableLighting();
        // @todo figure out why entities cause this to flicker if the TE is rendered in pass 0 instead of pass 1


        float u = (4 + (iconIndex % 4));
        float v = (12 + (iconIndex / 4));

        u = (u*16) / 256.0f;
        v = (v*16) / 256.0f;
        float duv = 16.0f / 256.0f;

        if (openphase < 1000) {

            GlStateManager.color4f(1, 1, 1, 1);

            BufferBuilder renderer = tessellator.getBuffer();
            renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

            BufferBuilder buffer = tessellator.getBuffer();

            float o = openphase / 2000.0f;
            float p = 1 - o;

            buffer.pos(-0.1, o, o).tex(u, v).endVertex();
            buffer.pos(-0.1, o, p).tex(u+duv, v).endVertex();
            buffer.pos(-0.1, p, p).tex(u+duv, v+duv).endVertex();
            buffer.pos(-0.1, p, o).tex(u, v+duv).endVertex();

            buffer.pos(.1, p, o).tex(u, v).endVertex();
            buffer.pos(.1, p, p).tex(u+duv, v).endVertex();
            buffer.pos(.1, o, p).tex(u+duv, v+duv).endVertex();
            buffer.pos(.1, o, o).tex(u, v+duv).endVertex();

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

            tessellator.draw();
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
        ClientRegistry.bindTileEntitySpecialRenderer(DoorMarkerTile.class, new DoorMarkerRenderer());
    }
}
