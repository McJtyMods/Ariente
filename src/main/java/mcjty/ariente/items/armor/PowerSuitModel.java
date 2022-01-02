package mcjty.ariente.items.armor;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.blocks.defense.ForceFieldRenderer;
import mcjty.ariente.blocks.defense.PanelInfo;
import mcjty.ariente.blocks.defense.PentakisDodecahedron;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.varia.Triangle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.EntityRenderersEvent;

import static mcjty.ariente.blocks.defense.ForceFieldRenderer.FORCEFIELD;

public class PowerSuitModel extends HumanoidModel<LivingEntity> {
    public static final ModelLayerLocation MODEL = new ModelLayerLocation(Registration.POWERSUIT_CHEST.getId(), "body");

    public static PowerSuitModel modelHelm;
    public static PowerSuitModel modelChest;
    public static PowerSuitModel modelLegs;
    public static PowerSuitModel modelBoots;

    private static PanelInfo[] panelInfo = null;

    // Hazmat Suit

    public ModelPart mask;
    public ModelPart respirator;

    public ModelPart arm_l;
    public ModelPart arm_r;
    public ModelPart glove_l;
    public ModelPart glove_r;

    public ModelPart belt;
    public ModelPart leg_l;
    public ModelPart leg_r;

    public ModelPart bootsLeft;
    public ModelPart bootsRight;

    public PowerSuitModel(ModelPart pRoot) {
        super(pRoot);
        this.mask = this.head.getChild("mask");
        this.respirator = this.head.getChild("respirator");
        this.arm_l = this.leftArm.getChild("vambrace");
        this.arm_r = this.rightArm.getChild("vambrace");
        this.glove_l = this.leftArm.getChild("glove");
        this.glove_r = this.rightArm.getChild("glove");
        this.belt = this.body.getChild("belt");
        this.leg_l = this.leftLeg.getChild("cuisses");
        this.leg_r = this.rightLeg.getChild("cuisses");
        this.bootsLeft = this.leftLeg.getChild("boot");
        this.bootsRight = this.rightLeg.getChild("boot");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        // helmet
        partdefinition.getChild("head").addOrReplaceChild(
                "mask",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.5F, -9.0F, -4.5F, 9, 13, 9),
                PartPose.ZERO
        );

        PartDefinition respirator = partdefinition.getChild("head").addOrReplaceChild(
                "respirator",
                CubeListBuilder.create()
                        .texOffs(36, 0)
                        .addBox(-2.5F, -5.0F, -5.5F, 5, 4, 4),
                PartPose.rotation(0.5235987755982988F, 0.0F, 0.0F)
        );

        respirator.addOrReplaceChild(
                "left",
                CubeListBuilder.create()
                        .texOffs(36, 8)
                        .addBox(0.0F, 0.0F, -2.5F, 3, 3, 3)
                        .mirror(),
                PartPose.offsetAndRotation(0.5F, -4.0F, -5.5F, 0.0F, -0.7853981633974483F, 0.0F)
        );

        respirator.addOrReplaceChild(
                "right",
                CubeListBuilder.create()
                        .texOffs(36, 8)
                        .addBox(-3.0F, 0.0F, -2.5F, 3, 3, 3),
                PartPose.offsetAndRotation(-0.5F, -4.0F, -5.5F, 0.0F, 0.7853981633974483F, 0.0F)
        );

