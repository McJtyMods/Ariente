package mcjty.ariente.display.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import mcjty.ariente.blocks.utility.camera.CameraTile;
import mcjty.ariente.config.DisplayConfiguration;
import mcjty.ariente.display.CameraDisplayModule;
import mcjty.ariente.display.InstalledModule;
import mcjty.ariente.display.LiveCameraView;
import mcjty.hologui.gui.HoloGuiEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Nearby-world prototype: reuses received chunks and actual entity renderers without moving the player. */
public class LiveCameraRenderer {

    private static final int MAX_FEEDS = 4;
    private static final Map<Key, Feed> FEEDS = new LinkedHashMap<>(4, .75f, true);
    private static final Map<RenderType, RenderType> TYPES = new IdentityHashMap<>();
    private static final Camera LENS = new Lens();
    private static final TargetBuffers BUFFERS = new TargetBuffers();
    private static ClientLevel currentLevel;
    private static RenderTarget activeTarget;
    private static boolean rendering;
    private static int updatesThisFrame;
    private static long frame;

    public static boolean isRendering() {
        return rendering;
    }

    /** Deferred world effects must finish in the camera pass instead of entering the next player frame. */
    public static MultiBufferSource buffersForView(MultiBufferSource worldBuffers) {
        return rendering ? BUFFERS : worldBuffers;
    }

    public static void beginFrame() {
        frame++;
        updatesThisFrame = 0;
    }

    public static void render(DisplayCanvas canvas, InstalledModule module) {
        if (rendering || !visible(canvas, module)) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        CompoundTag snapshot = module.snapshot();
        if (!mc.level.dimension().location().toString().equals(snapshot.getString("Dimension"))) {
            status(canvas, module, "Camera in another dimension");
            return;
        }
        BlockPos source = BlockPos.of(snapshot.getLong("Camera"));
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
        String unavailable = LiveCameraRenderCompat.unavailable();
        if (unavailable != null) {
            status(canvas, module, unavailable);
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
        int imageWidth = DisplayConfiguration.LIVE_CAMERA_TEXTURE_WIDTH.get();
        int fps = DisplayConfiguration.LIVE_CAMERA_FPS.get();
        long interval = (1_000_000_000L + fps - 1) / fps;
        boolean resize = feed.target != null && feed.target.width != imageWidth;
        if ((resize || now - feed.lastUpdate >= interval) && updatesThisFrame < 1 && next(feed)) {
            updatesThisFrame++;
            feed.update(key, mc.getFrameTime(), imageWidth);
            feed.lastUpdate = now;
        }
        if (feed.target == null) {
            status(canvas, module, "Starting live view");
            return;
        }
        Matrix4f pose = canvas.poses().last().pose();
        VertexConsumer vertices = canvas.buffer().getBuffer(feed.renderType);
        vertices.vertex(pose, 6, 18 + height, .04f).uv(0, 0).color(255, 255, 255, 255).endVertex();
        vertices.vertex(pose, 6 + width, 18 + height, .04f).uv(1, 0).color(255, 255, 255, 255).endVertex();
        vertices.vertex(pose, 6 + width, 18, .04f).uv(1, 1).color(255, 255, 255, 255).endVertex();
        vertices.vertex(pose, 6, 18, .04f).uv(0, 1).color(255, 255, 255, 255).endVertex();
    }

    private static boolean next(Feed feed) {
        // Prefer the oldest visible feed, so a slow main render cannot starve later modules.
        for (Feed other : FEEDS.values()) {
            if (other.requestedFrame >= frame - 1 && other.lastUpdate < feed.lastUpdate) {
                return false;
            }
        }
        return true;
    }

    private static void status(DisplayCanvas canvas, InstalledModule module, String status) {
        canvas.text(status, 6, 21, 0xffbc7a, .45f, module.size().width() - 12);
    }

    private static boolean visible(DisplayCanvas canvas, InstalledModule module) {
        Matrix4f clip = new Matrix4f(RenderSystem.getProjectionMatrix()).mul(canvas.poses().last().pose());
        int intersection = 63;
        for (int corner = 0; corner < 4; corner++) {
            Vector4f p = clip.transform(new Vector4f((corner & 1) == 0 ? 6 : module.size().width() - 6,
                    (corner & 2) == 0 ? 18 : module.size().height() - 6, .04f, 1));
            int code = (p.x < -p.w ? 1 : 0) | (p.x > p.w ? 2 : 0) | (p.y < -p.w ? 4 : 0)
                    | (p.y > p.w ? 8 : 0) | (p.z < -p.w ? 16 : 0) | (p.z > p.w ? 32 : 0);
            intersection &= code;
        }
        return intersection == 0;
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
            RenderSystem.recordRenderCall(LiveCameraRenderer::clear);
            return;
        }
        FEEDS.values().forEach(Feed::close);
        FEEDS.clear();
    }

