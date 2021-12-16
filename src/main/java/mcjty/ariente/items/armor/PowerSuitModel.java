package mcjty.ariente.items.armor;

import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.blocks.defense.ForceFieldRenderer;
import mcjty.ariente.blocks.defense.PanelInfo;
import mcjty.ariente.blocks.defense.PentakisDodecahedron;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.varia.Triangle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

import static mcjty.ariente.blocks.defense.ForceFieldRenderer.FORCEFIELD;

public class PowerSuitModel extends BipedModel<LivingEntity> {

    public static PowerSuitModel modelHelm;
    public static PowerSuitModel modelChest;
    public static PowerSuitModel modelLegs;
    public static PowerSuitModel modelBoots;

    private static PanelInfo[] panelInfo = null;

    // Hazmat Suit

    public ModelRenderer mask;
    public ModelRenderer respirator;
    public ModelRenderer resp_l;
    public ModelRenderer resp_r;
    
    public ModelRenderer body;
    public ModelRenderer arm_l;
    public ModelRenderer arm_r;
    public ModelRenderer glove_l;
    public ModelRenderer glove_r;
    
    public ModelRenderer belt;
    public ModelRenderer leg_l;
    public ModelRenderer leg_r;
    
    private ModelRenderer bootsLeft;
    private ModelRenderer bootsRight;
    public ModelRenderer boot_l;
    public ModelRenderer boot_r;

    public PowerSuitModel(float scale) {
        super(scale);

    	this.texWidth = 64;
        this.texHeight = 128;
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
        this.mask = new ModelRenderer(this, 0, 0);
        this.mask.addBox(-4.5F, -9.0F, -4.5F, 9, 13, 9, s);
        this.mask.setPos(0.0F, 0.0F, 0.0F);
        
        this.respirator = new ModelRenderer(this, 36, 0);
        this.respirator.addBox(-2.5F, -5.0F, -5.5F, 5, 4, 4, s);
        this.respirator.setPos(0.0F, 0.0F, 0.0F);
        this.setRotation(respirator, 0.5235987755982988F, 0.0F, 0.0F);
        
        this.resp_l = new ModelRenderer(this, 36, 8);
        this.resp_l.addBox(0.0F, 0.0F, -2.5F, 3, 3, 3, s);
        this.resp_l.setPos(0.5F, -4.0F, -5.5F);
        this.setRotation(resp_l, 0.0F, -0.7853981633974483F, 0.0F);
        this.resp_l.mirror = true;
        
        this.resp_r = new ModelRenderer(this, 36, 8);
        this.resp_r.addBox(-3.0F, 0.0F, -2.5F, 3, 3, 3, s);
        this.resp_r.setPos(-0.5F, -4.0F, -5.5F);
        this.setRotation(resp_r, 0.0F, 0.7853981633974483F, 0.0F);
   
        this.head.addChild(mask);
        this.head.addChild(respirator);
        this.respirator.addChild(resp_l);
        this.respirator.addChild(resp_r);
        
        //chestplate
        this.body = new ModelRenderer(this, 0, 22);
        this.body.addBox(-4.5F, 0.0F, -3.0F, 9, 9, 6, s);
        this.body.setPos(0.0F, 0.0F, 0.0F);
        
        this.arm_l = new ModelRenderer(this, 0, 37);
        this.arm_l.addBox(-1.5F, -2.5F, -2.9F, 5, 8, 6, s);
        this.arm_l.setPos(0.0F, 0.0F, 0.0F);
        this.arm_l.mirror = true;
        
        this.glove_l = new ModelRenderer(this, 22, 37);
        this.glove_l.addBox(-1.5F, 5.5F, -2.5F, 5, 5, 5, s);
        this.glove_l.setPos(0.0F, 0.0F, 0.0F);
        this.glove_l.mirror = true;
        
        this.arm_r = new ModelRenderer(this, 0, 37);
        this.arm_r.addBox(-3.5F, -2.5F, -3.0F, 5, 8, 6, s);
        this.arm_r.setPos(0.0F, 0.0F, 0.0F);
        
        this.glove_r = new ModelRenderer(this, 22, 37);
        this.glove_r.addBox(-3.5F, 5.5F, -2.5F, 5, 5, 5, s);
        this.glove_r.setPos(0.0F, 0.0F, 0.0F);
        
        this.body.addChild(body);
        this.rightArm.addChild(arm_r);
        this.rightArm.addChild(glove_r);
        this.leftArm.addChild(arm_l);
        this.leftArm.addChild(glove_l);

        //legs
        
        this.belt = new ModelRenderer(this, 0, 52);
        this.belt.addBox(-4.5F, 9.0F, -3.0F, 9, 5, 6, s);
        this.belt.setPos(0.0F, 0.0F, 0.0F);
        
        this.leg_l = new ModelRenderer(this, 0, 63);
        this.leg_l.addBox(-2.0F, -3.0F, -2.55F, 5, 9, 5, s);
        this.leg_l.setPos(0.0F, 0.0F, 0.0F);
        
        this.leg_r = new ModelRenderer(this, 0, 63);
        this.leg_r.addBox(-3.0F, -3.0F, -2.55F, 5, 9, 5, s);
        this.leg_r.setPos(0.0F, 0.0F, 0.0F);
        
        this.rightLeg.addChild(leg_r);
        this.leftLeg.addChild(leg_l);
        this.body.addChild(belt);
        
        //boots
        bootsLeft = new ModelRenderer(this, 0, 0);
        bootsRight = new ModelRenderer(this, 0, 0);
    
        this.boot_l = new ModelRenderer(this, 0, 77);
        this.boot_l.setPos(0,0,0);
        this.boot_l.addBox(-2.0F, 6.0F, -2.5F, 5, 6, 5, s);
        
        this.boot_r = new ModelRenderer(this, 0, 77);
        this.boot_r.setPos(0, 0 ,0);
        this.boot_r.addBox(-2.95F, 6.0F, -2.5F, 5, 6, 5, s);
    
        this.bootsLeft.addChild(boot_l);
        this.bootsRight.addChild(boot_r);
        
        this.leftLeg.addChild(bootsLeft);
        this.rightLeg.addChild(bootsRight);

    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.xRot = x;
        model.yRot = y;
        model.zRot = z;
    }

