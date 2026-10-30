package mcjty.ariente.entities.soldier;

import net.minecraft.client.model.AbstractZombieModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

public class SoldierModel<T extends SoldierEntity> extends AbstractZombieModel<T> {

    public SoldierModel(float modelSize, boolean p_i1168_2_) {
        this(modelSize, 0.0F, 64, p_i1168_2_ ? 32 : 64);
    }

    protected SoldierModel(float modelSize, float yOffsetIn, int textureWidthIn, int textureHeightIn) {
        super(LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(modelSize), yOffsetIn),
                textureWidthIn, textureHeightIn).bakeRoot());
    }

    @Override
    public boolean isAggressive(T p_212850_1_) {
        return p_212850_1_.isAggressive();
    }
}