    private record Key(BlockPos source, Direction facing, int pan, int tilt, int zoom, int radius,
                       int width, int height, int speed) {
    }

    private static class Feed implements AutoCloseable {

        private RenderTarget target;
        private RenderType renderType;
        private double startedAt;
        private long lastUpdate;
        private long lastUsed = System.nanoTime();
        private long requestedFrame;

        private Feed(double startedAt) {
            this.startedAt = startedAt;
        }

        private void update(Key key, float partialTick, int imageWidth) {
            Minecraft mc = Minecraft.getInstance();
            try (LiveCameraRenderCompat.Scope compatibility = LiveCameraRenderCompat.open(); State state = new State()) {
                rendering = true;
                int height = Math.round((float) imageWidth * key.height() / key.width());
                if (target == null) {
                    target = new TextureTarget(imageWidth, height, true, Minecraft.ON_OSX);
                    target.setFilterMode(GL11.GL_LINEAR);
                    renderType = DisplayRenderType.cameraImage(target::getColorTextureId);
                } else if (target.width != imageWidth || target.height != height) {
                    target.resize(imageWidth, height, Minecraft.ON_OSX);
                    target.setFilterMode(GL11.GL_LINEAR);
                }
                activeTarget = target;
                RenderSystem.depthMask(true);
                target.clear(Minecraft.ON_OSX);
                target.bindWrite(true);
                double pan = key.pan() + (mc.level.getGameTime() + partialTick - startedAt) * key.speed() / 20;
                LiveCameraView view = LiveCameraView.of(key.source(), key.facing(), pan, key.tilt());
                ((Lens) LENS).configure(view, partialTick);
                mc.gameRenderer.mainCamera = LENS;
                float fov = (float) (2 * Math.atan(Math.tan(Math.toRadians(35)) * 100 / key.zoom()));
                Matrix4f projection = new Matrix4f().perspective(fov, (float) imageWidth / height, .05f, key.radius() + 16);
                PoseStack poses = new PoseStack();
                poses.mulPoseMatrix(view.rotation());
                RenderSystem.setProjectionMatrix(projection, VertexSorting.DISTANCE_TO_ORIGIN);
                RenderSystem.setInverseViewRotationMatrix(new Matrix3f(poses.last().normal()).invert());
                RenderSystem.setShaderColor(1, 1, 1, 1);
                RenderSystem.enableDepthTest();
                RenderSystem.enableCull();
                FogRenderer.setupColor(LENS, partialTick, mc.level, Math.max(2, key.radius() / 16), mc.gameRenderer.getDarkenWorldAmount(partialTick));
                FogRenderer.levelFogColor();
                RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
                FogRenderer.setupFog(LENS, FogRenderer.FogMode.FOG_SKY, key.radius(), false, partialTick);
                RenderSystem.setShader(GameRenderer::getPositionShader);
                mc.levelRenderer.renderSky(poses, projection, partialTick, LENS, false,
                        () -> FogRenderer.setupFog(LENS, FogRenderer.FogMode.FOG_SKY, key.radius(), false, partialTick));
                FogRenderer.setupFog(LENS, FogRenderer.FogMode.FOG_TERRAIN, key.radius(), false, partialTick);
                Frustum frustum = new Frustum(poses.last().pose(), projection);
                Vec3 eye = view.eye();
                frustum.prepare(eye.x, eye.y, eye.z);
                try (LiveCameraRenderCompat.Terrain nativeTerrain = LiveCameraRenderCompat.terrain(frustum, eye, key.radius())) {
                    List<ChunkRenderDispatcher.RenderChunk> chunks = nativeTerrain == null ? chunks(frustum, eye, key.radius()) : List.of();
                    for (RenderType type : new RenderType[]{RenderType.solid(), RenderType.cutoutMipped(), RenderType.cutout()}) {
                        terrain(chunks, nativeTerrain, type, poses, projection, eye);
                    }
                    if (mc.level.effects().constantAmbientLight()) {
                        Lighting.setupNetherLevel(poses.last().pose());
                    } else {
                        Lighting.setupLevel(poses.last().pose());
                    }
                    List<BlockEntity> tiles = nativeTerrain == null
                            ? chunks.stream().flatMap(chunk -> chunk.getCompiledChunk().getRenderableBlockEntities().stream()).toList()
                            : nativeTerrain.tiles();
                    entities(tiles, poses, frustum, eye, partialTick, key.radius());
                    terrain(chunks, nativeTerrain, RenderType.translucent(), poses, projection, eye);
                    terrain(chunks, nativeTerrain, RenderType.tripwire(), poses, projection, eye);
                }
                mc.particleEngine.render(poses, BUFFERS, mc.gameRenderer.lightTexture(), LENS, partialTick, frustum);
                if (mc.options.getCloudsType() != CloudStatus.OFF) {
                    mc.levelRenderer.renderClouds(poses, projection, partialTick, eye.x, eye.y, eye.z);
                }
                RenderSystem.getModelViewStack().pushPose();
                RenderSystem.getModelViewStack().mulPoseMatrix(poses.last().pose());
                RenderSystem.applyModelViewMatrix();
                try {
                    mc.levelRenderer.renderSnowAndRain(mc.gameRenderer.lightTexture(), partialTick, eye.x, eye.y, eye.z);
                } finally {
                    RenderSystem.getModelViewStack().popPose();
                    RenderSystem.applyModelViewMatrix();
                }
                // A live image is opaque, including sky pixels blended by vanilla's sky/weather renderers.
                RenderSystem.colorMask(false, false, false, true);
                RenderSystem.clearColor(0, 0, 0, 1);
                RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);
                RenderSystem.colorMask(true, true, true, true);
            } finally {
                activeTarget = null;
                rendering = false;
            }
        }

