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

    private static final ResourceLocation FORCEFIELD = new ResourceLocation(Ariente.MODID, "textures/entity/forcefield.png");

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
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        for (BlockPos pos : forceFields) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof ForceFieldTile) {
                ForceFieldTile forcefield = (ForceFieldTile) te;
                int[] entityIds = forcefield.getEntityIds();
                for (int id : entityIds) {
                    if (id != -1) {
                        Entity entity = world.getEntityByID(id);
                        if (entity != null) {
                            // @todo optimize this into a single batch
                            Entity renderViewEntity = Minecraft.getMinecraft().renderViewEntity;
                            double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
                            double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks;
                            double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;
                            float f = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
                            double d3 = renderViewEntity.lastTickPosX + (renderViewEntity.posX - renderViewEntity.lastTickPosX) * partialTicks;
                            double d4 = renderViewEntity.lastTickPosY + (renderViewEntity.posY - renderViewEntity.lastTickPosY) * partialTicks;
                            double d5 = renderViewEntity.lastTickPosZ + (renderViewEntity.posZ - renderViewEntity.lastTickPosZ) * partialTicks;
                            doRender((ForceFieldPanelEntity)entity, d0 - d3, d1 - d4, d2 - d5, f, partialTicks);
                        }
                    }
                }
            } else {
                toRemove.add(pos);
            }
        }
        for (BlockPos pos : toRemove) {
            forceFields.add(pos);
        }
    }

    private static void doRender(ForceFieldPanelEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        Tessellator t = Tessellator.getInstance();
        BufferBuilder builder = t.getBuffer();

        Minecraft.getMinecraft().entityRenderer.disableLightmap();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 0.2f);

        Minecraft mc = Minecraft.getMinecraft();

        mc.renderEngine.bindTexture(FORCEFIELD);

        builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX);

        int index = entity.getIndex();
        Triangle triangle = PentakisDodecahedron.getTriangle(index);

        float scale = entity.getScale();
        Vec3d offs = triangle.getMid().scale(scale);
//        GlStateManager.translate(offs.x, offs.y, offs.z);

        Vec3d a = triangle.getA().scale(scale).subtract(offs);
        Vec3d b = triangle.getB().scale(scale).subtract(offs);
        Vec3d c = triangle.getC().scale(scale).subtract(offs);

        builder.pos(a.x, a.y, a.z).tex(0, 0).endVertex();
        builder.pos(b.x, b.y, b.z).tex(1, 0).endVertex();
        builder.pos(c.x, c.y, c.z).tex(0, 1).endVertex();

        builder.pos(c.x, c.y, c.z).tex(0, 1).endVertex();
        builder.pos(b.x, b.y, b.z).tex(1, 0).endVertex();
        builder.pos(a.x, a.y, a.z).tex(0, 0).endVertex();

        t.draw();


        GlStateManager.popMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        Minecraft.getMinecraft().entityRenderer.enableLightmap();
    }
}
