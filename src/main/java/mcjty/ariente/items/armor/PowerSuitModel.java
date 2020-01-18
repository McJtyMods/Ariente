package mcjty.ariente.items.armor;

import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.blocks.defense.ForceFieldRenderer;
import mcjty.ariente.blocks.defense.PanelInfo;
import mcjty.ariente.blocks.defense.PentakisDodecahedron;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.varia.Triangle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import static mcjty.ariente.blocks.defense.ForceFieldRenderer.FORCEFIELD;

public class PowerSuitModel extends BipedModel {

    public static PowerSuitModel modelHelm;
    public static PowerSuitModel modelChest;
    public static PowerSuitModel modelLegs;
    public static PowerSuitModel modelBoots;

    private static PanelInfo[] panelInfo = null;

    // Hazmat Suit

    public RendererModel mask;
    public RendererModel respirator;
    public RendererModel resp_l;
    public RendererModel resp_r;
    
    public RendererModel body;
    public RendererModel arm_l;
    public RendererModel arm_r;
    public RendererModel glove_l;
    public RendererModel glove_r;
    
    public RendererModel belt;
    public RendererModel leg_l;
    public RendererModel leg_r;
    
    private RendererModel bootsLeft;
    private RendererModel bootsRight;
    public RendererModel boot_l;
    public RendererModel boot_r;

    public PowerSuitModel(float scale) {
        super(scale);

    	this.textureWidth = 64;
        this.textureHeight = 128;
        float s = 0.01F;
    
        this.bipedHead.cubeList.clear();
        this.bipedHeadwear.cubeList.clear();
        this.bipedBody.cubeList.clear();
        this.bipedRightArm.cubeList.clear();
        this.bipedLeftArm.cubeList.clear();
        this.bipedLeftLeg.cubeList.clear();
        this.bipedRightLeg.cubeList.clear();
        
        //helmet
        this.mask = new RendererModel(this, 0, 0);
        this.mask.addBox(-4.5F, -9.0F, -4.5F, 9, 13, 9, s);
        this.mask.setRotationPoint(0.0F, 0.0F, 0.0F);
        
        this.respirator = new RendererModel(this, 36, 0);
        this.respirator.addBox(-2.5F, -5.0F, -5.5F, 5, 4, 4, s);
        this.respirator.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.setRotation(respirator, 0.5235987755982988F, 0.0F, 0.0F);
        
        this.resp_l = new RendererModel(this, 36, 8);
        this.resp_l.addBox(0.0F, 0.0F, -2.5F, 3, 3, 3, s);
        this.resp_l.setRotationPoint(0.5F, -4.0F, -5.5F);
        this.setRotation(resp_l, 0.0F, -0.7853981633974483F, 0.0F);
        this.resp_l.mirror = true;
        
        this.resp_r = new RendererModel(this, 36, 8);
        this.resp_r.addBox(-3.0F, 0.0F, -2.5F, 3, 3, 3, s);
        this.resp_r.setRotationPoint(-0.5F, -4.0F, -5.5F);
        this.setRotation(resp_r, 0.0F, 0.7853981633974483F, 0.0F);
   
        this.bipedHead.addChild(mask);
        this.bipedHead.addChild(respirator);
        this.respirator.addChild(resp_l);
        this.respirator.addChild(resp_r);
        
        //chestplate
        this.body = new RendererModel(this, 0, 22);
        this.body.addBox(-4.5F, 0.0F, -3.0F, 9, 9, 6, s);
        this.body.setRotationPoint(0.0F, 0.0F, 0.0F);
        
        this.arm_l = new RendererModel(this, 0, 37);
        this.arm_l.addBox(-1.5F, -2.5F, -2.9F, 5, 8, 6, s);
        this.arm_l.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.arm_l.mirror = true;
        
        this.glove_l = new RendererModel(this, 22, 37);
        this.glove_l.addBox(-1.5F, 5.5F, -2.5F, 5, 5, 5, s);
        this.glove_l.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.glove_l.mirror = true;
        
        this.arm_r = new RendererModel(this, 0, 37);
        this.arm_r.addBox(-3.5F, -2.5F, -3.0F, 5, 8, 6, s);
        this.arm_r.setRotationPoint(0.0F, 0.0F, 0.0F);
        
        this.glove_r = new RendererModel(this, 22, 37);
        this.glove_r.addBox(-3.5F, 5.5F, -2.5F, 5, 5, 5, s);
        this.glove_r.setRotationPoint(0.0F, 0.0F, 0.0F);
        
        this.bipedBody.addChild(body);
        this.bipedRightArm.addChild(arm_r);
        this.bipedRightArm.addChild(glove_r);
        this.bipedLeftArm.addChild(arm_l);
        this.bipedLeftArm.addChild(glove_l);

        //legs
        
        this.belt = new RendererModel(this, 0, 52);
        this.belt.addBox(-4.5F, 9.0F, -3.0F, 9, 5, 6, s);
        this.belt.setRotationPoint(0.0F, 0.0F, 0.0F);
        
        this.leg_l = new RendererModel(this, 0, 63);
        this.leg_l.addBox(-2.0F, -3.0F, -2.55F, 5, 9, 5, s);
        this.leg_l.setRotationPoint(0.0F, 0.0F, 0.0F);
        
        this.leg_r = new RendererModel(this, 0, 63);
        this.leg_r.addBox(-3.0F, -3.0F, -2.55F, 5, 9, 5, s);
        this.leg_r.setRotationPoint(0.0F, 0.0F, 0.0F);
        
        this.bipedRightLeg.addChild(leg_r);
        this.bipedLeftLeg.addChild(leg_l);
        this.bipedBody.addChild(belt);
        
        //boots
        bootsLeft = new RendererModel(this, 0, 0);
        bootsRight = new RendererModel(this, 0, 0);
    
        this.boot_l = new RendererModel(this, 0, 77);
        this.boot_l.setRotationPoint(0,0,0);
        this.boot_l.addBox(-2.0F, 6.0F, -2.5F, 5, 6, 5, s);
        
        this.boot_r = new RendererModel(this, 0, 77);
        this.boot_r.setRotationPoint(0, 0 ,0);
        this.boot_r.addBox(-2.95F, 6.0F, -2.5F, 5, 6, 5, s);
    
        this.bootsLeft.addChild(boot_l);
        this.bootsRight.addChild(boot_r);
        
        this.bipedLeftLeg.addChild(bootsLeft);
        this.bipedRightLeg.addChild(bootsRight);

    }

