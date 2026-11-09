package mcjty.ariente.display.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.LinkedHashMap;
import java.util.Map;
import mcjty.ariente.blocks.utility.camera.CameraTile;
import mcjty.ariente.config.DisplayConfiguration;
import mcjty.ariente.display.CameraDisplayModule;
import mcjty.ariente.display.InstalledModule;
import mcjty.ariente.display.LiveCameraView;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.TextureFilteringMethod;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GlobalSettingsUniform;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix4f;

/** Uses the native world pipeline for terrain, entities, sky, weather and render-target routing. */
public final class LiveCameraRenderer {

    private static final int MAX_FEEDS = 4;
    private static final Map<Key, Feed> FEEDS = new LinkedHashMap<>(4, .75f, true);
    private static ClientLevel currentLevel;
    private static boolean rendering;
    private static long frame;
    private static int nextTexture;

    public static boolean isRendering() {
        return rendering;
    }

    public static MultiBufferSource buffersForView(MultiBufferSource buffers) {
        return buffers;
    }

    public static void beginFrame() {
        if (rendering || Minecraft.getInstance().level == null || Minecraft.getInstance().player == null) return;
        // Refresh after the complete player render, outside its frame graph
        // and block-entity iterators, just as on newer renderer versions.
        Map.Entry<Key, Feed> oldest = null;
        for (var entry : FEEDS.entrySet()) {
            if (entry.getValue().requestedFrame == frame
                    && (oldest == null || entry.getValue().lastUpdate < oldest.getValue().lastUpdate)) {
                oldest = entry;
            }
        }
        if (oldest != null) {
            Feed feed = oldest.getValue();
            long now = System.nanoTime();
            int imageWidth = DisplayConfiguration.LIVE_CAMERA_TEXTURE_WIDTH.get();
            int fps = DisplayConfiguration.LIVE_CAMERA_FPS.get();
            long interval = (1_000_000_000L + fps - 1) / fps;
            boolean resize = feed.target != null && feed.target.width != imageWidth;
            if (resize || now - feed.lastUpdate >= interval) {
                feed.update(oldest.getKey(), Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true), imageWidth);
                feed.lastUpdate = now;
            }
        }
        frame++;
    }

    public static void render(DisplayCanvas canvas, InstalledModule module) {
        if (rendering) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        CompoundTag snapshot = module.snapshot();
        if (!mc.level.dimension().identifier().toString().equals(snapshot.getStringOr("Dimension", ""))) {
            status(canvas, module, "Camera in another dimension");
            return;
        }
        BlockPos source = BlockPos.of(snapshot.getLongOr("Camera", 0));
        if (!mc.level.hasChunkAt(source) || !(mc.level.getBlockEntity(source) instanceof CameraTile camera)
                || mc.player.distanceToSqr(source.getCenter()) > 96 * 96 || mc.levelRenderer.viewArea == null) {
            status(canvas, module, "Move closer to the camera");
            return;
        }
        if (!camera.isLive()) {
            status(canvas, module, "Updating camera mode");
            return;
        }
        int chunkOffset = Math.max(Math.abs((source.getX() >> 4) - (mc.player.getBlockX() >> 4)),
                Math.abs((source.getZ() >> 4) - (mc.player.getBlockZ() >> 4)));
        int available = (mc.options.getEffectiveRenderDistance() - chunkOffset - 1) * 16;
        int radius = Math.min(64, Math.min(CameraDisplayModule.radius(module.stack()), available));
        if (radius < 8) {
            status(canvas, module, "Move closer to the camera");
            return;
        }
        int width = module.size().width() - 12, height = module.size().height() - 24;
        Key key = new Key(source, camera.getBlockState().getValue(BlockStateProperties.FACING),
                CameraDisplayModule.livePan(module.stack()), CameraDisplayModule.liveTilt(module.stack()),
                CameraDisplayModule.zoom(module.stack()), radius, width, height, CameraDisplayModule.rotationSpeed(module.stack()));
        Feed feed = FEEDS.get(key);
        if (feed == null) {
            if (FEEDS.size() == MAX_FEEDS) {
                Key oldest = FEEDS.keySet().iterator().next();
                feed = FEEDS.remove(oldest);
                feed.startedAt = mc.level.getGameTime();
                feed.lastUpdate = 0;
            } else {
                feed = new Feed(mc.level.getGameTime());
            }
            FEEDS.put(key, feed);
        }
        long now = System.nanoTime();
        feed.lastUsed = now;
        feed.requestedFrame = frame;
        if (feed.target == null) {
            status(canvas, module, "Starting live view");
            return;
        }
        Matrix4f pose = canvas.poses().last().pose();
        VertexConsumer vertices = canvas.buffer().getBuffer(feed.renderType);
        vertices.addVertex(pose, 6, 18 + height, .04f).setUv(0, 0).setColor(255, 255, 255, 255);
        vertices.addVertex(pose, 6 + width, 18 + height, .04f).setUv(1, 0).setColor(255, 255, 255, 255);
        vertices.addVertex(pose, 6 + width, 18, .04f).setUv(1, 1).setColor(255, 255, 255, 255);
        vertices.addVertex(pose, 6, 18, .04f).setUv(0, 1).setColor(255, 255, 255, 255);
    }

    private static void status(DisplayCanvas canvas, InstalledModule module, String status) {
        canvas.text(status, 6, 21, 0xffbc7a, .45f, module.size().width() - 12);
    }

    public static void tick() {
        ClientLevel level = Minecraft.getInstance().level;
        if (currentLevel != level) {
            clear();
            currentLevel = level;
        }
        long oldest = System.nanoTime() - 5_000_000_000L;
        FEEDS.values().removeIf(feed -> {
            if (feed.lastUsed < oldest) {
                feed.close();
                return true;
            }
            return false;
        });
    }

    public static void clear() {
        if (!RenderSystem.isOnRenderThread()) {
            Minecraft.getInstance().execute(LiveCameraRenderer::clear);
            return;
        }
        FEEDS.values().forEach(Feed::close);
        FEEDS.clear();
    }

    private record Key(BlockPos source, Direction facing, int pan, int tilt, int zoom, int radius,
                       int width, int height, int speed) {
    }

    private static final class Feed implements AutoCloseable {
        private RenderTarget target;
        private RenderType renderType;
        private Identifier texture;
        private ProjectionMatrixBuffer projection;
        private FogRenderer fog;
        private GlobalSettingsUniform globals;
        private double startedAt;
        private long lastUpdate;
        private long lastUsed = System.nanoTime();
        private long requestedFrame;

        private Feed(double startedAt) {
            this.startedAt = startedAt;
        }

        private void update(Key key, float partialTick, int imageWidth) {
            Minecraft mc = Minecraft.getInstance();
            int height = Math.max(1, Math.round((float) imageWidth * key.height() / key.width()));
            if (target == null) {
                target = new TextureTarget("Ariente live camera", imageWidth, height, true);
                texture = Identifier.fromNamespaceAndPath("ariente", "live_camera/" + nextTexture++);
                mc.getTextureManager().register(texture, new RenderTargetTexture(target));
                renderType = DisplayRenderType.cameraImage(texture);
                projection = new ProjectionMatrixBuffer("Ariente live camera");
                fog = new FogRenderer();
                globals = new GlobalSettingsUniform();
            } else if (target.width != imageWidth || target.height != height) target.resize(imageWidth, height);
            var renderer = mc.levelRenderer;
            Camera originalCamera = mc.gameRenderer.getMainCamera();
            RenderTarget originalTarget = mc.getMainRenderTarget();
            LevelRenderState originalState = renderer.levelRenderState;
            var oldProjection = RenderSystem.getProjectionMatrixBuffer();
            var oldProjectionType = RenderSystem.getProjectionType();
            var oldFog = RenderSystem.getShaderFog();
            var oldGlobals = RenderSystem.getGlobalSettingsUniform();
            var oldColor = RenderSystem.outputColorTextureOverride;
            var oldDepth = RenderSystem.outputDepthTextureOverride;
            RenderSystem.getModelViewStack().pushMatrix();
            RenderSystem.getModelViewStack().identity();
            rendering = true;
            Lens camera = new Lens();
            try {
                double pan = key.pan() + (mc.level.getGameTime() + partialTick - startedAt) * key.speed() / 20;
                LiveCameraView view = LiveCameraView.of(key.source(), key.facing(), pan, key.tilt());
                float fov = (float) Math.toDegrees(2 * Math.atan(Math.tan(Math.toRadians(35)) * 100 / key.zoom()));
                var state = new LevelRenderState();
                camera.configure(view, fov, imageWidth, height, key.radius(), state.cameraRenderState, partialTick);
                // Terrain subtracts the camera origin stored in the global UBO.
                // A separate buffer keeps this view independent of player movement.
                globals.update(imageWidth, height, mc.options.glintStrength().get(), mc.level.getGameTime(),
                        mc.getDeltaTracker(), 0, view.eye(), mc.options.textureFiltering().get() == TextureFilteringMethod.RGSS);
                mc.gameRenderer.mainCamera = camera;
                mc.mainRenderTarget = target;
                renderer.levelRenderState = state;
                camera.extractRenderState(state.cameraRenderState, partialTick);
                state.cameraRenderState.fogType = camera.getFluidInCamera();
                state.cameraRenderState.fogData = fog.setupFog(camera, Math.max(2, key.radius() / 16), mc.getDeltaTracker(), 0, mc.level);
                fog.updateBuffer(state.cameraRenderState.fogData);
                renderer.update(camera);
                renderer.extractLevel(mc.getDeltaTracker(), camera, partialTick);
                RenderSystem.setProjectionMatrix(projection.getBuffer(state.cameraRenderState.projectionMatrix), ProjectionType.PERSPECTIVE);
                renderer.renderLevel(GraphicsResourceAllocator.UNPOOLED, mc.getDeltaTracker(), false, state.cameraRenderState,
                        state.cameraRenderState.viewRotationMatrix, fog.getBuffer(FogRenderer.FogMode.WORLD),
                        state.cameraRenderState.fogData.color, true, state.chunkSectionsToRender);
            } finally {
                renderer.levelRenderState = originalState;
                mc.mainRenderTarget = originalTarget;
                mc.gameRenderer.mainCamera = originalCamera;
                renderer.update(originalCamera);
                mc.getEntityRenderDispatcher().prepare(originalCamera, mc.crosshairPickEntity);
                mc.getBlockEntityRenderDispatcher().prepare(originalCamera.position());
                RenderSystem.outputColorTextureOverride = oldColor;
                RenderSystem.outputDepthTextureOverride = oldDepth;
                RenderSystem.setProjectionMatrix(oldProjection, oldProjectionType);
                RenderSystem.setShaderFog(oldFog);
                RenderSystem.setGlobalSettingsUniform(oldGlobals);
                RenderSystem.getModelViewStack().popMatrix();
                rendering = false;
            }
        }

        @Override
        public void close() {
            if (texture != null) Minecraft.getInstance().getTextureManager().release(texture);
            if (target != null) target.destroyBuffers();
            if (projection != null) projection.close();
            if (fog != null) fog.close();
            if (globals != null) globals.close();
        }
    }

    private static final class Lens extends Camera {

        private void configure(LiveCameraView view, float fov, int width, int height, int radius,
                               CameraRenderState state, float partialTick) {
            Minecraft mc = Minecraft.getInstance();
            setLevel(mc.level);
            setEntity(mc.player);
            update(mc.getDeltaTracker());
            extractRenderState(state, partialTick);
            setPosition(view.eye());
            setRotation(view.yaw(), view.pitch());
            attributeProbe().tick(mc.level, view.eye());
            // The sky uses geometry beyond the terrain capture radius. Retain
            // Minecraft's projection distance and limit terrain with the frustum.
            setupPerspective(.05f, state.depthFar, fov, width, height);
            Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(fov), (float) width / height, .05f, radius + 16);
            prepareCullFrustum(getViewRotationMatrix(new Matrix4f()), projection, view.eye());
        }

        @Override
        public boolean isDetached() {
            // A mounted camera can see the local player's body in any player view mode.
            return true;
        }
    }
}
