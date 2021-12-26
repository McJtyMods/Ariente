package mcjty.ariente.entities.soldier;

import net.minecraft.client.model.AbstractZombieModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;

public class SoldierModel<T extends SoldierEntity> extends AbstractZombieModel<T> {

    public static final ModelLayerLocation MODEL = ModelLayers.ZOMBIE; //new ModelLayerLocation(Registration.ENTITY_SOLDIER.getId(), "body");
    public static final ModelLayerLocation MASTER_MODEL = ModelLayers.ZOMBIE; //new ModelLayerLocation(Registration.ENTITY_MASTER_SOLDIER.getId(), "body");

    // public SoldierModel(float modelSize, boolean p_i1168_2_) {
    //     this(modelSize, 0.0F, 64, p_i1168_2_ ? 32 : 64);
    // }

    // protected SoldierModel(float modelSize, float yOffsetIn, int textureWidthIn, int textureHeightIn) {
    //     super(modelSize, yOffsetIn, textureWidthIn, textureHeightIn);
    // }

    public SoldierModel(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public boolean isAggressive(T p_212850_1_) {
        return p_212850_1_.isAggressive();
    }
}