    private void setRotation(RendererModel model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    public static BipedModel getModel(LivingEntity entity, ItemStack stack) {

        if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem)) {
            return null;
        }
        EquipmentSlotType slot = ((ArmorItem) stack.getItem()).getEquipmentSlot();

        PowerSuitModel armor;
        
        if (slot == EquipmentSlotType.HEAD && modelHelm != null) {
            return modelHelm;
        } else if (slot == EquipmentSlotType.CHEST && modelChest != null) {
            return modelChest;
        } else if (slot == EquipmentSlotType.LEGS && modelLegs != null) {
            return modelLegs;
        } else if (slot == EquipmentSlotType.FEET && modelBoots!= null) {
            return modelBoots;
        }

        armor = new PowerSuitModel(0.0625f);
        
        armor.bipedHead.isHidden = true;
        armor.bipedBody.isHidden = true;
        armor.bipedLeftArm.isHidden = true;
        armor.bipedRightArm.isHidden = true;
        armor.bipedLeftLeg.isHidden = true;
        armor.bipedRightLeg.isHidden = true;
        armor.bootsLeft.isHidden = true;
        armor.bootsRight.isHidden = true;

        switch (slot) {
            case HEAD:
                armor.bipedHead.isHidden = false;
                modelHelm = armor;
                break;
                
            case FEET:
                armor.bipedRightLeg.isHidden = false;
                armor.bipedLeftLeg.isHidden = false;
                armor.leg_l.isHidden = true;
                armor.leg_r.isHidden = true;
                armor.bootsLeft.isHidden = false;
                armor.bootsRight.isHidden = false;
                modelBoots = armor;
                break;
                
            case LEGS:
                armor.bipedLeftLeg.isHidden = false;
                armor.bipedRightLeg.isHidden = false;
                modelLegs = armor;
                break;
                
            case CHEST:
                armor.bipedBody.isHidden = false;
                armor.body.isHidden = false;
                armor.bipedLeftArm.isHidden = false;
                armor.bipedRightArm.isHidden = false;
                modelChest = armor;
                break;
        }
        