        // chestplate
        partdefinition.getChild("body").addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 22)
                        .addBox(-4.5F, 0.0F, -3.0F, 9, 9, 6),
                PartPose.ZERO
        );

        PartDefinition leftArm = partdefinition.getChild("left_arm");

        leftArm.addOrReplaceChild(
                "vambrace",
                CubeListBuilder.create()
                        .texOffs(0, 37)
                        .addBox(-1.5F, -2.5F, -2.9F, 5, 8, 6)
                        .mirror(),
                PartPose.ZERO
        );

        leftArm.addOrReplaceChild(
                "glove",
                CubeListBuilder.create()
                        .texOffs(22, 37)
                        .addBox(-1.5F, 5.5F, -2.5F, 5, 5, 5)
                        .mirror(),
                PartPose.ZERO
        );

        PartDefinition rightArm = partdefinition.getChild("right_arm");

        rightArm.addOrReplaceChild(
                "vambrace",
                CubeListBuilder.create()
                        .texOffs(0, 37)
                        .addBox(-3.5F, -2.5F, -3.0F, 5, 8, 6),
                PartPose.ZERO
        );

        rightArm.addOrReplaceChild(
                "glove",
                CubeListBuilder.create()
                        .texOffs(22, 37)
                        .addBox(-3.5F, 5.5F, -2.5F, 5, 5, 5),
                PartPose.ZERO
        );

        // legs
        partdefinition.getChild("body").addOrReplaceChild(
                "belt",
                CubeListBuilder.create()
                        .texOffs(0, 52)
                        .addBox(-4.5F, 9.0F, -3.0F, 9, 5, 6),
                PartPose.ZERO
        );

        PartDefinition leftLeg = partdefinition.getChild("left_leg");

        leftLeg.addOrReplaceChild(
                "cuisses",
                CubeListBuilder.create()
                        .texOffs(0, 63)
                        .addBox(-2.0F, -3.0F, -2.55F, 5, 9, 5),
                PartPose.ZERO
        );

        leftLeg.addOrReplaceChild(
                "boot",
                CubeListBuilder.create()
                        .texOffs(0, 77)
                        .addBox(-2.0F, 6.0F, -2.5F, 5, 6, 5),
                PartPose.ZERO
        );

        PartDefinition rightLeg = partdefinition.getChild("right_leg");

        rightLeg.addOrReplaceChild(
                "cuisses",
                CubeListBuilder.create()
                        .texOffs(0, 63)
                        .addBox(-3.0F, -3.0F, -2.55F, 5, 9, 5),
                PartPose.ZERO
        );

        rightLeg.addOrReplaceChild(
                "boot",
                CubeListBuilder.create()
                        .texOffs(0, 77)
                        .addBox(-2.95F, 6.0F, -2.5F, 5, 6, 5),
                PartPose.ZERO
        );

        return LayerDefinition.create(meshdefinition, 64, 128);
    }

    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MODEL, PowerSuitModel::createLayer);
    }

    // @todo 1.18 private void setRotation(ModelRenderer model, float x, float y, float z) {
    // @todo 1.18     model.xRot = x;
    // @todo 1.18     model.yRot = y;
    // @todo 1.18     model.zRot = z;
    // @todo 1.18 }

    // @todo 1.18 public static HumanoidModel getModel(LivingEntity entity, ItemStack stack) {

    // @todo 1.18     if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem)) {
    // @todo 1.18         return null;
    // @todo 1.18     }
    // @todo 1.18     EquipmentSlot slot = ((ArmorItem) stack.getItem()).getSlot();

    // @todo 1.18     PowerSuitModel armor;
    // @todo 1.18
    // @todo 1.18     if (slot == EquipmentSlot.HEAD && modelHelm != null) {
    // @todo 1.18         return modelHelm;
    // @todo 1.18     } else if (slot == EquipmentSlot.CHEST && modelChest != null) {
    // @todo 1.18         return modelChest;
    // @todo 1.18     } else if (slot == EquipmentSlot.LEGS && modelLegs != null) {
    // @todo 1.18         return modelLegs;
    // @todo 1.18     } else if (slot == EquipmentSlot.FEET && modelBoots!= null) {
    // @todo 1.18         return modelBoots;
    // @todo 1.18     }

    // @todo 1.18     armor = new PowerSuitModel(0.0625f);
    // @todo 1.18
    // @todo 1.18     armor.head.visible = false;
    // @todo 1.18     armor.body.visible = false;
    // @todo 1.18     armor.leftArm.visible = false;
    // @todo 1.18     armor.rightArm.visible = false;
    // @todo 1.18     armor.leftLeg.visible = false;
    // @todo 1.18     armor.rightLeg.visible = false;
    // @todo 1.18     armor.bootsLeft.visible = false;
    // @todo 1.18     armor.bootsRight.visible = false;

    // @todo 1.18     switch (slot) {
    // @todo 1.18         case HEAD:
    // @todo 1.18             armor.head.visible = true;
    // @todo 1.18             modelHelm = armor;
    // @todo 1.18             break;
    // @todo 1.18
    // @todo 1.18         case FEET:
    // @todo 1.18             armor.rightLeg.visible = true;
    // @todo 1.18             armor.leftLeg.visible = true;
    // @todo 1.18             armor.leg_l.visible = false;
    // @todo 1.18             armor.leg_r.visible = false;
    // @todo 1.18             armor.bootsLeft.visible = true;
    // @todo 1.18             armor.bootsRight.visible = true;
    // @todo 1.18             modelBoots = armor;
    // @todo 1.18             break;
    // @todo 1.18
    // @todo 1.18         case LEGS:
    // @todo 1.18             armor.leftLeg.visible = true;
    // @todo 1.18             armor.rightLeg.visible = true;
    // @todo 1.18             modelLegs = armor;
    // @todo 1.18             break;
    // @todo 1.18
    // @todo 1.18         case CHEST:
    // @todo 1.18             armor.body.visible = true;
    // @todo 1.18             armor.leftArm.visible = true;
    // @todo 1.18             armor.rightArm.visible = true;
    // @todo 1.18             modelChest = armor;
    // @todo 1.18             break;
    // @todo 1.18     }
    // @todo 1.18
    // @todo 1.18     return armor;
    // @todo 1.18 }

    //@Override
    public void xxsetupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

        if(entity instanceof LivingEntity)	{
            this.crouching = entity.isShiftKeyDown();
            // @todo 1.14
//            this.isRiding = entity.isRiding();
            this.young = ((LivingEntity)entity).isBaby();
            this.prepareMobModel((LivingEntity)entity, limbSwing, limbSwingAmount, ageInTicks);
            // @todo 1.15
//            this.setRotationAngles((LivingEntity) entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }

        // @todo 1.15
//        if (this.isChild) {
//            float f6 = 2.0F;
//            GlStateManager.pushMatrix();
//            GlStateManager.scalef(1.5F / f6, 1.5F / f6, 1.5F / f6);
//            GlStateManager.translatef(0.0F, 16.0F * scale, 0.0F);
//            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//            GlStateManager.enableBlend();
//            this.bipedHead.render(scale);
//            GlStateManager.disableBlend();
//            GlStateManager.popMatrix();
//            GlStateManager.pushMatrix();
//            GlStateManager.scalef(1.0F / f6, 1.0F / f6, 1.0F / f6);
//            GlStateManager.translatef(0.0F, 24.0F * scale, 0.0F);
//            this.bipedBody.render(scale);
//            this.bipedRightArm.render(scale);
//            this.bipedLeftArm.render(scale);
//            this.bipedRightLeg.render(scale);
//            this.bipedLeftLeg.render(scale);
//            GlStateManager.popMatrix();
//        } else {
//            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//            GlStateManager.enableBlend();
//            this.bipedHead.render(scale);
//            GlStateManager.disableBlend();
//            this.bipedBody.render(scale);
//            this.bipedRightArm.render(scale);
//            this.bipedLeftArm.render(scale);
//            this.bipedRightLeg.render(scale);
//            this.bipedLeftLeg.render(scale);
//        }

        if (this == modelChest) {   // @todo Proper test
            if (entity instanceof LivingEntity) {
                ItemStack chestStack = ((LivingEntity) entity).getItemBySlot(EquipmentSlot.CHEST);
                if (chestStack.getItem() == Registration.POWERSUIT_CHEST.get()) {
                    if (ModuleSupport.hasWorkingUpgrade(chestStack, ArmorUpgradeType.FORCEFIELD)) {
                        ForceFieldRenderer.personalForcefields.put(new Vec3(entity.xo, entity.yo, entity.zo), entity instanceof Player);
                    }
                }
            }
        }

    }


    public static void renderForcefield(double posX, double posY, double posZ, float partialTicks) {
//        GlStateManager.pushMatrix();
//        GlStateManager.translate(-.75, 0, -.5);
        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.lightTexture().turnOffLightLayer();
        GlStateManager._enableTexture();
        // @todo 1.18 GlStateManager._disableLighting();
        GlStateManager._enableBlend();
        GlStateManager._depthMask(false);
        GlStateManager._enableDepthTest();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.1f);
        mc.getTextureManager().bindForSetup(FORCEFIELD);

        Tesselator t = Tesselator.getInstance();
        BufferBuilder builder = t.getBuilder();

        Entity renderViewEntity = Minecraft.getInstance().getCameraEntity();
        double dx = renderViewEntity.xOld + (renderViewEntity.getX() - renderViewEntity.xOld) * partialTicks;
        double dy = renderViewEntity.yOld + (renderViewEntity.getY() - renderViewEntity.yOld) * partialTicks;
        double dz = renderViewEntity.zOld + (renderViewEntity.getZ() - renderViewEntity.zOld) * partialTicks;
        double x = posX + 0 - dx;
        double y = posY + .4 - dy;
        double z = posZ + 0 - dz;

        double scale = 1.2;

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.2f);
        renderPanels(t, builder, x, y, z, scale, getPanelInfo(posX, posY, posZ, scale));

        GlStateManager._enableTexture();
        GlStateManager._depthMask(true);
        // @todo 1.18 GlStateManager._enableLighting();
