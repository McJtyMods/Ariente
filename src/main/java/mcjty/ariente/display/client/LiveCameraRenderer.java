package mcjty.ariente.display.client;

import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Comparator;
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
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.Mth;
import mcjty.lib.client.RenderBufferSource;
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

    public static RenderBufferSource buffersForView(RenderBufferSource buffers) {
        return buffers;
    }

    public static void beginFrame() {
        if (rendering || Minecraft.getInstance().level == null || Minecraft.getInstance().player == null) return;
        // LevelRenderer's return hook runs after every pass and PreparedFrame.close().
        // Refreshing from END_MAIN would re-enter the still-active player frame.
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
                || mc.player.distanceToSqr(Vec3.atCenterOf(source)) > 96 * 96 || mc.levelRenderer.viewArea == null) {
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
                target = new TextureTarget("Ariente live camera", imageWidth, height, GpuFormat.RGBA8_UNORM, GpuFormat.D32_FLOAT);
                texture = Identifier.fromNamespaceAndPath("ariente", "live_camera/" + nextTexture++);
                mc.getTextureManager().register(texture, new RenderTargetTexture(target));
                renderType = DisplayRenderType.cameraImage(texture);
                projection = new ProjectionMatrixBuffer("Ariente live camera");
                var previousFog = RenderSystem.getShaderFog();
                fog = new FogRenderer();
                RenderSystem.setShaderFog(previousFog);
                globals = new GlobalSettingsUniform();
            } else if (target.width != imageWidth || target.height != height) target.resize(imageWidth, height);
            var renderer = mc.levelRenderer;
            Camera originalCamera = mc.gameRenderer.mainCamera();
            RenderTarget originalTarget = mc.gameRenderer.mainRenderTarget();
            LevelRenderState originalState = renderer.levelRenderState;
            var originalSections = new ArrayList<>(renderer.visibleSections());
            var originalNearbySections = new ArrayList<>(renderer.nearbyVisibleSections());
            var originalSky = renderer.skyRenderer();
            RenderTarget originalSkyTarget = originalSky == null ? null : originalSky.renderTarget;
            var oldProjection = RenderSystem.getProjectionMatrixBuffer();
            var oldProjectionType = RenderSystem.getProjectionType();
            var oldFog = RenderSystem.getShaderFog();
            var oldGlobals = RenderSystem.getGlobalSettingsUniform();
            RenderSystem.getModelViewStack().pushMatrix();
            RenderSystem.getModelViewStack().identity();
            rendering = true;
            Lens camera = new Lens();
            try {
                double pan = key.pan() + (mc.level.getGameTime() + partialTick - startedAt) * key.speed() / 20;
                LiveCameraView view = LiveCameraView.of(key.source(), key.facing(), pan, key.tilt());
                float fov = (float) Math.toDegrees(2 * Math.atan(Math.tan(Math.toRadians(35)) * 100 / key.zoom()));
                var state = new LevelRenderState();
                camera.configure(view, fov, imageWidth, height, key.radius(), state.cameraRenderState, partialTick, false);
                // Extraction needs a frozen culling view to leave player caches
                // untouched. Keep the active render camera uncaptured: vanilla
                // otherwise draws the captured frustum as coloured debug planes.
                Lens extractionCamera = new Lens();
                extractionCamera.configure(view, fov, imageWidth, height, key.radius(), new CameraRenderState(), partialTick, true);
                // Terrain subtracts the camera origin stored in the global UBO.
                // A separate buffer keeps this view independent of player movement.
                globals.update(imageWidth, height, mc.options.glintStrength().get(), mc.level.getGameTime(),
                        partialTick, 0, view.eye(), mc.options.textureFiltering().get() == TextureFilteringMethod.RGSS);
                mc.gameRenderer.mainCamera = camera;
                mc.gameRenderer.mainRenderTarget = target;
                // The sky renderer retains its target instead of looking up
                // GameRenderer's current target for each sky pass.
                if (originalSky != null) {
                    originalSky.renderTarget = target;
                }
                renderer.levelRenderState = state;
                mc.levelExtractor.levelRenderState = state;
                camera.extractRenderState(state.cameraRenderState, mc.getDeltaTracker());
                state.cameraRenderState.fogType = camera.getFluidInCamera();
                var cameraFog = fog.setupFog(camera, Math.max(2, (key.radius() + 15) / 16), mc.getDeltaTracker(), 0, mc.level);
                // Native fog accepts whole chunks. Finish its fade at the actual
                // block radius, including the eight/sixteen-block camera ranges.
                cameraFog.renderDistanceEnd = Math.min(cameraFog.renderDistanceEnd, key.radius());
                cameraFog.renderDistanceStart = Math.min(cameraFog.renderDistanceStart,
                        key.radius() - Mth.clamp(key.radius() / 10f, 4, 64));
                state.cameraRenderState.fogData = cameraFog;
                fog.updateBuffer(cameraFog);
                selectCameraSections(renderer, camera, key.radius());
                mc.levelExtractor.extract(mc.getDeltaTracker(), extractionCamera, partialTick);
                RenderSystem.setProjectionMatrix(projection.getBuffer(state.cameraRenderState.projectionMatrix), ProjectionType.PERSPECTIVE);
                // The level state supplies the lens geometry, sky and entity poses.
                // Keep native terrain maintenance anchored to the player's storage;
                // moving it to each lens resets edge chunks and races the player's
                // asynchronous occlusion graph on every camera refresh.
                var terrainCamera = new CameraRenderState();
                extractionCamera.extractRenderState(terrainCamera, mc.getDeltaTracker());
                terrainCamera.fogType = state.cameraRenderState.fogType;
                terrainCamera.fogData = cameraFog;
                terrainCamera.pos = originalCamera.position();
                terrainCamera.blockPos = originalCamera.blockPosition();
                renderer.render(GraphicsResourceAllocator.UNPOOLED, false, terrainCamera,
                        fog.getBuffer(FogRenderer.FogMode.WORLD), state.cameraRenderState.fogData.color, true, false);
            } finally {
                renderer.levelRenderState = originalState;
                renderer.clearVisibleSections();
                renderer.visibleSections().addAll(originalSections);
                renderer.nearbyVisibleSections().addAll(originalNearbySections);
                mc.levelExtractor.levelRenderState = originalState;
                mc.gameRenderer.mainRenderTarget = originalTarget;
                var sky = renderer.skyRenderer();
                if (sky != null) {
                    sky.renderTarget = sky == originalSky ? originalSkyTarget : originalTarget;
                }
                mc.gameRenderer.mainCamera = originalCamera;
                mc.getEntityRenderDispatcher().prepare(originalCamera, mc.crosshairPickEntity);
                mc.getBlockEntityRenderDispatcher().prepare(originalCamera.position());
                RenderSystem.setProjectionMatrix(oldProjection, oldProjectionType);
                RenderSystem.setShaderFog(oldFog);
                RenderSystem.setGlobalSettingsUniform(oldGlobals);
                RenderSystem.getModelViewStack().popMatrix();
                fog.endFrame();
                rendering = false;
            }
        }

        private static void selectCameraSections(LevelRenderer renderer, Lens camera, int radius) {
            // The player's occlusion graph may hide sections which the mounted
            // lens can see. Read compiled sections from the loaded view area
            // directly, without changing that graph or its cached frustum.
            renderer.clearVisibleSections();
            BlockPos origin = camera.blockPosition();
            int margin = radius + 16;
            int minY = Math.max(renderer.viewArea().minSectionY(), (origin.getY() - margin) >> 4);
            int maxY = Math.min(renderer.viewArea().maxSectionY(), (origin.getY() + margin) >> 4);
            for (int x = (origin.getX() - margin) >> 4; x <= (origin.getX() + margin) >> 4; x++) {
                for (int z = (origin.getZ() - margin) >> 4; z <= (origin.getZ() + margin) >> 4; z++) {
                    for (int y = minY; y <= maxY; y++) {
                        var section = renderer.viewArea().getRenderSectionAt(new BlockPos(x << 4, y << 4, z << 4));
                        if (section != null && camera.getCullFrustum().isVisible(section.getBoundingBox())) {
                            renderer.visibleSections().add(section);
                            renderer.nearbyVisibleSections().add(section);
                        }
                    }
                }
            }
            renderer.visibleSections().sort(Comparator.comparingDouble(section ->
                    section.getBoundingBox().getCenter().distanceToSqr(camera.position())));
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
                               CameraRenderState state, float partialTick, boolean capture) {
            Minecraft mc = Minecraft.getInstance();
            setLevel(mc.level);
            setEntity(mc.player);
            update(mc.getDeltaTracker());
            extractRenderState(state, Minecraft.getInstance().getDeltaTracker());
            setPosition(view.eye());
            setRotation(view.yaw(), view.pitch());
            attributeProbe().tick(mc.level, view.eye());
            // The sky uses geometry beyond the terrain capture radius. Retain
            // Minecraft's projection distance and limit terrain with the frustum.
            setupPerspective(.05f, state.depthFar, fov, width, height);
            Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(fov), (float) width / height, .05f, radius + 16);
            // A captured lens frustum prevents extraction/rendering from updating
            // the player's cached visibility or scheduling a different graph.
            if (capture) {
                captureFrustum();
            }
            prepareCullFrustum(getViewRotationMatrix(new Matrix4f()), projection, view.eye());
        }

        @Override
        public boolean isDetached() {
            // A mounted camera can see the local player's body in any player view mode.
            return true;
        }
    }
}
