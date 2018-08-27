package mcjty.ariente.items.armor;

import mcjty.ariente.blocks.defense.ForceFieldRenderer;
import mcjty.ariente.blocks.defense.PanelInfo;
import mcjty.ariente.blocks.defense.PentakisDodecahedron;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.items.modules.ArmorUpgradeType;
import mcjty.ariente.varia.Triangle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import static mcjty.ariente.blocks.defense.ForceFieldRenderer.FORCEFIELD;

public class PowerSuitModel extends ModelBiped {

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
        this.mask = new ModelRenderer(this, 0, 0);
        this.mask.addBox(-4.5F, -9.0F, -4.5F, 9, 13, 9, s);
        this.mask.setRotationPoint(0.0F, 0.0F, 0.0F);
        
        this.respirator = new ModelRenderer(this, 36, 0);
        this.respirator.addBox(-2.5F, -5.0F, -5.5F, 5, 4, 4, s);
        this.respirator.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.setRotation(respirator, 0.5235987755982988F, 0.0F, 0.0F);
        
        this.resp_l = new ModelRenderer(this, 36, 8);
        this.resp_l.addBox(0.0F, 0.0F, -2.5F, 3, 3, 3, s);
        this.resp_l.setRotationPoint(0.5F, -4.0F, -5.5F);
        this.setRotation(resp_l, 0.0F, -0.7853981633974483F, 0.0F);
        this.resp_l.mirror = true;
        
        this.resp_r = new ModelRenderer(this, 36, 8);
        this.resp_r.addBox(-3.0F, 0.0F, -2.5F, 3, 3, 3, s);
        this.resp_r.setRotationPoint(-0.5F, -4.0F, -5.5F);
        this.setRotation(resp_r, 0.0F, 0.7853981633974483F, 0.0F);
   
        this.bipedHead.addChild(mask);
        this.bipedHead.addChild(respirator);
        this.respirator.addChild(resp_l);
        this.respirator.addChild(resp_r);
        
        //chestplate
        this.body = new ModelRenderer(this, 0, 22);
        this.body.addBox(-4.5F, 0.0F, -3.0F, 9, 9, 6, s);
        this.body.setRotationPoint(0.0F, 0.0F, 0.0F);
        
        this.arm_l = new ModelRenderer(this, 0, 37);
        this.arm_l.addBox(-1.5F, -2.5F, -2.9F, 5, 8, 6, s);
        this.arm_l.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.arm_l.mirror = true;
        
        this.glove_l = new ModelRenderer(this, 22, 37);
        this.glove_l.addBox(-1.5F, 5.5F, -2.5F, 5, 5, 5, s);
        this.glove_l.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.glove_l.mirror = true;
        
        this.arm_r = new ModelRenderer(this, 0, 37);
        this.arm_r.addBox(-3.5F, -2.5F, -3.0F, 5, 8, 6, s);
        this.arm_r.setRotationPoint(0.0F, 0.0F, 0.0F);
        
        this.glove_r = new ModelRenderer(this, 22, 37);
        this.glove_r.addBox(-3.5F, 5.5F, -2.5F, 5, 5, 5, s);
        this.glove_r.setRotationPoint(0.0F, 0.0F, 0.0F);
        
        this.bipedBody.addChild(body);
        this.bipedRightArm.addChild(arm_r);
        this.bipedRightArm.addChild(glove_r);
        this.bipedLeftArm.addChild(arm_l);
        this.bipedLeftArm.addChild(glove_l);

        //legs
        
        this.belt = new ModelRenderer(this, 0, 52);
        this.belt.addBox(-4.5F, 9.0F, -3.0F, 9, 5, 6, s);
        this.belt.setRotationPoint(0.0F, 0.0F, 0.0F);
        
        this.leg_l = new ModelRenderer(this, 0, 63);
        this.leg_l.addBox(-2.0F, -3.0F, -2.55F, 5, 9, 5, s);
        this.leg_l.setRotationPoint(0.0F, 0.0F, 0.0F);
        
        this.leg_r = new ModelRenderer(this, 0, 63);
        this.leg_r.addBox(-3.0F, -3.0F, -2.55F, 5, 9, 5, s);
        this.leg_r.setRotationPoint(0.0F, 0.0F, 0.0F);
        
        this.bipedRightLeg.addChild(leg_r);
        this.bipedLeftLeg.addChild(leg_l);
        this.bipedBody.addChild(belt);
        