        @Override
        public void close() {
            if (target != null) {
                target.destroyBuffers();
                Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
            }
        }
    }

    private static List<ChunkRenderDispatcher.RenderChunk> chunks(Frustum frustum, Vec3 eye, int radius) {
        Minecraft mc = Minecraft.getInstance();
        List<ChunkRenderDispatcher.RenderChunk> chunks = new ArrayList<>();
        for (ChunkRenderDispatcher.RenderChunk chunk : mc.levelRenderer.viewArea.chunks) {
            if (chunk.getBoundingBox().getCenter().distanceToSqr(eye) <= (radius + 14) * (radius + 14)
                    && frustum.isVisible(chunk.getBoundingBox()) && mc.level.hasChunkAt(chunk.getOrigin())) {
                chunks.add(chunk);
            }
        }
        chunks.sort(Comparator.comparingDouble(chunk -> chunk.getBoundingBox().getCenter().distanceToSqr(eye)));
        int rebuilt = 0;
        RenderRegionCache cache = new RenderRegionCache();
        for (ChunkRenderDispatcher.RenderChunk chunk : chunks) {
            if (chunk.isDirty() && rebuilt < 2) {
                chunk.rebuildChunkAsync(mc.levelRenderer.chunkRenderDispatcher, cache);
                chunk.setNotDirty();
                rebuilt++;
            }
        }
        mc.levelRenderer.chunkRenderDispatcher.uploadAllPendingUploads();
        return chunks;
    }

    private static void terrain(List<ChunkRenderDispatcher.RenderChunk> chunks, LiveCameraRenderCompat.Terrain nativeTerrain,
                                RenderType type, PoseStack poses, Matrix4f projection, Vec3 eye) {
        if (nativeTerrain != null) {
            nativeTerrain.draw(type, poses, eye, activeTarget);
            return;
        }
        RenderType wrapped = type(type);
        wrapped.setupRenderState();
        ShaderInstance shader = RenderSystem.getShader();
        boolean reverse = type == RenderType.translucent();
        try {
            for (int index = 0; index < chunks.size(); index++) {
                ChunkRenderDispatcher.RenderChunk chunk = chunks.get(reverse ? chunks.size() - index - 1 : index);
                if (!chunk.getCompiledChunk().isEmpty(type)) {
                    BlockPos origin = chunk.getOrigin();
                    if (shader.CHUNK_OFFSET != null) {
                        shader.CHUNK_OFFSET.set((float) (origin.getX() - eye.x), (float) (origin.getY() - eye.y), (float) (origin.getZ() - eye.z));
                    }
                    VertexBuffer buffer = chunk.getBuffer(type);
                    buffer.bind();
                    buffer.drawWithShader(poses.last().pose(), projection, shader);
                }
            }
        } finally {
            if (shader.CHUNK_OFFSET != null) {
                shader.CHUNK_OFFSET.set(0f, 0f, 0f);
            }
            VertexBuffer.unbind();
            wrapped.clearRenderState();
        }
    }

    private static void entities(List<BlockEntity> tiles, PoseStack poses, Frustum frustum, Vec3 eye, float partialTick, int radius) {
        Minecraft mc = Minecraft.getInstance();
        var dispatcher = mc.getEntityRenderDispatcher();
        dispatcher.prepare(mc.level, LENS, null);
        dispatcher.setRenderHitBoxes(false);
        mc.getBlockEntityRenderDispatcher().prepare(mc.level, LENS, null);
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof HoloGuiEntity) && !entity.isRemoved() && entity.distanceToSqr(eye) <= radius * radius
                    && dispatcher.shouldRender(entity, frustum, eye.x, eye.y, eye.z)) {
                dispatcher.render(entity, Mth.lerp(partialTick, entity.xOld, entity.getX()) - eye.x,
                        Mth.lerp(partialTick, entity.yOld, entity.getY()) - eye.y,
                        Mth.lerp(partialTick, entity.zOld, entity.getZ()) - eye.z,
                        Mth.lerp(partialTick, entity.yRotO, entity.getYRot()), partialTick, poses, BUFFERS,
                        dispatcher.getPackedLightCoords(entity, partialTick));
            }
        }
        for (BlockEntity tile : tiles) {
            if (tile.getBlockPos().distToCenterSqr(eye) <= radius * radius && frustum.isVisible(tile.getRenderBoundingBox())) {
                poses.pushPose();
                poses.translate(tile.getBlockPos().getX() - eye.x, tile.getBlockPos().getY() - eye.y, tile.getBlockPos().getZ() - eye.z);
                mc.getBlockEntityRenderDispatcher().render(tile, partialTick, poses, BUFFERS);
                poses.popPose();
            }
        }
        BUFFERS.endBatch();
    }

    private static RenderType type(RenderType original) {
        return original instanceof TargetType ? original : TYPES.computeIfAbsent(original, TargetType::new);
    }

    private static class TargetType extends RenderType {

        private TargetType(RenderType original) {
            super("ariente_live_" + original, original.format(), original.mode(), original.bufferSize(), original.affectsCrumbling(), true,
                    () -> {
                        original.setupRenderState();
                        activeTarget.bindWrite(false);
                    }, () -> {
                        original.clearRenderState();
                        activeTarget.bindWrite(false);
                    });
        }
    }

    private static class TargetBuffers extends MultiBufferSource.BufferSource {

        private TargetBuffers() {
            super(new BufferBuilder(8192), Map.of(type(RenderType.glint()), new BufferBuilder(256),
                    type(RenderType.glintDirect()), new BufferBuilder(256), type(RenderType.glintTranslucent()), new BufferBuilder(256),
                    type(RenderType.entityGlint()), new BufferBuilder(256), type(RenderType.entityGlintDirect()), new BufferBuilder(256),
                    type(RenderType.armorGlint()), new BufferBuilder(256), type(RenderType.armorEntityGlint()), new BufferBuilder(256)));
        }

        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            return super.getBuffer(type(renderType));
        }

        @Override
        public void endBatch(RenderType renderType) {
            super.endBatch(type(renderType));
        }
    }

    private static class Lens extends Camera {

        private void configure(LiveCameraView view, float partialTick) {
            Minecraft mc = Minecraft.getInstance();
            setup(mc.level, mc.player, true, false, partialTick);
            setPosition(view.eye());
            setRotation(view.yaw(), view.pitch());
        }
    }

    private static class State implements AutoCloseable {

        private final Minecraft mc = Minecraft.getInstance();
        private final Camera camera = mc.gameRenderer.getMainCamera();
        private final CameraPerspectiveRenderer.GlState gl = CameraPerspectiveRenderer.GlState.capture();
        private final Matrix4f projection = new Matrix4f(RenderSystem.getProjectionMatrix());
        private final VertexSorting sorting = RenderSystem.getVertexSorting();
        private final Matrix3f inverse = new Matrix3f(RenderSystem.getInverseViewRotationMatrix());
        private final ShaderInstance shader = RenderSystem.getShader();
        private final float fogStart = RenderSystem.getShaderFogStart();
        private final float fogEnd = RenderSystem.getShaderFogEnd();
        private final float[] fogColor = RenderSystem.getShaderFogColor().clone();
        private final FogShape fogShape = RenderSystem.getShaderFogShape();
        private final boolean hitBoxes = mc.getEntityRenderDispatcher().shouldRenderHitBoxes();
        private final int[] textures = new int[12];
        private final Vector3f light0;
        private final Vector3f light1;

        private State() {
            for (int index = 0; index < textures.length; index++) {
                textures[index] = RenderSystem.getShaderTexture(index);
            }
            ShaderInstance lights = GameRenderer.getRendertypeEntitySolidShader();
            RenderSystem.setupShaderLights(lights);
            var first = lights.LIGHT0_DIRECTION.getFloatBuffer();
            var second = lights.LIGHT1_DIRECTION.getFloatBuffer();
            light0 = new Vector3f(first.get(0), first.get(1), first.get(2));
            light1 = new Vector3f(second.get(0), second.get(1), second.get(2));
            RenderSystem.getModelViewStack().pushPose();
            RenderSystem.getModelViewStack().setIdentity();
            RenderSystem.applyModelViewMatrix();
        }

        @Override
        public void close() {
            try {
                BUFFERS.endBatch();
            } finally {
                restore();
            }
        }

        private void restore() {
            mc.gameRenderer.mainCamera = camera;
            mc.getEntityRenderDispatcher().prepare(mc.level, camera, mc.crosshairPickEntity);
            mc.getEntityRenderDispatcher().setRenderHitBoxes(hitBoxes);
            mc.getBlockEntityRenderDispatcher().prepare(mc.level, camera, mc.hitResult);
            RenderSystem.getModelViewStack().popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(projection, sorting);
            RenderSystem.setInverseViewRotationMatrix(inverse);
            RenderSystem.setShaderLights(light0, light1);
            FogRenderer.setupColor(camera, mc.getFrameTime(), mc.level, mc.options.getEffectiveRenderDistance(), mc.gameRenderer.getDarkenWorldAmount(mc.getFrameTime()));
            RenderSystem.setShaderFogStart(fogStart);
            RenderSystem.setShaderFogEnd(fogEnd);
            RenderSystem.setShaderFogColor(fogColor[0], fogColor[1], fogColor[2], fogColor[3]);
            RenderSystem.setShaderFogShape(fogShape);
            RenderSystem.setShader(() -> shader);
            mc.getMainRenderTarget().bindWrite(true);
            gl.restore();
            for (int index = 0; index < textures.length; index++) {
                RenderSystem.setShaderTexture(index, textures[index]);
            }
        }
    }
}
