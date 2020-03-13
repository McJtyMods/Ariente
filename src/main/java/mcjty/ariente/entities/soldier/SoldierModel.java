package mcjty.ariente.entities.soldier;

import net.minecraft.client.renderer.entity.model.AbstractZombieModel;

public class SoldierModel<T extends SoldierEntity> extends AbstractZombieModel<T> {

    public SoldierModel(float modelSize, boolean p_i1168_2_) {
        this(modelSize, 0.0F, 64, p_i1168_2_ ? 32 : 64);
    }

    protected SoldierModel(float modelSize, float yOffsetIn, int textureWidthIn, int textureHeightIn) {
        super(modelSize, yOffsetIn, textureWidthIn, textureHeightIn);
    }

    public boolean func_212850_a_(T p_212850_1_) {
        return p_212850_1_.isAggressive();
    }
}
