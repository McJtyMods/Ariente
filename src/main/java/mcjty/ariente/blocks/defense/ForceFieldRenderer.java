package mcjty.ariente.blocks.defense;

import mcjty.ariente.Ariente;
import mcjty.ariente.varia.Triangle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.HashSet;
import java.util.Set;

public class ForceFieldRenderer {

    private static final ResourceLocation FORCEFIELD = new ResourceLocation(Ariente.MODID, "textures/effects/forcefield.png");

    private static final Set<BlockPos> forceFields = new HashSet<>();    // A set of force fields that are in render range

    public static void register(BlockPos pos) {
        forceFields.add(pos);
    }

    public static void unregister(BlockPos pos) {
        forceFields.remove(pos);
    }

    public static void renderForceFields(float partialTicks) {
        Set<BlockPos> toRemove = new HashSet<>();
        WorldClient world = Minecraft.getMinecraft().world;
        for (BlockPos pos : forceFields) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof ForceFieldTile) {
                Minecraft mc = Minecraft.getMinecraft();
                mc.entityRenderer.disableLightmap();
                GlStateManager.enableTexture2D();
                GlStateManager.disableLighting();
                GlStateManager.enableBlend();
                GlStateManager.depthMask(false);
                GlStateManager.color(1.0f, 1.0f, 1.0f, 0.2f);
                mc.renderEngine.bindTexture(FORCEFIELD);

                Tessellator t = Tessellator.getInstance();
                BufferBuilder builder = t.getBuffer();
                builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX);

                Entity renderViewEntity = Minecraft.getMinecraft().renderViewEntity;
                double dx = renderViewEntity.lastTickPosX + (renderViewEntity.posX - renderViewEntity.lastTickPosX) * partialTicks;
                double dy = renderViewEntity.lastTickPosY + (renderViewEntity.posY - renderViewEntity.lastTickPosY) * partialTicks;
                double dz = renderViewEntity.lastTickPosZ + (renderViewEntity.posZ - renderViewEntity.lastTickPosZ) * partialTicks;

                ForceFieldTile forcefield = (ForceFieldTile) te;
                PanelInfo[] panelInfo = forcefield.getPanelInfo();
                for (PanelInfo info : panelInfo) {
                    if (info != null) {
                        double scale = info.getScale();
                        doRender(info, pos.getX() + .5 - dx, pos.getY() + .5 - dy, pos.getZ() + .5 - dz, scale);
                    }
                }

                t.draw();

                GlStateManager.color(1.0f, 1.0f, 1.0f, 0.6f);
                builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX);
                for (PanelInfo info : panelInfo) {
                    if (info != null) {
                        doRender(info, pos.getX() + .5 - dx, pos.getY() + 1.5 - dy, pos.getZ() + .5 - dz, 0.5);
                    }
                }
                t.draw();

                GlStateManager.enableTexture2D();
                GlStateManager.depthMask(true);
                GlStateManager.enableLighting();
                mc.entityRenderer.enableLightmap();
            } else {
                toRemove.add(pos);
            }
        }
        for (BlockPos pos : toRemove) {
            forceFields.add(pos);
        }
    }

    private static void doRender(PanelInfo info, double x, double y, double z, double scale) {
        Tessellator t = Tessellator.getInstance();
        BufferBuilder builder = t.getBuffer();

        int index = info.getIndex();
        Triangle triangle = PentakisDodecahedron.getTriangle(index);

        Vec3d offs = triangle.getMid().scale(scale);
        x += offs.x;
        y += offs.y;
        z += offs.z;

        Vec3d a = triangle.getA().scale(scale).subtract(offs);
        Vec3d b = triangle.getB().scale(scale).subtract(offs);
        Vec3d c = triangle.getC().scale(scale).subtract(offs);

        builder.pos(x + a.x, y + a.y, z + a.z).tex(0, 0).endVertex();
        builder.pos(x + b.x, y + b.y, z + b.z).tex(1, 0).endVertex();
        builder.pos(x + c.x, y + c.y, z + c.z).tex(0, 1).endVertex();

        builder.pos(x + c.x, y + c.y, z + c.z).tex(0, 1).endVertex();
        builder.pos(x + b.x, y + b.y, z + b.z).tex(1, 0).endVertex();
        builder.pos(x + a.x, y + a.y, z + a.z).tex(0, 0).endVertex();
    }
}
