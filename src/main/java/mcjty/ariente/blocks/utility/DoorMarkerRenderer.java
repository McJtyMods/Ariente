package mcjty.ariente.blocks.utility;

import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.ModBlocks;
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

    private ResourceLocation halo = new ResourceLocation(Ariente.MODID, "textures/blocks/machines/door_panel.png");

    public DoorMarkerRenderer() {
    }

    @Override
    public void render(DoorMarkerTile te, double x, double y, double z, float time, int breakTime, float alpha) {
//        if (te.isWorking()) {
        Tessellator tessellator = Tessellator.getInstance();
        GlStateManager.pushMatrix();

        EnumFacing frontDirection = ModBlocks.doorMarkerBlock.getFrontDirection(getWorld().getBlockState(te.getPos()));
        if (EnumFacing.NORTH.equals(frontDirection) || EnumFacing.SOUTH.equals(frontDirection)) {
            GlStateManager.translate(x, y, z+.5);
            GlStateManager.rotate(90, 0, 1, 0);
        } else {
            GlStateManager.translate(x + .5, y, z);
        }


        GlStateManager.disableBlend();

        ResourceLocation beamIcon = halo;
        bindTexture(beamIcon);

        Minecraft mc = Minecraft.getMinecraft();
        boolean opening = false;
        double sqdist = mc.player.getPositionVector().squareDistanceTo(te.getPos().getX() + .5, te.getPos().getY() + 1, te.getPos().getZ() + .5);
        if (sqdist < 4*4) {
            opening = true;
        }

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

        if (openphase < 1000) {

            GlStateManager.color(1, 1, 1, 1);

            BufferBuilder renderer = tessellator.getBuffer();
            renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

            BufferBuilder buffer = tessellator.getBuffer();

            float o = openphase / 2000.0f;

            buffer.pos(0, o, o).tex(0.0D, 0.0D).endVertex();
            buffer.pos(0, o, 1 - o).tex(1.0D, 0.0D).endVertex();
            buffer.pos(0, 1 - o, 1 - o).tex(1.0D, 1.0D).endVertex();
            buffer.pos(0, 1 - o, o).tex(0.0D, 1.0D).endVertex();

            buffer.pos(0, 1 - o, o).tex(0.0D, 0.0D).endVertex();
            buffer.pos(0, 1 - o, 1 - o).tex(1.0D, 0.0D).endVertex();
            buffer.pos(0, o, 1 - o).tex(1.0D, 1.0D).endVertex();
            buffer.pos(0, o, o).tex(0.0D, 1.0D).endVertex();

            for (int yy = 1; yy < 10; yy++) {
                if (getWorld().isAirBlock(te.getPos().up(yy))) {
                    buffer.pos(0, yy + o, o).tex(0.0D, 0.0D).endVertex();
                    buffer.pos(0, yy + o, 1 - o).tex(1.0D, 0.0D).endVertex();
                    buffer.pos(0, yy + 1 - o, 1 - o).tex(1.0D, 1.0D).endVertex();
                    buffer.pos(0, yy + 1 - o, o).tex(0.0D, 1.0D).endVertex();

                    buffer.pos(0, yy + 1 - o, o).tex(0.0D, 0.0D).endVertex();
                    buffer.pos(0, yy + 1 - o, 1 - o).tex(1.0D, 0.0D).endVertex();
                    buffer.pos(0, yy + o, 1 - o).tex(1.0D, 1.0D).endVertex();
                    buffer.pos(0, yy + o, o).tex(0.0D, 1.0D).endVertex();
                } else {
                    break;
                }
            }


            tessellator.draw();

        }

        GlStateManager.popMatrix();
    }
}
