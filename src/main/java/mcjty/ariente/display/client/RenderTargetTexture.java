package mcjty.ariente.display.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.renderer.texture.AbstractTexture;

/** A managed texture name for a target owned by its camera feed. */
final class RenderTargetTexture extends AbstractTexture {

    private final RenderTarget target;

    RenderTargetTexture(RenderTarget target) {
        this.target = target;
        sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
    }

    @Override
    public GpuTexture getTexture() {
        return target.getColorTexture();
    }

    @Override
    public GpuTextureView getTextureView() {
        return target.getColorTextureView();
    }

    @Override
    public void close() {
        // The feed closes the target, including its current texture after a resize.
    }
}
