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
// @todo 1.18 import net.minecraft.client.renderer.model.ModelRenderer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Map;

import static mcjty.ariente.blocks.defense.ForceFieldRenderer.FORCEFIELD;

public class PowerSuitModel extends HumanoidModel<LivingEntity> {

    public static PowerSuitModel modelHelm;
    public static PowerSuitModel modelChest;
    public static PowerSuitModel modelLegs;
    public static PowerSuitModel modelBoots;

    private static PanelInfo[] panelInfo = null;

    // Hazmat Suit

    // @todo 1.18 public ModelRenderer mask;
    // @todo 1.18 public ModelRenderer respirator;
    // @todo 1.18 public ModelRenderer resp_l;
    // @todo 1.18 public ModelRenderer resp_r;
    // @todo 1.18
    // @todo 1.18 public ModelRenderer body;
    // @todo 1.18 public ModelRenderer arm_l;
    // @todo 1.18 public ModelRenderer arm_r;
    // @todo 1.18 public ModelRenderer glove_l;
    // @todo 1.18 public ModelRenderer glove_r;
    // @todo 1.18
    // @todo 1.18 public ModelRenderer belt;
    // @todo 1.18 public ModelRenderer leg_l;
    // @todo 1.18 public ModelRenderer leg_r;
    // @todo 1.18
    // @todo 1.18 private ModelRenderer bootsLeft;
    // @todo 1.18 private ModelRenderer bootsRight;
    // @todo 1.18 public ModelRenderer boot_l;
    // @todo 1.18 public ModelRenderer boot_r;

    public PowerSuitModel(float scale) {
        super(new ModelPart(List.of(), Map.of()));

    	// @todo 1.18 this.texWidth = 64;
        // @todo 1.18 this.texHeight = 128;
        float s = 0.01F;

        // @todo 1.15
//        this.bipedHead.cubeList.clear();
//        this.bipedHeadwear.cubeList.clear();
//        this.bipedBody.cubeList.clear();
//        this.bipedRightArm.cubeList.clear();
//        this.bipedLeftArm.cubeList.clear();
//        this.bipedLeftLeg.cubeList.clear();
//        this.bipedRightLeg.cubeList.clear();
        
        //helmet
        // @todo 1.18 this.mask = new ModelRenderer(this, 0, 0);
        // @todo 1.18 this.mask.addBox(-4.5F, -9.0F, -4.5F, 9, 13, 9, s);
        // @todo 1.18 this.mask.setPos(0.0F, 0.0F, 0.0F);
        // @todo 1.18
        // @todo 1.18 this.respirator = new ModelRenderer(this, 36, 0);
        // @todo 1.18 this.respirator.addBox(-2.5F, -5.0F, -5.5F, 5, 4, 4, s);
        // @todo 1.18 this.respirator.setPos(0.0F, 0.0F, 0.0F);
        // @todo 1.18 this.setRotation(respirator, 0.5235987755982988F, 0.0F, 0.0F);
        // @todo 1.18
        // @todo 1.18 this.resp_l = new ModelRenderer(this, 36, 8);
        // @todo 1.18 this.resp_l.addBox(0.0F, 0.0F, -2.5F, 3, 3, 3, s);
        // @todo 1.18 this.resp_l.setPos(0.5F, -4.0F, -5.5F);
        // @todo 1.18 this.setRotation(resp_l, 0.0F, -0.7853981633974483F, 0.0F);
        // @todo 1.18 this.resp_l.mirror = true;
        // @todo 1.18
        // @todo 1.18 this.resp_r = new ModelRenderer(this, 36, 8);
        // @todo 1.18 this.resp_r.addBox(-3.0F, 0.0F, -2.5F, 3, 3, 3, s);
        // @todo 1.18 this.resp_r.setPos(-0.5F, -4.0F, -5.5F);
        // @todo 1.18 this.setRotation(resp_r, 0.0F, 0.7853981633974483F, 0.0F);
   
        // @todo 1.18 this.head.addChild(mask);
        // @todo 1.18 this.head.addChild(respirator);
        // @todo 1.18 this.respirator.addChild(resp_l);
        // @todo 1.18 this.respirator.addChild(resp_r);
        // @todo 1.18
        // @todo 1.18 //chestplate
        // @todo 1.18 this.body = new ModelRenderer(this, 0, 22);
        // @todo 1.18 this.body.addBox(-4.5F, 0.0F, -3.0F, 9, 9, 6, s);
        // @todo 1.18 this.body.setPos(0.0F, 0.0F, 0.0F);
        // @todo 1.18
        // @todo 1.18 this.arm_l = new ModelRenderer(this, 0, 37);
        // @todo 1.18 this.arm_l.addBox(-1.5F, -2.5F, -2.9F, 5, 8, 6, s);
        // @todo 1.18 this.arm_l.setPos(0.0F, 0.0F, 0.0F);
        // @todo 1.18 this.arm_l.mirror = true;
        // @todo 1.18
        // @todo 1.18 this.glove_l = new ModelRenderer(this, 22, 37);
        // @todo 1.18 this.glove_l.addBox(-1.5F, 5.5F, -2.5F, 5, 5, 5, s);
        // @todo 1.18 this.glove_l.setPos(0.0F, 0.0F, 0.0F);
        // @todo 1.18 this.glove_l.mirror = true;
        // @todo 1.18
        // @todo 1.18 this.arm_r = new ModelRenderer(this, 0, 37);
        // @todo 1.18 this.arm_r.addBox(-3.5F, -2.5F, -3.0F, 5, 8, 6, s);
        // @todo 1.18 this.arm_r.setPos(0.0F, 0.0F, 0.0F);
        // @todo 1.18
        // @todo 1.18 this.glove_r = new ModelRenderer(this, 22, 37);
        // @todo 1.18 this.glove_r.addBox(-3.5F, 5.5F, -2.5F, 5, 5, 5, s);
        // @todo 1.18 this.glove_r.setPos(0.0F, 0.0F, 0.0F);
        // @todo 1.18
        // @todo 1.18 this.body.addChild(body);
        // @todo 1.18 this.rightArm.addChild(arm_r);
        // @todo 1.18 this.rightArm.addChild(glove_r);
        // @todo 1.18 this.leftArm.addChild(arm_l);
        // @todo 1.18 this.leftArm.addChild(glove_l);

        // @todo 1.18 //legs
        // @todo 1.18
        // @todo 1.18 this.belt = new ModelRenderer(this, 0, 52);
        // @todo 1.18 this.belt.addBox(-4.5F, 9.0F, -3.0F, 9, 5, 6, s);
        // @todo 1.18 this.belt.setPos(0.0F, 0.0F, 0.0F);
        // @todo 1.18
        // @todo 1.18 this.leg_l = new ModelRenderer(this, 0, 63);
        // @todo 1.18 this.leg_l.addBox(-2.0F, -3.0F, -2.55F, 5, 9, 5, s);
        // @todo 1.18 this.leg_l.setPos(0.0F, 0.0F, 0.0F);
        // @todo 1.18
        // @todo 1.18 this.leg_r = new ModelRenderer(this, 0, 63);
        // @todo 1.18 this.leg_r.addBox(-3.0F, -3.0F, -2.55F, 5, 9, 5, s);
        // @todo 1.18 this.leg_r.setPos(0.0F, 0.0F, 0.0F);
        // @todo 1.18
        // @todo 1.18 this.rightLeg.addChild(leg_r);
        // @todo 1.18 this.leftLeg.addChild(leg_l);
        // @todo 1.18 this.body.addChild(belt);
        // @todo 1.18
        // @todo 1.18 //boots
        // @todo 1.18 bootsLeft = new ModelRenderer(this, 0, 0);
        // @todo 1.18 bootsRight = new ModelRenderer(this, 0, 0);
    
        // @todo 1.18 this.boot_l = new ModelRenderer(this, 0, 77);
        // @todo 1.18 this.boot_l.setPos(0,0,0);
        // @todo 1.18 this.boot_l.addBox(-2.0F, 6.0F, -2.5F, 5, 6, 5, s);
        // @todo 1.18
        // @todo 1.18 this.boot_r = new ModelRenderer(this, 0, 77);
        // @todo 1.18 this.boot_r.setPos(0, 0 ,0);
        // @todo 1.18 this.boot_r.addBox(-2.95F, 6.0F, -2.5F, 5, 6, 5, s);
    
        // @todo 1.18 this.bootsLeft.addChild(boot_l);
        // @todo 1.18 this.bootsRight.addChild(boot_r);
        // @todo 1.18
        // @todo 1.18 this.leftLeg.addChild(bootsLeft);
        // @todo 1.18 this.rightLeg.addChild(bootsRight);

    }

