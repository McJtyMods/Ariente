package mcjty.ariente.entities.drone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
// @todo 1.18 import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.FlyingMob;

public class DroneModel <T extends FlyingMob> extends EntityModel<T> {
    // @todo 1.18 ModelRenderer body;

    public DroneModel() {
        // @todo 1.18 this.body = new ModelRenderer(this, 0, 0);
        // @todo 1.18 this.body.addBox(-8.0F, -8.0F, -8.0F, 16, 16, 16);
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
    }

    @Override
    public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, 1.0f /* @todo 1.15 scale*/, entityIn);
    }

    @Override
    public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.0F, 0.6F, 0.0F);
        // @todo 1.18 this.body.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);

        matrixStackIn.popPose();

    }
}