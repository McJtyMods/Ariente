package mcjty.ariente.display.client;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mcjty.ariente.display.CameraDisplayModule;
import mcjty.ariente.display.CameraPerspective;
import mcjty.ariente.display.CameraView;
import mcjty.ariente.display.InstalledModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Snapshot geometry is projected into a managed target using the game's GPU pipelines. */
public final class CameraPerspectiveRenderer {

    private static final int MAX_SCENES = 16;
    private static final int RESOLUTION_SCALE = 3;
    private static final Map<UUID, Scene> SCENES = new LinkedHashMap<>(16, .75f, true);
    private static ClientLevel currentLevel;
    private static int nextTexture;

    public static void render(DisplayCanvas canvas, InstalledModule module) {
        Minecraft mc = Minecraft.getInstance();
        tick();
        Scene scene = SCENES.get(module.id());
        if (scene == null) {
            if (SCENES.size() >= MAX_SCENES) {
                UUID oldest = SCENES.keySet().iterator().next();
                SCENES.remove(oldest).close();
            }
            scene = new Scene(mc.level.getGameTime());
            SCENES.put(module.id(), scene);
        }
        scene.lastUsed = System.nanoTime();
        if (scene.snapshot != module.snapshot()) {
            if (scene.snapshot == null || !scene.snapshot.equals(module.snapshot())) {
                if (scene.geometry != null) {
                    scene.geometry.close();
                }
                scene.geometry = CameraDisplayRenderer.perspectiveGeometry(module);
                scene.yaw = Double.NaN;
            }
            scene.snapshot = module.snapshot();
        }
        int width = module.size().width() - 12, height = module.size().height() - 24;
        double now = mc.level.getGameTime() + (double) mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        double yaw = CameraDisplayModule.viewYaw(module.stack(), now - scene.startedAt);
        int tilt = CameraDisplayModule.tilt(module.stack()), zoom = CameraDisplayModule.zoom(module.stack());
        if (scene.target == null || scene.target.width != width * RESOLUTION_SCALE || scene.target.height != height * RESOLUTION_SCALE
                || scene.yaw != yaw || scene.tilt != tilt || scene.zoom != zoom) {
            scene.update(width, height, yaw, tilt, zoom);
        }
        Matrix4f pose = canvas.poses().last().pose();
        VertexConsumer vertices = canvas.buffer().getBuffer(scene.renderType);
        vertices.addVertex(pose, 6, 18 + height, .04f).setUv(0, 0).setColor(255, 255, 255, 255);
        vertices.addVertex(pose, 6 + width, 18 + height, .04f).setUv(1, 0).setColor(255, 255, 255, 255);
        vertices.addVertex(pose, 6 + width, 18, .04f).setUv(1, 1).setColor(255, 255, 255, 255);
        vertices.addVertex(pose, 6, 18, .04f).setUv(0, 1).setColor(255, 255, 255, 255);
    }

    public static void tick() {
        ClientLevel level = Minecraft.getInstance().level;
        if (currentLevel != level) {
            clear();
            currentLevel = level;
        }
        long oldest = System.nanoTime() - 5_000_000_000L;
        SCENES.values().removeIf(scene -> {
            if (scene.lastUsed < oldest) {
                scene.close();
                return true;
            }
            return false;
        });
    }

    public static void clear() {
        if (!RenderSystem.isOnRenderThread()) {
            Minecraft.getInstance().execute(CameraPerspectiveRenderer::clear);
            return;
        }
        SCENES.values().forEach(Scene::close);
        SCENES.clear();
    }

    record Quad(float[] vertices, int[] colors, boolean translucent) {

        private float depth(CameraPerspective camera) {
            float x = 0, y = 0, z = 0;
            for (int i = 0; i < 4; i++) {
                x += vertices[i * 5];
                y += vertices[i * 5 + 1];
                z += vertices[i * 5 + 2];
            }
            return camera.view().transformPosition(x / 4, y / 4, z / 4, new Vector3f()).z;
        }