    // @todo 1.18 private void setRotation(ModelRenderer model, float x, float y, float z) {
    // @todo 1.18     model.xRot = x;
    // @todo 1.18     model.yRot = y;
    // @todo 1.18     model.zRot = z;
    // @todo 1.18 }

    public static HumanoidModel getModel(LivingEntity entity, ItemStack stack) {

        if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem)) {
            return null;
        }
        EquipmentSlot slot = ((ArmorItem) stack.getItem()).getSlot();

        PowerSuitModel armor;
        
        if (slot == EquipmentSlot.HEAD && modelHelm != null) {
            return modelHelm;
        } else if (slot == EquipmentSlot.CHEST && modelChest != null) {
            return modelChest;
        } else if (slot == EquipmentSlot.LEGS && modelLegs != null) {
            return modelLegs;
        } else if (slot == EquipmentSlot.FEET && modelBoots!= null) {
            return modelBoots;
        }

        armor = new PowerSuitModel(0.0625f);
        
        armor.head.visible = false;
        armor.body.visible = false;
        armor.leftArm.visible = false;
        armor.rightArm.visible = false;
        armor.leftLeg.visible = false;
        armor.rightLeg.visible = false;
        // @todo 1.18 armor.bootsLeft.visible = false;
        // @todo 1.18 armor.bootsRight.visible = false;

        switch (slot) {
            case HEAD:
                armor.head.visible = true;
                modelHelm = armor;
                break;
                
            case FEET:
                armor.rightLeg.visible = true;
                armor.leftLeg.visible = true;
                // @todo 1.18 armor.leg_l.visible = false;
                // @todo 1.18 armor.leg_r.visible = false;
                // @todo 1.18 armor.bootsLeft.visible = true;
                // @todo 1.18 armor.bootsRight.visible = true;
                modelBoots = armor;
                break;
                
            case LEGS:
                armor.leftLeg.visible = true;
                armor.rightLeg.visible = true;
                modelLegs = armor;
                break;
                
            case CHEST:
                armor.body.visible = true;
                armor.body.visible = true;
                armor.leftArm.visible = true;
                armor.rightArm.visible = true;
                modelChest = armor;
                break;
        }
        
        return armor;
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

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
