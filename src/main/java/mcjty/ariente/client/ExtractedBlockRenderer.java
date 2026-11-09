package mcjty.ariente.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.hologui.gui.HoloRenderBatch;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/** Extracts Ariente's procedural geometry into a frame-owned snapshot. */
public abstract class ExtractedBlockRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, ExtractedBlockRenderer.State> {

    public static class State extends BlockEntityRenderState {
        private HoloRenderBatch batch;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T tile, State state, float partialTick, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTick, camera, breaking);
        state.batch = new HoloRenderBatch();
        var previous = WorldHolograms.origin(tile.getBlockPos());
        try {
            render(tile, partialTick, new PoseStack(), state.batch, state.lightCoords, OverlayTexture.NO_OVERLAY);
        } finally {
            WorldHolograms.origin(previous);
        }
    }

    public abstract void render(T tile, float partialTick, PoseStack poses, MultiBufferSource buffer, int light, int overlay);

    @Override
    public void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        state.batch.submit(poses, collector);
    }
}
