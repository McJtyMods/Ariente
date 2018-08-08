package mcjty.ariente.blocks.utility;

import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class DoorMarkerRenderer extends TileEntitySpecialRenderer<DoorMarkerTile> {

    private ResourceLocation halo = new ResourceLocation(Ariente.MODID, "textures/gui/guielements.png");

    public DoorMarkerRenderer() {
    }

    @Override
    public void render(DoorMarkerTile te, double x, double y, double z, float time, int breakTime, float alpha) {
        IBlockState state = getWorld().getBlockState(te.getPos());
        if (state.getBlock() != ModBlocks.doorMarkerBlock) {
            return;
        }

        Tessellator tessellator = Tessellator.getInstance();
        GlStateManager.pushMatrix();

        EnumFacing frontDirection = ModBlocks.doorMarkerBlock.getFrontDirection(state);
        if (EnumFacing.NORTH.equals(frontDirection) || EnumFacing.SOUTH.equals(frontDirection)) {
            GlStateManager.translate(x, y, z+.5);
            GlStateManager.rotate(90, 0, 1, 0);
        } else {
            GlStateManager.translate(x + .5, y, z);
        }


//        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();

//        GlStateManager.disableLighting();
//        Minecraft.getMinecraft().entityRenderer.disableLightmap();
        GlStateManager.enableLighting();
//        GlStateManager.enableRescaleNormal();
        Minecraft.getMinecraft().entityRenderer.enableLightmap();
//        int light = Minecraft.getMinecraft().world.getCombinedLight(new BlockPos(MathHelper.floor(te.getPos().getX()), MathHelper.floor(te.getPos().getY()), MathHelper.floor(te.getPos().getZ())), 0);
//        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)(light & 0xFFFF), (float)((light >> 16) & 0xFFFF));

//        GlStateManager.enableLighting();
        // @todo figure out why entities cause this to flicker if the TE is rendered in pass 0 instead of pass 1

        bindTexture(halo);

        int openphase = getOpenphase(te);

        int iconIndex = te.getIconIndex();
        float u = (4 + (iconIndex % 4));
        float v = (12 + (iconIndex / 4));

        u = (u*16) / 256.0f;
        v = (v*16) / 256.0f;
        float duv = 16.0f / 256.0f;

        if (openphase < 1000) {

            GlStateManager.color(1, 1, 1, 1);

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

            for (int yy = 1; yy < 10; yy++) {
                if (getWorld().isAirBlock(te.getPos().up(yy))) {
                    buffer.pos(-.1, yy + o, o).tex(u, v).endVertex();
                    buffer.pos(-.1, yy + o, p).tex(u+duv, v).endVertex();
                    buffer.pos(-.1, yy + p, p).tex(u+duv, v+duv).endVertex();
                    buffer.pos(-.1, yy + p, o).tex(u, v+duv).endVertex();

                    buffer.pos(.1, yy + p, o).tex(u, v).endVertex();
                    buffer.pos(.1, yy + p, p).tex(u+duv, v).endVertex();
                    buffer.pos(.1, yy + o, p).tex(u+duv, v+duv).endVertex();
                    buffer.pos(.1, yy + o, o).tex(u, v+duv).endVertex();
                } else {
                    break;
                }
            }

            tessellator.draw();
        }

        GlStateManager.popMatrix();
    }

    private int getOpenphase(DoorMarkerTile te) {
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

    @Override
    public boolean isGlobalRenderer(DoorMarkerTile te) {
        return true;
    }
}
