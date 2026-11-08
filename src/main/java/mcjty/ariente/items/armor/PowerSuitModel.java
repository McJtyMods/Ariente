package mcjty.ariente.items.armor;

import java.util.EnumMap;
import java.util.Map;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.blocks.defense.ForceFieldRenderer;
import mcjty.ariente.items.modules.ModuleSupport;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/** Original 64x128 suit geometry, attached to the modern humanoid animation bones. */
public class PowerSuitModel extends HumanoidModel<LivingEntity> {

    private static final Map<EquipmentSlot, PowerSuitModel> MODELS = new EnumMap<>(EquipmentSlot.class);
    private static final CubeDeformation INFLATION = new CubeDeformation(.01f);

    public static final IClientItemExtensions EXTENSIONS = new IClientItemExtensions() {

        @Override
        public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
                return original;
            }
            if (slot == EquipmentSlot.CHEST && ModuleSupport.hasWorkingUpgrade(stack, ArmorUpgradeType.FORCEFIELD)) {
                ForceFieldRenderer.personalForcefields.add(entity);
            }
            return MODELS.computeIfAbsent(slot, piece -> new PowerSuitModel(createLayer(piece).bakeRoot()));
        }
    };

    public PowerSuitModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createLayer(EquipmentSlot slot) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5, 2, 0));
        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5, 2, 0));
        PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9f, 12, 0));
        PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9f, 12, 0));

        switch (slot) {
            case HEAD -> {
                head.addOrReplaceChild("mask", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.5f, -9, -4.5f, 9, 13, 9, INFLATION), PartPose.ZERO);
                PartDefinition respirator = head.addOrReplaceChild("respirator", CubeListBuilder.create().texOffs(36, 0)
                        .addBox(-2.5f, -5, -5.5f, 5, 4, 4, INFLATION), PartPose.rotation((float) Math.PI / 6, 0, 0));
                respirator.addOrReplaceChild("left", CubeListBuilder.create().texOffs(36, 8).mirror()
                        .addBox(0, 0, -2.5f, 3, 3, 3, INFLATION), PartPose.offsetAndRotation(.5f, -4, -5.5f, 0, -(float) Math.PI / 4, 0));
                respirator.addOrReplaceChild("right", CubeListBuilder.create().texOffs(36, 8)
                        .addBox(-3, 0, -2.5f, 3, 3, 3, INFLATION), PartPose.offsetAndRotation(-.5f, -4, -5.5f, 0, (float) Math.PI / 4, 0));
            }
            case CHEST -> {
                body.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(0, 22)
                        .addBox(-4.5f, 0, -3, 9, 9, 6, INFLATION), PartPose.ZERO);
                leftArm.addOrReplaceChild("sleeve", CubeListBuilder.create().texOffs(0, 37).mirror()
                        .addBox(-1.5f, -2.5f, -2.9f, 5, 8, 6, INFLATION)
                        .texOffs(22, 37).addBox(-1.5f, 5.5f, -2.5f, 5, 5, 5, INFLATION), PartPose.ZERO);
                rightArm.addOrReplaceChild("sleeve", CubeListBuilder.create().texOffs(0, 37)
                        .addBox(-3.5f, -2.5f, -3, 5, 8, 6, INFLATION)
                        .texOffs(22, 37).addBox(-3.5f, 5.5f, -2.5f, 5, 5, 5, INFLATION), PartPose.ZERO);
            }
            case LEGS -> {
                body.addOrReplaceChild("belt", CubeListBuilder.create().texOffs(0, 52)
                        .addBox(-4.5f, 9, -3, 9, 5, 6, INFLATION), PartPose.ZERO);
                leftLeg.addOrReplaceChild("plate", CubeListBuilder.create().texOffs(0, 63)
                        .addBox(-2, -3, -2.55f, 5, 9, 5, INFLATION), PartPose.ZERO);
                rightLeg.addOrReplaceChild("plate", CubeListBuilder.create().texOffs(0, 63)
                        .addBox(-3, -3, -2.55f, 5, 9, 5, INFLATION), PartPose.ZERO);
            }
            case FEET -> {
                leftLeg.addOrReplaceChild("boot", CubeListBuilder.create().texOffs(0, 77)
                        .addBox(-2, 6, -2.5f, 5, 6, 5, INFLATION), PartPose.ZERO);
                rightLeg.addOrReplaceChild("boot", CubeListBuilder.create().texOffs(0, 77)
                        .addBox(-2.95f, 6, -2.5f, 5, 6, 5, INFLATION), PartPose.ZERO);
            }
            default -> throw new IllegalArgumentException("Not an armor slot: " + slot);
        }
        return LayerDefinition.create(mesh, 64, 128);
    }
}
