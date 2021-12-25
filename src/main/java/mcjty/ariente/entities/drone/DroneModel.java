package mcjty.ariente.entities.drone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mcjty.ariente.setup.Registration;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.FlyingMob;
import net.minecraftforge.client.event.EntityRenderersEvent;

public class DroneModel <T extends FlyingMob> extends HierarchicalModel<T> {
    public static final ModelLayerLocation MODEL = new ModelLayerLocation(Registration.ENTITY_DRONE.getId(), "body");

    private final ModelPart root;

    public DroneModel(ModelPart modelPart) {
        super();
        this.root = modelPart;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, 4.0F, -8.0F, 16, 16, 16), PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MODEL, DroneModel::createBodyLayer);
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
    }

    @Override
    public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, 1.0f /* @todo 1.15 scale*/, entityIn);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}