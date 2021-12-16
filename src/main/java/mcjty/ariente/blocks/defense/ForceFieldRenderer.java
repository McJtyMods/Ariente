package mcjty.ariente.blocks.defense;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import mcjty.ariente.Ariente;
import mcjty.ariente.items.armor.PowerSuitModel;
import mcjty.ariente.varia.Triangle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class ForceFieldRenderer {

    public static final ResourceLocation FORCEFIELD = new ResourceLocation(Ariente.MODID, "textures/effects/forcefield.png");
    private static final ResourceLocation FORCEFIELD_HIT = new ResourceLocation(Ariente.MODID, "textures/effects/forcefield_hit.png");
    private static final float FIELD_ALPHA = 0.3f;
    private static final Set<BlockPos> forceFields = new HashSet<>();    // A set of force fields that are in render range
    private static Random random = new Random();

    public static Map<Vec3, Boolean> personalForcefields = new HashMap<>();

    private static class DamageInfo {
        float damage;
        long prevticks;
        Vec3 intersection;

        public DamageInfo(float damage, long prevticks, Vec3 intersection) {
            this.damage = damage;
            this.prevticks = prevticks;
            this.intersection = intersection;
        }
    }

    private static Map<Pair<BlockPos, Integer>, DamageInfo> damageStats = new HashMap<>();

    public static void register(BlockPos pos) {
        forceFields.add(pos);
    }

    public static void unregister(BlockPos pos) {
        forceFields.remove(pos);
        Set<Pair<BlockPos, Integer>> toRemove = new HashSet<>();
        for (Pair<BlockPos, Integer> pair : damageStats.keySet()) {
            if (pos.equals(pair.getKey())) {
                toRemove.add(pair);
            }
        }
        for (Pair<BlockPos, Integer> pair : toRemove) {
            damageStats.remove(pair);
        }
    }

    public static void damageField(BlockPos pos, int index, Vec3 intersection) {
        Pair<BlockPos, Integer> key = Pair.of(pos, index);
        damageStats.put(key, new DamageInfo(1.0f, -1, intersection));
    }

    private static long randomSeedCounter = 0;

    public static void renderForceFields(float partialTicks) {
        for (Vec3 vec3d : personalForcefields.keySet()) {
            PowerSuitModel.renderForcefield(vec3d.x, vec3d.y+.6, vec3d.z, personalForcefields.get(vec3d) ? 1 : partialTicks);
        }
        personalForcefields.clear();


        Set<BlockPos> toRemove = new HashSet<>();
        ClientWorld world = Minecraft.getInstance().level;
        for (BlockPos pos : forceFields) {
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof ForceFieldTile) {
                Minecraft mc = Minecraft.getInstance();
                mc.gameRenderer.lightTexture().turnOffLightLayer();
                GlStateManager._enableTexture();
                GlStateManager._disableLighting();
                GlStateManager._enableBlend();
                GlStateManager._depthMask(false);
                GlStateManager._color4f(1.0f, 1.0f, 1.0f, FIELD_ALPHA);
                mc.getTextureManager().bind(FORCEFIELD);

                Tessellator t = Tessellator.getInstance();
                BufferBuilder builder = t.getBuilder();

                Entity renderViewEntity = Minecraft.getInstance().getCameraEntity();
                double dx = renderViewEntity.xOld + (renderViewEntity.getX() - renderViewEntity.xOld) * partialTicks;
                double dy = renderViewEntity.yOld + (renderViewEntity.getY() - renderViewEntity.yOld) * partialTicks;
                double dz = renderViewEntity.zOld + (renderViewEntity.getZ() - renderViewEntity.zOld) * partialTicks;
                double x = pos.getX() + .5 - dx;
                double y = pos.getY() + .5 - dy;
                double z = pos.getZ() + .5 - dz;

                ForceFieldTile forcefield = (ForceFieldTile) te;
                double scale = forcefield.getScaleDouble();
                PanelInfo[] panelInfo = forcefield.getPanelInfo();

                randomSeedCounter++;
                renderPanels(pos, t, builder, x, y, z, scale, panelInfo);
                GlStateManager._color4f(1.0f, 1.0f, 1.0f, 0.6f);
                renderPanels(pos, t, builder, x, y+1, z, .5, panelInfo);

                tickDamageEffects(pos, panelInfo, .5 - dx, .5 - dy, .5 - dz, scale);

                GlStateManager._enableTexture();
                GlStateManager._depthMask(true);
                GlStateManager._enableLighting();
//                mc.entityRenderer.enableLightmap();

            } else {
                toRemove.add(pos);
            }
        }
        for (BlockPos pos : toRemove) {
            forceFields.add(pos);
        }
    }

    private static void renderPanels(BlockPos pos, Tessellator t, BufferBuilder builder, double x, double y, double z, double scale, PanelInfo[] panelInfo) {
        random.setSeed(randomSeedCounter);
        random.nextFloat();
        random.nextFloat();

        builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (PanelInfo info : panelInfo) {
            if (info != null) {
                renderPanel(pos, x, y, z, scale, info);
            }
        }
        t.end();
    }

    private static void tickDamageEffects(BlockPos pos, PanelInfo[] panelInfo, double x, double y, double z, double scale) {
        Minecraft.getInstance().getTextureManager().bind(FORCEFIELD_HIT);
        for (PanelInfo info : panelInfo) {
            if (info != null) {
                Pair<BlockPos, Integer> key = Pair.of(pos, info.getIndex());
                DamageInfo damage = damageStats.get(key);
                if (damage != null) {
                    Triangle triangle = PentakisDodecahedron.getTriangle(info.getIndex());

                    Vec3 offs = triangle.getMid().scale(scale);

                    // @todo optimize/cache?
                    Vec3 t0 = triangle.getA().scale(scale).subtract(offs);
                    Vec3 t1 = triangle.getB().scale(scale).subtract(offs);
                    Vec3 t2 = triangle.getC().scale(scale).subtract(offs);
                    // Calculate triangle normal
                    Vec3 e0 = t1.subtract(t0);
                    Vec3 n = e0.cross(t2.subtract(t0)).normalize();
                    e0 = e0.normalize();
                    Vec3 e1 = e0.cross(n).normalize();

                    Tessellator t = Tessellator.getInstance();
                    BufferBuilder builder = t.getBuilder();
                    builder.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

                    float a = damage.damage;
                    float sc = 2.0f;

                    e0 = e0.scale(sc);
                    e1 = e1.scale(sc);

                    Vec3 v0 = damage.intersection.subtract(e0).subtract(e1);
                    Vec3 v1 = damage.intersection.add(e0).subtract(e1);
                    Vec3 v2 = damage.intersection.add(e0).add(e1);
                    Vec3 v3 = damage.intersection.subtract(e0).add(e1);

                    builder.vertex(x + v0.x, y + v0.y, z + v0.z).uv(0, 0).color(1, 1, 1, a).endVertex();
                    builder.vertex(x + v1.x, y + v1.y, z + v1.z).uv(1, 0).color(1, 1, 1, a).endVertex();
                    builder.vertex(x + v2.x, y + v2.y, z + v2.z).uv(1, 1).color(1, 1, 1, a).endVertex();
                    builder.vertex(x + v3.x, y + v3.y, z + v3.z).uv(0, 1).color(1, 1, 1, a).endVertex();

                    builder.vertex(x + v3.x, y + v3.y, z + v3.z).uv(0, 0).color(1, 1, 1, a).endVertex();
                    builder.vertex(x + v2.x, y + v2.y, z + v2.z).uv(1, 0).color(1, 1, 1, a).endVertex();
                    builder.vertex(x + v1.x, y + v1.y, z + v1.z).uv(1, 1).color(1, 1, 1, a).endVertex();
                    builder.vertex(x + v0.x, y + v0.y, z + v0.z).uv(0, 1).color(1, 1, 1, a).endVertex();
                    t.end();


                    long ticks = System.currentTimeMillis();
                    long prevTicks = damage.prevticks;
                    if (prevTicks == -1) {
                        prevTicks = ticks-1;
                    }
                    long dt = ticks - prevTicks;
                    damage.damage -= dt / 100.0f;
                    if (damage.damage <= 0) {
                        damageStats.remove(key);
                    }
                }
            }
        }
    }

    private static void renderPanel(BlockPos pos, double x, double y, double z, double scale, PanelInfo info) {
        int life = info.getLife();
        float l = 1.0f - Math.min(1.0f, - life / 50.0f);
        if (life >= 0 || random.nextFloat() < (l / 20.0f)) {
            Pair<BlockPos, Integer> key = Pair.of(pos, info.getIndex());
            DamageInfo damage = damageStats.get(key);
            float lf = info.getLifePercentage();
            // Damage indicator flickers more on the preview
            float damageIndicatorScale = scale < .6 ? 1.0f : 10.0f;
            if (life >= 0 && lf < .5f && random.nextFloat() < .02f + (.5f-lf) / damageIndicatorScale) {
                // Random red flash if life is getting low
                doRender(info, x, y, z, scale, 1.0f, 0.0f, 0.0f, FIELD_ALPHA);
            } else if (damage != null) {
                // Damage animation
                float d = damage.damage;
                doRender(info, x, y, z, scale, 1.0f, 1.0f, 1.0f, FIELD_ALPHA + d * (1.0f - FIELD_ALPHA));
            } else {
                // Normal render
                doRender(info, x, y, z, scale, 1.0f, 1.0f, 1.0f, FIELD_ALPHA);
            }
        } else {
            // Building up render
            doRender(info, x, y, z, scale, 1.0f, l, l, 0.1f + l * 0.1f);
        }
    }

    public static void doRender(PanelInfo info, double x, double y, double z, double scale,
                                 float r, float g, float b, float a) {
        Tessellator t = Tessellator.getInstance();
        BufferBuilder builder = t.getBuilder();

        int index = info.getIndex();
        Triangle triangle = PentakisDodecahedron.getTriangle(index);

        Vec3 offs = triangle.getMid().scale(scale);
        x += offs.x;
        y += offs.y;
        z += offs.z;

        // @todo optimize?
        Vec3 v0 = triangle.getA().scale(scale).subtract(offs);
        Vec3 v1 = triangle.getB().scale(scale).subtract(offs);
        Vec3 v2 = triangle.getC().scale(scale).subtract(offs);

        builder.vertex(x + v0.x, y + v0.y, z + v0.z).uv(0, 0).color(r, g, b, a).endVertex();
        builder.vertex(x + v1.x, y + v1.y, z + v1.z).uv(1, 0).color(r, g, b, a).endVertex();
        builder.vertex(x + v2.x, y + v2.y, z + v2.z).uv(0, 1).color(r, g, b, a).endVertex();

        builder.vertex(x + v2.x, y + v2.y, z + v2.z).uv(0, 1).color(r, g, b, a).endVertex();
        builder.vertex(x + v1.x, y + v1.y, z + v1.z).uv(1, 0).color(r, g, b, a).endVertex();
        builder.vertex(x + v0.x, y + v0.y, z + v0.z).uv(0, 0).color(r, g, b, a).endVertex();
    }



}
