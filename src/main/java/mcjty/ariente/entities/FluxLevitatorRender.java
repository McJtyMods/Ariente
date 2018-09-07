package mcjty.ariente.entities;

import mcjty.ariente.gui.HoloGuiEntity;
import mcjty.ariente.gui.HoloGuiEntityRender;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FluxLevitatorRender extends Render<FluxLevitatorEntity> {

    private static final ResourceLocation TEXTURES = new ResourceLocation("ariente:textures/entity/flux_levitator.png");
    protected ModelBase model = new FluxLevitatorModel();

    public FluxLevitatorRender(RenderManager renderManagerIn) {
        super(renderManagerIn);
        this.shadowSize = 0.5F;
    }

    @Override
    public void doRender(FluxLevitatorEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
//        long i = entity.getEntityId() * 493286711L;
//        i = i * i * 4392167121L + i * 98761L;
//        float fx = (((i >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
//        float fy = (((i >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
//        float fz = (((i >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
//        GlStateManager.translate(fx, fy, fz);

        double interX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double interY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double interZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
        Vec3d vec3d = entity.getPos(interX, interY, interZ);
        float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;


        if (vec3d != null) {
            Vec3d vec3d1 = entity.getPosOffset(interX, interY, interZ, 0.3D);
            Vec3d vec3d2 = entity.getPosOffset(interX, interY, interZ, -0.3D);

            if (vec3d1 == null) {
                vec3d1 = vec3d;
            }

            if (vec3d2 == null) {
                vec3d2 = vec3d;
            }

            x += vec3d.x - interX;
            y += (vec3d1.y + vec3d2.y) / 2.0D - interY;
            z += vec3d.z - interZ;
            Vec3d vec3d3 = vec3d2.addVector(-vec3d1.x, -vec3d1.y, -vec3d1.z);

            if (vec3d3.lengthVector() != 0.0D) {
                vec3d3 = vec3d3.normalize();
                entityYaw = (float) (Math.atan2(vec3d3.z, vec3d3.x) * 180.0D / Math.PI);
                pitch = (float) (Math.atan(vec3d3.y) * 73.0D);
            }
        }

        GlStateManager.translate((float) x, (float) y + 0.375F, (float) z);
        GlStateManager.rotate(180.0F - entityYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-pitch, 0.0F, 0.0F, 1.0F);
        float f5 = entity.getRollingAmplitude() - partialTicks;
        float f6 = entity.getDamage() - partialTicks;

        if (f6 < 0.0F) {
            f6 = 0.0F;
        }

        if (f5 > 0.0F) {
            GlStateManager.rotate(MathHelper.sin(f5) * f5 * f6 / 10.0F * entity.getRollingDirection(), 1.0F, 0.0F, 0.0F);
        }

        this.bindEntityTexture(entity);

        if (this.renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }

        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
        this.model.render(entity, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);

        HoloGuiEntity holoGui = entity.getHoloGuiFront();
        if (holoGui != null) {
            GlStateManager.scale(-1.0F, -1.0F, 1.0F);
            HoloGuiEntityRender.doActualRender(holoGui, 1, 0, 0, -90);
        }
        holoGui = entity.getHoloGuiBack();
        if (holoGui != null) {
            GlStateManager.scale(-1.0F, -1.0F, 1.0F);
            HoloGuiEntityRender.doActualRender(holoGui, -1, 0, 0, 90);
        }


        GlStateManager.popMatrix();

        if (this.renderOutlines) {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(FluxLevitatorEntity entity) {
        return TEXTURES;
    }

    protected void renderCartContents(FluxLevitatorEntity p_188319_1_, float partialTicks, IBlockState p_188319_3_) {
        GlStateManager.pushMatrix();
        Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlockBrightness(p_188319_3_, p_188319_1_.getBrightness());
        GlStateManager.popMatrix();
    }


    public static class Factory implements IRenderFactory<FluxLevitatorEntity> {

        @Override
        public Render<? super FluxLevitatorEntity> createRenderFor(RenderManager manager) {
            return new FluxLevitatorRender(manager);
        }

    }
}