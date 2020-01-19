package mcjty.ariente.entities.fluxelevator;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.entity.Entity;

public class FluxElevatorModel extends EntityModel {
    public RendererModel sideModelsS1;
    public RendererModel sideModelsB;
    public RendererModel sideModels2;
    public RendererModel sideModels3;
    public RendererModel sideModels4;
    public RendererModel sideModelsT;
    public RendererModel sideModels5;
    public RendererModel sideModelsS4;
    public RendererModel sideModelsS3;
    public RendererModel sideModelsS2;

    public FluxElevatorModel() {
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.sideModels3 = new RendererModel(this, 0, 0);
        this.sideModels3.setRotationPoint(15.0F, 4.0F, 0.0F);
        this.sideModels3.addBox(-8.0F, -10.0F, 4.0F, 16, 9, 2, 0.0F);
        this.setRotateAngle(sideModels3, 0.0F, 1.5707963267948966F, 0.0F);
        this.sideModelsS1 = new RendererModel(this, 0, 0);
        this.sideModelsS1.setRotationPoint(-15.0F, 4.0F, 0.0F);
        this.sideModelsS1.addBox(6.5F, -19.5F, 5.0F, 2, 11, 2, 0.0F);
        this.setRotateAngle(sideModelsS1, 0.0F, 4.71238898038469F, 0.17453292519943295F);
        this.sideModels5 = new RendererModel(this, 0, 0);
        this.sideModels5.setRotationPoint(0.0F, 4.0F, 7.0F);
        this.sideModels5.addBox(-14.0F, -10.0F, 0.5F, 28, 9, 2, 0.0F);
        this.sideModelsS2 = new RendererModel(this, 0, 0);
        this.sideModelsS2.setRotationPoint(-15.0F, 4.0F, 0.0F);
        this.sideModelsS2.addBox(-8.5F, -19.5F, 5.0F, 2, 11, 2, 0.0F);
        this.setRotateAngle(sideModelsS2, 0.0F, 4.71238898038469F, 0.17453292519943295F);
        this.sideModelsS4 = new RendererModel(this, 0, 0);
        this.sideModelsS4.setRotationPoint(15.0F, 4.0F, 0.0F);
        this.sideModelsS4.addBox(-8.5F, -19.5F, -6.5F, 2, 11, 2, 0.0F);
        this.setRotateAngle(sideModelsS4, 0.0F, 4.71238898038469F, -0.17453292519943295F);
        this.sideModels2 = new RendererModel(this, 0, 0);
        this.sideModels2.setRotationPoint(-15.0F, 4.0F, 0.0F);
        this.sideModels2.addBox(-8.0F, -10.0F, 4.0F, 16, 9, 2, 0.0F);
        this.setRotateAngle(sideModels2, 0.0F, 4.71238898038469F, 0.0F);
        this.sideModels4 = new RendererModel(this, 0, 0);
        this.sideModels4.setRotationPoint(0.0F, 4.0F, -7.0F);
        this.sideModels4.addBox(-14.0F, -10.0F, 0.5F, 28, 9, 2, 0.0F);
        this.setRotateAngle(sideModels4, 0.0F, 3.141592653589793F, 0.0F);
        this.sideModelsS3 = new RendererModel(this, 0, 0);
        this.sideModelsS3.setRotationPoint(15.0F, 4.0F, 0.0F);
        this.sideModelsS3.addBox(6.5F, -19.5F, -6.5F, 2, 11, 2, 0.0F);
        this.setRotateAngle(sideModelsS3, 0.0F, 4.71238898038469F, -0.17453292519943295F);
        this.sideModelsB = new RendererModel(this, 0, 10);
        this.sideModelsB.setRotationPoint(0.0F, 4.0F, 0.0F);
        this.sideModelsB.addBox(-14.0F, -8.0F, -1.0F, 28, 16, 2, 0.0F);
        this.setRotateAngle(sideModelsB, 1.5707963267948966F, 0.0F, 0.0F);
        this.sideModelsT = new RendererModel(this, 0, 10);
        this.sideModelsT.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.sideModelsT.addBox(-14.0F, -8.0F, 15.0F, 28, 16, 2, 0.0F);
        this.setRotateAngle(sideModelsT, 1.5707963267948966F, 0.0F, 0.0F);
    }


    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        this.sideModels3.render(f5);
        this.sideModelsS1.render(f5);
        GlStateManager.pushMatrix();
        GlStateManager.translatef(this.sideModels5.offsetX, this.sideModels5.offsetY, this.sideModels5.offsetZ);
        GlStateManager.translatef(this.sideModels5.rotationPointX * f5, this.sideModels5.rotationPointY * f5, this.sideModels5.rotationPointZ * f5);
        GlStateManager.scaled(1.5D, 1.0D, 1.0D);
        GlStateManager.translatef(-this.sideModels5.offsetX, -this.sideModels5.offsetY, -this.sideModels5.offsetZ);
        GlStateManager.translatef(-this.sideModels5.rotationPointX * f5, -this.sideModels5.rotationPointY * f5, -this.sideModels5.rotationPointZ * f5);
        this.sideModels5.render(f5);
        GlStateManager.popMatrix();
        this.sideModelsS2.render(f5);
        this.sideModelsS4.render(f5);
        this.sideModels2.render(f5);
        GlStateManager.pushMatrix();
        GlStateManager.translatef(this.sideModels4.offsetX, this.sideModels4.offsetY, this.sideModels4.offsetZ);
        GlStateManager.translatef(this.sideModels4.rotationPointX * f5, this.sideModels4.rotationPointY * f5, this.sideModels4.rotationPointZ * f5);
        GlStateManager.scaled(1.5D, 1.0D, 1.0D);
        GlStateManager.translatef(-this.sideModels4.offsetX, -this.sideModels4.offsetY, -this.sideModels4.offsetZ);
        GlStateManager.translatef(-this.sideModels4.rotationPointX * f5, -this.sideModels4.rotationPointY * f5, -this.sideModels4.rotationPointZ * f5);
        this.sideModels4.render(f5);
        GlStateManager.popMatrix();
        this.sideModelsS3.render(f5);
        GlStateManager.pushMatrix();
        GlStateManager.translatef(this.sideModelsB.offsetX, this.sideModelsB.offsetY, this.sideModelsB.offsetZ);
        GlStateManager.translatef(this.sideModelsB.rotationPointX * f5, this.sideModelsB.rotationPointY * f5, this.sideModelsB.rotationPointZ * f5);
        GlStateManager.scaled(1.5D, 1.0D, 1.2D);
        GlStateManager.translatef(-this.sideModelsB.offsetX, -this.sideModelsB.offsetY, -this.sideModelsB.offsetZ);
        GlStateManager.translatef(-this.sideModelsB.rotationPointX * f5, -this.sideModelsB.rotationPointY * f5, -this.sideModelsB.rotationPointZ * f5);
        this.sideModelsB.render(f5);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.translatef(this.sideModelsT.offsetX, this.sideModelsT.offsetY, this.sideModelsT.offsetZ);
        GlStateManager.translatef(this.sideModelsT.rotationPointX * f5, this.sideModelsT.rotationPointY * f5, this.sideModelsT.rotationPointZ * f5);
        GlStateManager.scaled(1.35D, 1.0D, 1.1D);
        GlStateManager.translatef(-this.sideModelsT.offsetX, -this.sideModelsT.offsetY, -this.sideModelsT.offsetZ);
        GlStateManager.translatef(-this.sideModelsT.rotationPointX * f5, -this.sideModelsT.rotationPointY * f5, -this.sideModelsT.rotationPointZ * f5);
        this.sideModelsT.render(f5);
        GlStateManager.popMatrix();
    }

    /**
     * This is a helper function from Tabula to set the rotation of model parts
     */
    public void setRotateAngle(RendererModel modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