        private void emit(VertexConsumer output, Matrix4f view) {
            for (int i = 0; i < 4; i++) {
                output.addVertex(view, vertices[i * 5], vertices[i * 5 + 1], vertices[i * 5 + 2])
                        .setUv(vertices[i * 5 + 3], vertices[i * 5 + 4]).setColor(0xff000000 | colors[i]);
            }
        }
    }

    static class Geometry implements AutoCloseable {
        private final List<Quad> quads;
        private final Vec3 center;
        private final float radius;

        Geometry(List<Quad> quads, Vec3 center, float radius) {
            this.quads = quads;
            this.center = center;
            this.radius = radius;
        }

        private void draw(CameraPerspective camera) {
            try (var memory = new ByteBufferBuilder(262144)) {
            var buffers = MultiBufferSource.immediate(memory);
            var solid = buffers.getBuffer(DisplayRenderType.scene(false));
            for (Quad quad : quads) if (!quad.translucent()) quad.emit(solid, camera.view());
            buffers.endBatch();
            List<Quad> translucent = new ArrayList<>();
            for (Quad quad : quads) if (quad.translucent()) translucent.add(quad);
            translucent.sort(Comparator.comparingDouble(quad -> quad.depth(camera)));
            if (!translucent.isEmpty()) {
                var output = buffers.getBuffer(DisplayRenderType.scene(true));
                for (Quad quad : translucent) quad.emit(output, camera.view());
                buffers.endBatch();
            }
            }
        }

        @Override
        public void close() {
        }
    }

    private static final class Scene implements AutoCloseable {
        private final double startedAt;
        private CompoundTag snapshot;
        private Geometry geometry;
        private RenderTarget target;
        private RenderType renderType;
        private Identifier texture;
        private ProjectionMatrixBuffer projection;
        private double yaw = Double.NaN;
        private int tilt;
        private int zoom;
        private long lastUsed = System.nanoTime();

        private Scene(double startedAt) {
            this.startedAt = startedAt;
        }

        private void update(int width, int height, double yaw, int tilt, int zoom) {
            if (target == null) {
                target = new TextureTarget("Ariente snapshot camera", width * RESOLUTION_SCALE, height * RESOLUTION_SCALE, true);
                texture = Identifier.fromNamespaceAndPath("ariente", "snapshot_camera/" + nextTexture++);
                Minecraft.getInstance().getTextureManager().register(texture, new RenderTargetTexture(target));
                renderType = DisplayRenderType.cameraImage(texture);
                projection = new ProjectionMatrixBuffer("Ariente snapshot camera");
            } else if (target.width != width * RESOLUTION_SCALE || target.height != height * RESOLUTION_SCALE) {
                target.resize(width * RESOLUTION_SCALE, height * RESOLUTION_SCALE);
            }
            var oldProjection = RenderSystem.getProjectionMatrixBuffer();
            var oldType = RenderSystem.getProjectionType();
            var oldColor = RenderSystem.outputColorTextureOverride;
            var oldDepth = RenderSystem.outputDepthTextureOverride;
            RenderSystem.getModelViewStack().pushMatrix();
            RenderSystem.getModelViewStack().identity();
            try {
                RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(target.getColorTexture(), 0, target.getDepthTexture(), 1);
                RenderSystem.outputColorTextureOverride = target.getColorTextureView();
                RenderSystem.outputDepthTextureOverride = target.getDepthTextureView();
                CameraPerspective camera = CameraPerspective.of(geometry.center, geometry.radius, CameraView.of(yaw, tilt), width, height, zoom);
                RenderSystem.setProjectionMatrix(projection.getBuffer(camera.projection()), ProjectionType.PERSPECTIVE);
                geometry.draw(camera);
                this.yaw = yaw;
                this.tilt = tilt;
                this.zoom = zoom;
            } finally {
                RenderSystem.outputColorTextureOverride = oldColor;
                RenderSystem.outputDepthTextureOverride = oldDepth;
                RenderSystem.setProjectionMatrix(oldProjection, oldType);
                RenderSystem.getModelViewStack().popMatrix();
            }
        }

        @Override
        public void close() {
            if (texture != null) Minecraft.getInstance().getTextureManager().release(texture);
            if (target != null) target.destroyBuffers();
            if (projection != null) projection.close();
        }
    }
}
