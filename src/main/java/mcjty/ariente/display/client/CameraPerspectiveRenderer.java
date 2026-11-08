package mcjty.ariente.display.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import mcjty.ariente.display.CameraDisplayModule;
import mcjty.ariente.display.CameraPerspective;
import mcjty.ariente.display.CameraView;
import mcjty.ariente.display.InstalledModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

/** GPU projection, texture interpolation and depth testing of the cached mesh; never renders the world again. */
public class CameraPerspectiveRenderer {

    private static final int MAX_SCENES = 16;
    private static final int RESOLUTION_SCALE = 3;
    private static final Map<UUID, Scene> SCENES = new LinkedHashMap<>(16, .75f, true);
    private static ClientLevel currentLevel;

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
        double now = mc.level.getGameTime() + (double) mc.getTimer().getGameTimeDeltaPartialTick(true);
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
            RenderSystem.recordRenderCall(CameraPerspectiveRenderer::clear);
            return;
        }
        SCENES.values().forEach(Scene::close);
        SCENES.clear();
    }

    static class Geometry implements AutoCloseable {

        private final VertexBuffer solid;
        private final VertexBuffer translucent;
        private final MeshData.SortState sorting;
        private final ByteBufferBuilder sortBuffer = new ByteBufferBuilder(256);
        private final Vec3 center;
        private final float radius;

        Geometry(VertexBuffer solid, VertexBuffer translucent, MeshData.SortState sorting, Vec3 center, float radius) {
            this.solid = solid;
            this.translucent = translucent;
            this.sorting = sorting;
            this.center = center;
            this.radius = radius;
        }

        private void draw(CameraPerspective camera) {
            if (solid != null) {
                RenderSystem.disableBlend();
                RenderSystem.depthMask(true);
                solid.bind();
                solid.drawWithShader(camera.view(), camera.projection(), GameRenderer.getPositionTexColorShader());
            }
            if (translucent != null) {
                Vec3 eye = camera.eye();
                translucent.bind();
                translucent.uploadIndexBuffer(sorting.buildSortedIndexBuffer(sortBuffer, VertexSorting.byDistance((float) eye.x, (float) eye.y, (float) eye.z)));
                RenderSystem.depthMask(false);
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                        GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                translucent.drawWithShader(camera.view(), camera.projection(), GameRenderer.getPositionTexColorShader());
            }
            VertexBuffer.unbind();
        }

        @Override
        public void close() {
            sortBuffer.close();
            if (solid != null) {
                solid.close();
            }
            if (translucent != null) {
                translucent.close();
            }
        }
    }

    private static class Scene implements AutoCloseable {

        private final double startedAt;
        private CompoundTag snapshot;
        private Geometry geometry;
        private RenderTarget target;
        private RenderType renderType;
        private double yaw = Double.NaN;
        private int tilt;
        private int zoom;
        private long lastUsed = System.nanoTime();

        private Scene(double startedAt) {
            this.startedAt = startedAt;
        }

        private void update(int width, int height, double yaw, int tilt, int zoom) {
            GlState state = GlState.capture();
            try {
                RenderSystem.depthMask(true);
                if (target == null) {
                    target = new TextureTarget(width * RESOLUTION_SCALE, height * RESOLUTION_SCALE, true, Minecraft.ON_OSX);
                    target.setClearColor(0, 0, 0, 0);
                    target.setFilterMode(GL11.GL_LINEAR);
                    renderType = DisplayRenderType.cameraImage(target::getColorTextureId);
                } else if (target.width != width * RESOLUTION_SCALE || target.height != height * RESOLUTION_SCALE) {
                    target.resize(width * RESOLUTION_SCALE, height * RESOLUTION_SCALE, Minecraft.ON_OSX);
                    target.setFilterMode(GL11.GL_LINEAR);
                }
                target.clear(Minecraft.ON_OSX);
                target.bindWrite(true);
                RenderSystem.enableDepthTest();
                RenderSystem.depthFunc(GL11.GL_LEQUAL);
                RenderSystem.disableCull();
                RenderSystem.setShaderColor(1, 1, 1, 1);
                RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
                CameraPerspective camera = CameraPerspective.of(geometry.center, geometry.radius, CameraView.of(yaw, tilt), width, height, zoom);
                geometry.draw(camera);
                this.yaw = yaw;
                this.tilt = tilt;
                this.zoom = zoom;
            } finally {
                Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
                state.restore();
            }
        }

        @Override
        public void close() {
            if (geometry != null) {
                geometry.close();
            }
            if (target != null) {
                target.destroyBuffers();
                Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
            }
        }
    }

    record GlState(boolean depth, boolean depthMask, boolean cull, boolean blend, int depthFunc,
                           int sourceRgb, int destRgb, int sourceAlpha, int destAlpha, int texture, float[] color) {

        static GlState capture() {
            return new GlState(GL11.glIsEnabled(GL11.GL_DEPTH_TEST), GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK),
                    GL11.glIsEnabled(GL11.GL_CULL_FACE), GL11.glIsEnabled(GL11.GL_BLEND), GL11.glGetInteger(GL11.GL_DEPTH_FUNC),
                    GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB), GL11.glGetInteger(GL14.GL_BLEND_DST_RGB),
                    GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA), GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA),
                    RenderSystem.getShaderTexture(0), RenderSystem.getShaderColor().clone());
        }

        void restore() {
            if (depth) {
                RenderSystem.enableDepthTest();
            } else {
                RenderSystem.disableDepthTest();
            }
            if (cull) {
                RenderSystem.enableCull();
            } else {
                RenderSystem.disableCull();
            }
            if (blend) {
                RenderSystem.enableBlend();
            } else {
                RenderSystem.disableBlend();
            }
            RenderSystem.depthMask(depthMask);
            RenderSystem.depthFunc(depthFunc);
            RenderSystem.blendFuncSeparate(sourceRgb, destRgb, sourceAlpha, destAlpha);
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);
        }
    }
}
