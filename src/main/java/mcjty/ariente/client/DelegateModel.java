package mcjty.ariente.client;

import mcjty.lib.client.BakedModelWrapper;
import net.minecraft.client.resources.model.BakedModel;

public abstract class DelegateModel extends BakedModelWrapper<BakedModel> {

    protected DelegateModel(BakedModel delegate) {
        super(delegate);
    }
}
