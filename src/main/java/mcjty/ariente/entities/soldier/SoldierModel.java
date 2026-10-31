package mcjty.ariente.entities.soldier;

import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.HumanoidArm;

public class SoldierModel<T extends SoldierEntity> extends HumanoidModel<T> {

    public SoldierModel(float modelSize, boolean p_i1168_2_) {
        this(modelSize, 0.0F, 64, p_i1168_2_ ? 32 : 64);
    }

    protected SoldierModel(float modelSize, float yOffsetIn, int textureWidthIn, int textureHeightIn) {
        super(LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(modelSize), yOffsetIn),
                textureWidthIn, textureHeightIn).bakeRoot());
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch) {
        boolean rightHanded = entity.getMainArm() == HumanoidArm.RIGHT;
        rightArmPose = (rightHanded ? entity.getMainHandItem() : entity.getOffhandItem()).isEmpty() ? ArmPose.EMPTY : ArmPose.ITEM;
        leftArmPose = (rightHanded ? entity.getOffhandItem() : entity.getMainHandItem()).isEmpty() ? ArmPose.EMPTY : ArmPose.ITEM;
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch);
        if (entity.isAggressive()) {
            AnimationUtils.swingWeaponDown(rightArm, leftArm, entity, attackTime, ageInTicks);
        }
    }
}