//        GlStateManager.popMatrix();
    }

    private static PanelInfo[] getPanelInfo(double posX, double posY, double posZ, double scale) {
        if (panelInfo == null) {
            panelInfo = new PanelInfo[PentakisDodecahedron.MAX_TRIANGLES];
        }
        for (int i = 0 ; i < PentakisDodecahedron.MAX_TRIANGLES ; i++) {
            Triangle triangle = PentakisDodecahedron.getTriangle(i);
            Vec3 offs = triangle.getMid().scale(scale);
            double x = posX+.5 + offs.x;
            double y = posY+.5 + offs.y;
            double z = posZ+.5 + offs.z;
            panelInfo[i] = new PanelInfo(i, x, y, z);
        }
        return panelInfo;
    }


    private static void renderPanels(Tesselator t, BufferBuilder builder, double x, double y, double z, double scale, PanelInfo[] panelInfo) {
        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);

        for (PanelInfo info : panelInfo) {
            if (info != null) {
                renderPanel(x, y, z, scale, info);
            }
        }
        t.end();
    }

    private static void renderPanel(double x, double y, double z, double scale, PanelInfo info) {
        ForceFieldRenderer.doRender(info, x, y, z, scale, 1.0f, 1.0f, 1.0f, 0.2f);
    }


}
