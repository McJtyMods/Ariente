package mcjty.ariente.entities.levitator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;

public class FluxLevitatorModel<T extends Entity> extends EntityModel<T> {

    private final ModelPart root;

    public FluxLevitatorModel() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("sideModels3", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -10.0F, 4.0F, 16, 9, 2),
                PartPose.offsetAndRotation(15.0F, 4.0F, 0.0F, 0, 0, 0));
        mesh.getRoot().addOrReplaceChild("sideModelsS1", CubeListBuilder.create().texOffs(0, 0).addBox(6.5F, -19.5F, 5.0F, 2, 11, 2),
                PartPose.offsetAndRotation(-15.0F, 4.0F, 0.0F, 0, 0, 0));
        mesh.getRoot().addOrReplaceChild("sideModels5", CubeListBuilder.create().texOffs(0, 0).addBox(-14.0F, -10.0F, 0.5F, 28, 9, 2),
                PartPose.offsetAndRotation(0.0F, 4.0F, 7.0F, 0, 0, 0));
        mesh.getRoot().addOrReplaceChild("sideModelsS2", CubeListBuilder.create().texOffs(0, 0).addBox(-8.5F, -19.5F, 5.0F, 2, 11, 2),
                PartPose.offsetAndRotation(-15.0F, 4.0F, 0.0F, 0, 0, 0));
        mesh.getRoot().addOrReplaceChild("sideModelsS4", CubeListBuilder.create().texOffs(0, 0).addBox(-8.5F, -19.5F, -6.5F, 2, 11, 2),
                PartPose.offsetAndRotation(15.0F, 4.0F, 0.0F, 0, 0, 0));
        mesh.getRoot().addOrReplaceChild("sideModels2", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -10.0F, 4.0F, 16, 9, 2),
                PartPose.offsetAndRotation(-15.0F, 4.0F, 0.0F, 0, 0, 0));
        mesh.getRoot().addOrReplaceChild("sideModels4", CubeListBuilder.create().texOffs(0, 0).addBox(-14.0F, -10.0F, 0.5F, 28, 9, 2),
                PartPose.offsetAndRotation(0.0F, 4.0F, -7.0F, 0, 0, 0));
        mesh.getRoot().addOrReplaceChild("sideModelsS3", CubeListBuilder.create().texOffs(0, 0).addBox(6.5F, -19.5F, -6.5F, 2, 11, 2),
                PartPose.offsetAndRotation(15.0F, 4.0F, 0.0F, 0, 0, 0));
        mesh.getRoot().addOrReplaceChild("sideModelsB", CubeListBuilder.create().texOffs(0, 10).addBox(-14.0F, -8.0F, -1.0F, 28, 16, 2),
                PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0, 0, 0));
        mesh.getRoot().addOrReplaceChild("sideModelsT", CubeListBuilder.create().texOffs(0, 10).addBox(-14.0F, -8.0F, 15.0F, 28, 16, 2),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0, 0, 0));
        root = LayerDefinition.create(mesh, 64, 32).bakeRoot();
        root.getChild("sideModels5").xScale = 1.5f;
        root.getChild("sideModels4").xScale = 1.5f;
        root.getChild("sideModelsB").xScale = 1.5f;
        root.getChild("sideModelsB").yScale = 1.2f;
        root.getChild("sideModelsT").xScale = 1.35f;
        root.getChild("sideModelsT").yScale = 1.1f;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poses, VertexConsumer buffer, int light, int overlay, float red, float green, float blue, float alpha) {
        root.render(poses, buffer, light, overlay, red, green, blue, alpha);
    }
}
