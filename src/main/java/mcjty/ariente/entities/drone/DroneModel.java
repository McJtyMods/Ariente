package mcjty.ariente.entities.drone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.world.entity.FlyingMob;

public class DroneModel<T extends FlyingMob> extends EntityModel<T> {

    private final ModelPart body;

    public DroneModel() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-8, -8, -8, 16, 16, 16), PartPose.ZERO);
        body = LayerDefinition.create(mesh, 64, 32).bakeRoot().getChild("body");
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, float red, float green, float blue, float alpha) {
        poseStack.pushPose();
        poseStack.translate(0, 0.6, 0);
        body.render(poseStack, buffer, light, overlay, red, green, blue, alpha);
        poseStack.popPose();
    }
}
