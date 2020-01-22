package mcjty.ariente.entities.drone;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FlyingEntity;

public class DroneModel <T extends FlyingEntity> extends EntityModel<T> {
    RendererModel body;

    public DroneModel() {
        this.body = new RendererModel(this, 0, 0);
        this.body.addBox(-8.0F, -8.0F, -8.0F, 16, 16, 16);
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
    }

    @Override
    public void render(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        GlStateManager.pushMatrix();
        GlStateManager.translatef(0.0F, 0.6F, 0.0F);
        this.body.render(scale);

        GlStateManager.popMatrix();
    }
}