        //boots
        bootsLeft = new ModelRenderer(this, 0, 0);
        bootsRight = new ModelRenderer(this, 0, 0);
    
        this.boot_l = new ModelRenderer(this, 0, 77);
        this.boot_l.setRotationPoint(0,0,0);
        this.boot_l.addBox(-2.0F, 6.0F, -2.5F, 5, 6, 5, s);
        
        this.boot_r = new ModelRenderer(this, 0, 77);
        this.boot_r.setRotationPoint(0, 0 ,0);
        this.boot_r.addBox(-2.95F, 6.0F, -2.5F, 5, 6, 5, s);
    
        this.bootsLeft.addChild(boot_l);
        this.bootsRight.addChild(boot_r);
        
        this.bipedLeftLeg.addChild(bootsLeft);
        this.bipedRightLeg.addChild(bootsRight);

    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    public static ModelBiped getModel(EntityLivingBase entity, ItemStack stack) {

        if (stack.isEmpty() || !(stack.getItem() instanceof ItemArmor)) {
            return null;
        }
        EntityEquipmentSlot slot = ((ItemArmor) stack.getItem()).armorType;

        PowerSuitModel armor;
        
        if (slot == EntityEquipmentSlot.HEAD && modelHelm != null) {
            return modelHelm;
        } else if (slot == EntityEquipmentSlot.CHEST && modelChest != null) {
            return modelChest;
        } else if (slot == EntityEquipmentSlot.LEGS && modelLegs != null) {
            return modelLegs;
        } else if (slot == EntityEquipmentSlot.FEET && modelBoots!= null) {
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
        
        if(entity instanceof EntityLivingBase)	{
            this.isSneak = entity.isSneaking();
            this.isRiding = entity.isRiding();
            this.isChild = ((EntityLivingBase)entity).isChild();
            this.setLivingAnimations((EntityLivingBase)entity, limbSwing, limbSwingAmount, ageInTicks);
        }
        
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        
        if (this.isChild) {
            float f6 = 2.0F;
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.5F / f6, 1.5F / f6, 1.5F / f6);
            GlStateManager.translate(0.0F, 16.0F * scale, 0.0F);
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableBlend();
            this.bipedHead.render(scale);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.0F / f6, 1.0F / f6, 1.0F / f6);
            GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
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
            if (entity instanceof EntityLivingBase) {
                ItemStack chestStack = ((EntityLivingBase) entity).getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                if (chestStack.getItem() == ModItems.powerSuitChest) {
                    if (PowerSuit.hasWorkingUpgrade(chestStack, ArmorUpgradeType.FORCEFIELD)) {
                        ForceFieldRenderer.personalForcefields.put(new Vec3d(entity.prevPosX, entity.prevPosY, entity.prevPosZ), entity instanceof EntityPlayer);
                    }
                }
            }
        }

    }


    public static void renderForcefield(double posX, double posY, double posZ, float partialTicks) {
//        GlStateManager.pushMatrix();
//        GlStateManager.translate(-.75, 0, -.5);
        Minecraft mc = Minecraft.getMinecraft();
        mc.entityRenderer.disableLightmap();
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);
        GlStateManager.enableDepth();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 0.1f);
        mc.renderEngine.bindTexture(FORCEFIELD);

        Tessellator t = Tessellator.getInstance();
        BufferBuilder builder = t.getBuffer();

        Entity renderViewEntity = Minecraft.getMinecraft().getRenderViewEntity();
        double dx = renderViewEntity.lastTickPosX + (renderViewEntity.posX - renderViewEntity.lastTickPosX) * partialTicks;
        double dy = renderViewEntity.lastTickPosY + (renderViewEntity.posY - renderViewEntity.lastTickPosY) * partialTicks;
        double dz = renderViewEntity.lastTickPosZ + (renderViewEntity.posZ - renderViewEntity.lastTickPosZ) * partialTicks;
        double x = posX + 0 - dx;
        double y = posY + .4 - dy;
        double z = posZ + 0 - dz;

        double scale = 1.2;

        GlStateManager.color(1.0f, 1.0f, 1.0f, 0.2f);
        renderPanels(t, builder, x, y, z, scale, getPanelInfo(posX, posY, posZ, scale));

        GlStateManager.enableTexture2D();
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
