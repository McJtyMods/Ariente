package mcjty.ariente.entities.fluxship;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mcjty.ariente.Ariente;
import mcjty.ariente.client.ArienteRenderType;
import mcjty.ariente.display.client.LiveCameraRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static mcjty.ariente.blocks.generators.NegariteTankRenderer.NEGARITE_BEAM;
import static mcjty.ariente.blocks.generators.PosiriteTankRenderer.POSIRITE_BEAM;

/** Camera-facing power ribbons in the same rotating cabin frame as the hull. */
@EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public class FluxShipExhaustRender {

    private static final int STRANDS = 4;
    private static final int SEGMENTS = 24;
    private static final MultiBufferSource.BufferSource EXHAUST_BUFFERS = MultiBufferSource.immediate(new ByteBufferBuilder(32768));

    public static void render(FluxShipEntity ship, float partialTick, PoseStack pose) {
        render(ship, partialTick, pose, LiveCameraRenderer.buffersForView(EXHAUST_BUFFERS));
    }

    public static void render(FluxShipEntity ship, float partialTick, PoseStack pose, MultiBufferSource buffers) {
        Minecraft mc = Minecraft.getInstance();
        Entity viewer = mc.getCameraEntity();
        float strength = ship.getExhaustStrength(partialTick);
        if (strength < .001f || mc.options.getCameraType().isFirstPerson() && viewer != null && viewer.getVehicle() == ship) {
            return;
        }

        Vec3 cameraOffset = mc.gameRenderer.getMainCamera().getPosition()
                .subtract(ship.getPosition(partialTick)).subtract(0, .5, 0);
        Quaternionf inverseHull = new Quaternionf().rotationY(-ship.getFlightYaw(partialTick) * Mth.DEG_TO_RAD)
                .rotateX(ship.getFlightPitch(partialTick) * Mth.DEG_TO_RAD)
                .rotateZ(ship.getBank(partialTick) * Mth.DEG_TO_RAD).conjugate();
        Vec3 camera = new Vec3(inverseHull.transform(cameraOffset.toVector3f()));
        Vec3 cameraUp = new Vec3(inverseHull.transform(new Vector3f(mc.gameRenderer.getMainCamera().getUpVector())));
        double time = ship.level().getGameTime() + partialTick;
        double seed = (ship.getUUID().getLeastSignificantBits() & 65535) * Math.PI * 2 / 65536;
        var atlas = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
        TextureAtlasSprite negarite = atlas.apply(NEGARITE_BEAM);
        TextureAtlasSprite posirite = atlas.apply(POSIRITE_BEAM);
        VertexConsumer vertices = buffers.getBuffer(ArienteRenderType.SHIP_EXHAUST);
        for (int engine = 0; engine < 2; engine++) {
            // The model's open rear engine bay is at cabin Z = -2.5, around the hull's center height.
            double nozzle = engine == 0 ? -.32 : .32;
            TextureAtlasSprite sprite = engine == 0 ? negarite : posirite;
            for (int strand = 0; strand < STRANDS; strand++) {
                double phase = seed + strand * Math.PI * 2 / STRANDS + engine * .7;
                double length = (1 + 19 * strength) * (.87 + .13 * Math.sin(strand * 2.4 + engine));
                // Like the tanks, stretch a horizontal strip of the power texture along the beam.
                float v = sprite.getV((float) ((time * .025 + strand * .19 + engine * .37) % 1) * 16);
                renderStrand(pose, vertices, sprite, v, nozzle, length, phase, time, strength, camera, cameraUp);
            }
        }
    }

    @SubscribeEvent
    public static void renderStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            EXHAUST_BUFFERS.endBatch();
        }
    }

    private static void renderStrand(PoseStack pose, VertexConsumer vertices, TextureAtlasSprite sprite, float v,
                                     double nozzle, double length, double phase, double time, float strength,
                                     Vec3 camera, Vec3 cameraUp) {
        Vec3 previousLeft = Vec3.ZERO;
        Vec3 previousRight = Vec3.ZERO;
        Vec3 previousSide = Vec3.ZERO;
        float previousGlow = 0;
        for (int segment = 0; segment <= SEGMENTS; segment++) {
            double progress = segment / (double) SEGMENTS;
            Vec3 center = point(nozzle, length, phase, time, progress);
            Vec3 tangent = point(nozzle, length, phase, time, progress + .01)
                    .subtract(point(nozzle, length, phase, time, progress - .01));
            Vec3 side = camera.subtract(center).cross(tangent);
            if (side.lengthSqr() < 1.0E-8) {
                side = cameraUp.cross(tangent);
            }
            if (side.lengthSqr() < 1.0E-8) {
                side = new Vec3(1, 0, 0).cross(tangent);
            }
            side = side.normalize();
            if (side.dot(previousSide) < 0) {
                side = side.scale(-1);
            }
            // Keep the fine ribbons about a pixel wide on distant patrols, without changing their brightness.
            double distanceWidth = Math.min(.18, camera.distanceTo(center) * .0012);
            double width = Math.max(.035 + .055 * strength, distanceWidth) * (1 - progress)
                    * (1 + .12 * Math.sin(progress * 11 - time * .12 + phase));
            Vec3 left = center.add(side.scale(width));
            Vec3 right = center.subtract(side.scale(width));
            float glow = (float) (.8 * strength * Math.pow(1 - progress, .8)
                    * (.88 + .12 * Math.cos(time * .15 + phase - progress * 5)));
            if (segment > 0) {
                vertex(pose, vertices, previousLeft, sprite.getU0(), v, previousGlow);
                vertex(pose, vertices, previousRight, sprite.getU1(), v, previousGlow);
                vertex(pose, vertices, right, sprite.getU1(), v, glow);
                vertex(pose, vertices, left, sprite.getU0(), v, glow);
            }
            previousLeft = left;
            previousRight = right;
            previousSide = side;
            previousGlow = glow;
        }
    }

    private static Vec3 point(double nozzle, double length, double phase, double time, double progress) {
        double wave = phase - time * .16 + progress * 8;
        double radius = .065 + progress * .32;
        double drift = .1 * progress * progress * Math.sin(time * .055 + phase + progress * 3);
        return new Vec3(nozzle + Math.cos(wave) * radius + drift,
                -.03 + Math.sin(wave) * radius * .7, -2.54 - length * progress);
    }

    private static void vertex(PoseStack pose, VertexConsumer vertices, Vec3 point, float u, float v, float glow) {
        // ONE + ONE blending needs fading in RGB as well as alpha.
        vertices.addVertex(pose.last().pose(), (float) point.x, (float) point.y, (float) point.z)
                .setColor(glow, glow, glow, glow).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(), 0, 1, 0);
    }
}
