package mcjty.ariente.blocks.defense;

import com.mojang.blaze3d.vertex.*;
import java.util.*;
import mcjty.ariente.client.ArienteRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import mcjty.ariente.Ariente;
import mcjty.ariente.items.armor.PersonalForcefield;
import mcjty.ariente.varia.Triangle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;

public class ForceFieldRenderer {

    public static final Identifier FORCEFIELD = Identifier.fromNamespaceAndPath(Ariente.MODID, "textures/effects/forcefield.png");
    private static final Identifier FORCEFIELD_HIT = Identifier.fromNamespaceAndPath(Ariente.MODID, "textures/effects/forcefield_hit.png");
    private static final float FIELD_ALPHA = 0.3f;
    private static final float PERSONAL_RECOVERY_FADE_TICKS = 3;
    private static final Set<BlockPos> forceFields = new HashSet<>();    // A set of force fields that are in render range
    private static Random random = new Random();

    public static final Set<LivingEntity> personalForcefields = new HashSet<>();

    public static void updatePersonalField(int entityId, long cooldownUntil, long lastHit) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && level.getEntity(entityId) instanceof LivingEntity entity) {
            PersonalForcefield.updateState(entity, cooldownUntil, lastHit);
        }
    }

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
    private static ClientLevel lastLevel;

    public static void renderForceFields(PoseStack poseStack, MultiBufferSource buffer, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel world = minecraft.level;
        if (world != lastLevel) {
            forceFields.clear();
            damageStats.clear();
            personalForcefields.clear();
            lastLevel = world;
        }
        if (world == null) {
            return;
        }
        Vec3 camera = minecraft.gameRenderer.getMainCamera().position();
        Matrix4f matrix = poseStack.last().pose();
            for (var candidate : world.entitiesForRendering()) {
                if (candidate instanceof LivingEntity living && PersonalForcefield.isActive(living)) {
                    personalForcefields.add(living);
                }
            }
            // The local player's armor model is not rendered in first person.
            if (minecraft.options.getCameraType().isFirstPerson() && minecraft.getCameraEntity() instanceof LivingEntity viewer
                    && PersonalForcefield.isActive(viewer)) {
                personalForcefields.add(viewer);
            }
            for (LivingEntity entity : personalForcefields) {
                if (entity.level() != world || !PersonalForcefield.isActive(entity)) {
                    continue;
                }
                Vec3 pos = entity.getPosition(partialTicks);
                boolean firstPerson = entity == minecraft.getCameraEntity() && minecraft.options.getCameraType().isFirstPerson();
                double time = world.getGameTime() + partialTicks;
                float remaining = (float) (PersonalForcefield.getCooldownUntil(entity) - time);
                boolean coolingDown = remaining > 0;
                float recoveryFade = Mth.clamp(remaining / PERSONAL_RECOVERY_FADE_TICKS, 0, 1);
                float flash = coolingDown ? Mth.clamp(1 - (float) (time - PersonalForcefield.getLastHit(entity)) / 8, 0, 1) : 0;
                float red = (firstPerson ? 0.75f + flash * 0.25f : 1) * recoveryFade;
                float alpha = firstPerson ? 0.09f + flash * 0.08f * recoveryFade
                        : 0.2f + (0.25f + flash * 0.3f) * recoveryFade;
                // This shader discards vertex alpha below 0.1; its color modulator is applied after that cutoff.
                VertexConsumer builder = buffer.getBuffer(ArienteRenderType.forceField(FORCEFIELD, VertexFormat.Mode.TRIANGLES));
                for (int i = 0; i < PentakisDodecahedron.MAX_TRIANGLES; i++) {
                    doRender(builder, matrix, new PanelInfo(i, pos.x, pos.y, pos.z), pos.x - camera.x,
                            pos.y + 1 - camera.y, pos.z - camera.z, 1.2, 1, 1 - red * 0.95f, 1 - red * 0.95f, alpha);
                }
            }
            personalForcefields.clear();

            Iterator<BlockPos> iterator = forceFields.iterator();
            while (iterator.hasNext()) {
                BlockPos pos = iterator.next();
                if (!(world.getBlockEntity(pos) instanceof ForceFieldTile forcefield)) {
                    iterator.remove();
                    damageStats.keySet().removeIf(key -> key.getLeft().equals(pos));
                    continue;
                }
                double x = pos.getX() + .5 - camera.x;
                double y = pos.getY() + .5 - camera.y;
                double z = pos.getZ() + .5 - camera.z;
                PanelInfo[] panels = forcefield.getPanelInfo();
                randomSeedCounter++;
                renderPanels(matrix, pos, buffer, x, y, z, forcefield.getScaleDouble(), panels);
                renderPanels(matrix, pos, buffer, x, y + 1, z, .5, panels);
                tickDamageEffects(matrix, buffer, pos, panels, .5 - camera.x, .5 - camera.y, .5 - camera.z);
            }
    }

    private static void renderPanels(Matrix4f matrix, BlockPos pos, MultiBufferSource buffer, double x, double y, double z, double scale, PanelInfo[] panelInfo) {
        random.setSeed(randomSeedCounter);
        random.nextFloat();
        random.nextFloat();

        VertexConsumer builder = buffer.getBuffer(ArienteRenderType.forceField(FORCEFIELD, VertexFormat.Mode.TRIANGLES));
        for (PanelInfo info : panelInfo) {
            if (info != null) {
                renderPanel(builder, matrix, pos, x, y, z, scale, info);
            }
        }
    }

    private static void tickDamageEffects(Matrix4f matrix, MultiBufferSource buffer, BlockPos pos, PanelInfo[] panelInfo, double x, double y, double z) {
        for (PanelInfo info : panelInfo) {
            if (info != null) {
                Pair<BlockPos, Integer> key = Pair.of(pos, info.getIndex());
                DamageInfo damage = damageStats.get(key);
                if (damage != null) {
                    Triangle triangle = PentakisDodecahedron.getTriangle(info.getIndex());

                    Vec3 t0 = triangle.getA();
                    Vec3 t1 = triangle.getB();
                    Vec3 t2 = triangle.getC();
                    // Calculate triangle normal
                    Vec3 e0 = t1.subtract(t0);
                    Vec3 n = e0.cross(t2.subtract(t0)).normalize();
                    e0 = e0.normalize();
                    Vec3 e1 = e0.cross(n).normalize();

                    VertexConsumer builder = buffer.getBuffer(ArienteRenderType.forceField(FORCEFIELD_HIT, VertexFormat.Mode.QUADS));

                    float a = damage.damage;
                    float sc = 2.0f;

                    e0 = e0.scale(sc);
                    e1 = e1.scale(sc);

                    Vec3 v0 = damage.intersection.subtract(e0).subtract(e1);
                    Vec3 v1 = damage.intersection.add(e0).subtract(e1);
                    Vec3 v2 = damage.intersection.add(e0).add(e1);
                    Vec3 v3 = damage.intersection.subtract(e0).add(e1);

                    builder.addVertex(matrix, (float) (x + v0.x), (float) (y + v0.y), (float) (z + v0.z)).setUv(0, 0).setColor(1, 1, 1, a);
                    builder.addVertex(matrix, (float) (x + v1.x), (float) (y + v1.y), (float) (z + v1.z)).setUv(1, 0).setColor(1, 1, 1, a);
                    builder.addVertex(matrix, (float) (x + v2.x), (float) (y + v2.y), (float) (z + v2.z)).setUv(1, 1).setColor(1, 1, 1, a);
                    builder.addVertex(matrix, (float) (x + v3.x), (float) (y + v3.y), (float) (z + v3.z)).setUv(0, 1).setColor(1, 1, 1, a);

                    builder.addVertex(matrix, (float) (x + v3.x), (float) (y + v3.y), (float) (z + v3.z)).setUv(0, 0).setColor(1, 1, 1, a);
                    builder.addVertex(matrix, (float) (x + v2.x), (float) (y + v2.y), (float) (z + v2.z)).setUv(1, 0).setColor(1, 1, 1, a);
                    builder.addVertex(matrix, (float) (x + v1.x), (float) (y + v1.y), (float) (z + v1.z)).setUv(1, 1).setColor(1, 1, 1, a);
                    builder.addVertex(matrix, (float) (x + v0.x), (float) (y + v0.y), (float) (z + v0.z)).setUv(0, 1).setColor(1, 1, 1, a);

                    long ticks = System.currentTimeMillis();
                    long prevTicks = damage.prevticks;
                    if (prevTicks == -1) {
                        prevTicks = ticks-1;
                    }
                    long dt = ticks - prevTicks;
                    damage.prevticks = ticks;
                    damage.damage -= dt / 100.0f;
                    if (damage.damage <= 0) {
                        damageStats.remove(key);
                    }
                }
            }
        }
    }

    private static void renderPanel(VertexConsumer builder, Matrix4f matrix, BlockPos pos, double x, double y, double z, double scale, PanelInfo info) {
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
                doRender(builder, matrix, info, x, y, z, scale, 1.0f, 0.0f, 0.0f, FIELD_ALPHA);
            } else if (damage != null) {
                // Damage animation
                float d = damage.damage;
                doRender(builder, matrix, info, x, y, z, scale, 1.0f, 1.0f, 1.0f, FIELD_ALPHA + d * (1.0f - FIELD_ALPHA));
            } else {
                // Normal render
                doRender(builder, matrix, info, x, y, z, scale, 1.0f, 1.0f, 1.0f, FIELD_ALPHA);
            }
        } else {
            // Building up render
            doRender(builder, matrix, info, x, y, z, scale, 1.0f, l, l, 0.1f + l * 0.1f);
        }
    }

    public static void doRender(MultiBufferSource buffer, PanelInfo info, double x, double y, double z, double scale,
                                float r, float g, float b, float a) {
        VertexConsumer builder = buffer.getBuffer(ArienteRenderType.forceField(FORCEFIELD, VertexFormat.Mode.TRIANGLES));
        doRender(builder, new Matrix4f(), info, x, y, z, scale, r, g, b, a);
    }

    private static void doRender(VertexConsumer builder, Matrix4f matrix, PanelInfo info, double x, double y, double z, double scale,
                                 float r, float g, float b, float a) {
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

        builder.addVertex(matrix, (float) (x + v0.x), (float) (y + v0.y), (float) (z + v0.z)).setUv(0, 0).setColor(r, g, b, a);
        builder.addVertex(matrix, (float) (x + v1.x), (float) (y + v1.y), (float) (z + v1.z)).setUv(1, 0).setColor(r, g, b, a);
        builder.addVertex(matrix, (float) (x + v2.x), (float) (y + v2.y), (float) (z + v2.z)).setUv(0, 1).setColor(r, g, b, a);

        builder.addVertex(matrix, (float) (x + v2.x), (float) (y + v2.y), (float) (z + v2.z)).setUv(0, 1).setColor(r, g, b, a);
        builder.addVertex(matrix, (float) (x + v1.x), (float) (y + v1.y), (float) (z + v1.z)).setUv(1, 0).setColor(r, g, b, a);
        builder.addVertex(matrix, (float) (x + v0.x), (float) (y + v0.y), (float) (z + v0.z)).setUv(0, 0).setColor(r, g, b, a);
    }

}
