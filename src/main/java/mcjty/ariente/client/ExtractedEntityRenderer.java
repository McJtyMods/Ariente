package mcjty.ariente.client;

import mcjty.ariente.entities.LaserEntity;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import net.minecraft.world.phys.AABB;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.hologui.gui.HoloRenderBatch;
import mcjty.lib.client.RenderBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public abstract class ExtractedEntityRenderer<T extends Entity> extends EntityRenderer<T, ExtractedEntityRenderer.State> {

    public static class State extends EntityRenderState {
        private HoloRenderBatch batch;
    }

    protected ExtractedEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected AABB getBoundingBoxForCulling(T entity) {
        if (entity instanceof LaserEntity laser) {
            return laser.getBoundingBoxForCulling();
        }
        if (entity instanceof FluxShipEntity ship) {
            return ship.getBoundingBoxForCulling();
        }
        return super.getBoundingBoxForCulling(entity);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.batch = new HoloRenderBatch();
        render(entity, entity.getViewYRot(partialTick), partialTick, new PoseStack(), state.batch, state.lightCoords);
    }

    public void render(T entity, float yaw, float partialTick, PoseStack poses, RenderBufferSource buffer, int light) {
    }

    public abstract Identifier getTextureLocation(T entity);

    @Override
    public void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poses, collector, camera);
        state.batch.submit(poses, collector);
    }
}