        return armor;
    }
    
    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        
        if(entity instanceof LivingEntity)	{
            this.isSneak = entity.isSneaking();
            // @todo 1.14
//            this.isRiding = entity.isRiding();
            this.isChild = ((LivingEntity)entity).isChild();
            this.setLivingAnimations((LivingEntity)entity, limbSwing, limbSwingAmount, ageInTicks);
            this.setRotationAngles((LivingEntity) entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }

        if (this.isChild) {
            float f6 = 2.0F;
            GlStateManager.pushMatrix();
            GlStateManager.scalef(1.5F / f6, 1.5F / f6, 1.5F / f6);
            GlStateManager.translatef(0.0F, 16.0F * scale, 0.0F);
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableBlend();
            this.bipedHead.render(scale);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scalef(1.0F / f6, 1.0F / f6, 1.0F / f6);
            GlStateManager.translatef(0.0F, 24.0F * scale, 0.0F);
            this.bipedBody.render(scale);
            this.bipedRightArm.render(scale);
            this.bipedLeftArm.render(scale);
            this.bipedRightLeg.render(scale);
            this.bipedLeftLeg.render(scale);
            GlStateManager.popMatrix();
        } else {
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableBlend();
            this.bipedHead.render(scale);
            GlStateManager.disableBlend();
            this.bipedBody.render(scale);
            this.bipedRightArm.render(scale);
            this.bipedLeftArm.render(scale);
            this.bipedRightLeg.render(scale);
            this.bipedLeftLeg.render(scale);
        }

        if (this == modelChest) {   // @todo Proper test
            if (entity instanceof LivingEntity) {
                ItemStack chestStack = ((LivingEntity) entity).getItemStackFromSlot(EquipmentSlotType.CHEST);
                if (chestStack.getItem() == ModItems.powerSuitChest) {
                    if (ModuleSupport.hasWorkingUpgrade(chestStack, ArmorUpgradeType.FORCEFIELD)) {
                        ForceFieldRenderer.personalForcefields.put(new Vec3d(entity.prevPosX, entity.prevPosY, entity.prevPosZ), entity instanceof PlayerEntity);
                    }
                }
            }
        }

    }


    public static void renderForcefield(double posX, double posY, double posZ, float partialTicks) {
//        GlStateManager.pushMatrix();
//        GlStateManager.translate(-.75, 0, -.5);
        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.disableLightmap();
        GlStateManager.enableTexture();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);
        GlStateManager.enableDepthTest();
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 0.1f);
        mc.getTextureManager().bindTexture(FORCEFIELD);

        Tessellator t = Tessellator.getInstance();
        BufferBuilder builder = t.getBuffer();

        Entity renderViewEntity = Minecraft.getInstance().getRenderViewEntity();
        double dx = renderViewEntity.lastTickPosX + (renderViewEntity.posX - renderViewEntity.lastTickPosX) * partialTicks;
        double dy = renderViewEntity.lastTickPosY + (renderViewEntity.posY - renderViewEntity.lastTickPosY) * partialTicks;
        double dz = renderViewEntity.lastTickPosZ + (renderViewEntity.posZ - renderViewEntity.lastTickPosZ) * partialTicks;
        double x = posX + 0 - dx;
        double y = posY + .4 - dy;
        double z = posZ + 0 - dz;

        double scale = 1.2;

        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 0.2f);
        renderPanels(t, builder, x, y, z, scale, getPanelInfo(posX, posY, posZ, scale));

        GlStateManager.enableTexture();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
//        GlStateManager.popMatrix();
    }

    private static PanelInfo[] getPanelInfo(double posX, double posY, double posZ, double scale) {
        if (panelInfo == null) {
            panelInfo = new PanelInfo[PentakisDodecahedron.MAX_TRIANGLES];
        }
        for (int i = 0 ; i < PentakisDodecahedron.MAX_TRIANGLES ; i++) {
            Triangle triangle = PentakisDodecahedron.getTriangle(i);
            Vec3d offs = triangle.getMid().scale(scale);
            double x = posX+.5 + offs.x;
            double y = posY+.5 + offs.y;
            double z = posZ+.5 + offs.z;
            panelInfo[i] = new PanelInfo(i, x, y, z);
        }
        return panelInfo;
    }


    private static void renderPanels(Tessellator t, BufferBuilder builder, double x, double y, double z, double scale, PanelInfo[] panelInfo) {
        builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);

        for (PanelInfo info : panelInfo) {
            if (info != null) {
                renderPanel(x, y, z, scale, info);
            }
        }
        t.draw();
    }

    private static void renderPanel(double x, double y, double z, double scale, PanelInfo info) {
        ForceFieldRenderer.doRender(info, x, y, z, scale, 1.0f, 1.0f, 1.0f, 0.2f);
    }


}
