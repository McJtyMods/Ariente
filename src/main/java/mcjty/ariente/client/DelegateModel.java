package mcjty.ariente.client;

import mcjty.lib.client.AbstractDynamicBakedModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.sprite.Material;

public abstract class DelegateModel extends AbstractDynamicBakedModel {

    protected final BlockStateModel delegate;

    protected DelegateModel(BlockStateModel delegate) {
        this.delegate = delegate;
    }

    @Override
    public Material.Baked particleMaterial() {
        return delegate.particleMaterial();
    }

    @Override
    public int materialFlags() {
        return delegate.materialFlags();
    }
}
