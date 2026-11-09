package mcjty.ariente.client;

import net.minecraft.client.renderer.RenderBuffers;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Optional;
import java.util.OptionalDouble;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import mcjty.ariente.display.client.LiveCameraRenderer;
import mcjty.hologui.gui.HoloRenderBatch;
import net.minecraft.client.Minecraft;
import mcjty.lib.client.RenderBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/** Deferred overlays contain only extracted geometry and item render state. */
public final class WorldHolograms {

    private static final ContextKey<Frame> FRAME = new ContextKey<>(Identifier.fromNamespaceAndPath("ariente", "holograms"));
    private static final List<Entry> PENDING = new ArrayList<>();
    private static final Set<BlockEntity> QUEUED = Collections.newSetFromMap(new IdentityHashMap<>());
    private static BlockPos extractionOrigin;
    private static Frame activeFrame;
    private static FeatureRenderDispatcher features;
    private static SubmitNodeStorage storage;
    private static RenderBuffers overlayBuffers;

    private WorldHolograms() {
    }

    public static BlockPos origin(BlockPos origin) {
        BlockPos previous = extractionOrigin;
        extractionOrigin = origin;
        return previous;
    }

    public static boolean begin(BlockEntity owner) {
        return !LiveCameraRenderer.isRendering() && QUEUED.add(owner);
    }

    public static PoseStack copy(PoseStack poses) {
        PoseStack copy = new PoseStack();
        copy.last().set(poses.last());
        return copy;
    }

    public static void add(Vec3 center, Consumer<RenderBufferSource> draw) {
        if (LiveCameraRenderer.isRendering()) return;
        HoloRenderBatch batch = new HoloRenderBatch();
        draw.accept(batch);
        PENDING.add(new Entry(center, extractionOrigin, batch));
    }

    public static void addRelative(Vec3 center, HoloRenderBatch batch) {
        PENDING.add(new Entry(center, null, batch));
    }

    public static void finishExtraction(LevelExtractionContext event) {
        if (LiveCameraRenderer.isRendering()) return;
        RenderData.set(event.levelState(), FRAME, new Frame(List.copyOf(PENDING), event.camera().position()));
        PENDING.clear();
        QUEUED.clear();
    }

    public static void captureFrame(LevelRenderContext event) {
        if (!LiveCameraRenderer.isRendering()) activeFrame = RenderData.get(event.levelState(), FRAME);
    }

    public static void renderWorld() {
        Frame frame = activeFrame;
        activeFrame = null;
        if (frame == null || frame.entries().isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (overlayBuffers == null) overlayBuffers = new RenderBuffers(1);
        var buffers = overlayBuffers;
        if (features == null) {
            storage = new SubmitNodeStorage();
            features = new FeatureRenderDispatcher(buffers, mc.getModelManager(), mc.getAtlasManager(), mc.font, mc.gameRenderer.gameRenderState());
        }
        List<Entry> entries = new ArrayList<>(frame.entries());
        entries.sort(Comparator.comparingDouble((Entry entry) -> entry.center().distanceToSqr(frame.camera())).reversed());
        for (Entry entry : entries) {
            PoseStack poses = new PoseStack();
            if (entry.origin() != null) poses.translate(entry.origin().getX() - frame.camera().x,
                    entry.origin().getY() - frame.camera().y, entry.origin().getZ() - frame.camera().z);
            entry.batch().submit(poses, storage);
            var target = mc.gameRenderer.mainRenderTarget();
            try (var prepared = features.prepareFrame(storage);
                 var pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                         () -> "Ariente holograms", target.getColorTextureView(), Optional.empty(),
                         target.getDepthTextureView(), OptionalDouble.empty())) {
                FeatureRenderDispatcher.renderAllFeatures(pass, prepared);
            }
            buffers.endFrame();
        }
    }

    private record Frame(List<Entry> entries, Vec3 camera) {
    }

    private record Entry(Vec3 center, BlockPos origin, HoloRenderBatch batch) {
    }
}
