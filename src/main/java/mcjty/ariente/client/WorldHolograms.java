package mcjty.ariente.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.display.client.LiveCameraRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/** Composite world-space holograms after the scene, without translucent depth writes. */
public class WorldHolograms {

    private static final List<Entry> PENDING = new ArrayList<>();
    private static final Set<BlockEntity> QUEUED = Collections.newSetFromMap(new IdentityHashMap<>());
    // Foil requests two consumers at once, so glint cannot share the fallback builder.
    private static final MultiBufferSource.BufferSource BUFFERS = MultiBufferSource.immediateWithBuffers(Map.of(
            RenderType.glint(), new BufferBuilder(256),
            RenderType.glintDirect(), new BufferBuilder(256),
            RenderType.glintTranslucent(), new BufferBuilder(256),
            RenderType.entityGlint(), new BufferBuilder(256),
            RenderType.entityGlintDirect(), new BufferBuilder(256)), new BufferBuilder(4096));

    public static boolean begin(BlockEntity owner) {
        return !LiveCameraRenderer.isRendering() && QUEUED.add(owner);
    }

    public static PoseStack copy(PoseStack poses) {
        PoseStack copy = new PoseStack();
        copy.last().pose().set(poses.last().pose());
        copy.last().normal().set(poses.last().normal());
        return copy;
    }

    public static void add(Vec3 center, Consumer<MultiBufferSource.BufferSource> draw) {
        if (!LiveCameraRenderer.isRendering()) {
            PENDING.add(new Entry(center, draw));
        }
    }

    public static void renderWorld() {
        if (PENDING.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        // Fabulous composites a full-screen quad into the main depth attachment.
        // The particle target retains the scene's original opaque depth for world-space overlays.
        if (Minecraft.useShaderTransparency() && mc.levelRenderer.getParticlesTarget() != null) {
            mc.getMainRenderTarget().copyDepthFrom(mc.levelRenderer.getParticlesTarget());
        }
        mc.getMainRenderTarget().bindWrite(false);
        render();
    }

    public static void render() {
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        PENDING.sort(Comparator.comparingDouble((Entry entry) -> entry.center().distanceToSqr(camera)).reversed());
        try {
            for (Entry entry : PENDING) {
                entry.draw().accept(BUFFERS);
                BUFFERS.endBatch();
            }
        } finally {
            PENDING.clear();
            QUEUED.clear();
        }
    }

    private record Entry(Vec3 center, Consumer<MultiBufferSource.BufferSource> draw) {
    }
}