    public static BipedModel getModel(LivingEntity entity, ItemStack stack) {

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
        armor.bootsLeft.visible = false;
        armor.bootsRight.visible = false;

        switch (slot) {
            case HEAD:
                armor.head.visible = true;
                modelHelm = armor;
                break;
                
            case FEET:
                armor.rightLeg.visible = true;
                armor.leftLeg.visible = true;
                armor.leg_l.visible = false;
                armor.leg_r.visible = false;
                armor.bootsLeft.visible = true;
                armor.bootsRight.visible = true;
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
        GlStateManager._disableLighting();
        GlStateManager._enableBlend();
        GlStateManager._depthMask(false);
        GlStateManager._enableDepthTest();
        GlStateManager._color4f(1.0f, 1.0f, 1.0f, 0.1f);
        mc.getTextureManager().bind(FORCEFIELD);

        Tessellator t = Tessellator.getInstance();
        BufferBuilder builder = t.getBuilder();

        Entity renderViewEntity = Minecraft.getInstance().getCameraEntity();
        double dx = renderViewEntity.xOld + (renderViewEntity.getX() - renderViewEntity.xOld) * partialTicks;
        double dy = renderViewEntity.yOld + (renderViewEntity.getY() - renderViewEntity.yOld) * partialTicks;
        double dz = renderViewEntity.zOld + (renderViewEntity.getZ() - renderViewEntity.zOld) * partialTicks;
        double x = posX + 0 - dx;
        double y = posY + .4 - dy;
        double z = posZ + 0 - dz;

        double scale = 1.2;

        GlStateManager._color4f(1.0f, 1.0f, 1.0f, 0.2f);
        renderPanels(t, builder, x, y, z, scale, getPanelInfo(posX, posY, posZ, scale));

        GlStateManager._enableTexture();
        GlStateManager._depthMask(true);
        GlStateManager._enableLighting();
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


    private static void renderPanels(Tessellator t, BufferBuilder builder, double x, double y, double z, double scale, PanelInfo[] panelInfo) {
        builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);

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
