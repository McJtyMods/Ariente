package mcjty.ariente.cables;

import mcjty.ariente.Ariente;
import mcjty.lib.client.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class CableRenderer extends TileEntitySpecialRenderer<GenericCableTileEntity> {

    Random random = new Random();

    ResourceLocation laserbeams[] = new ResourceLocation[4];
    public CableRenderer() {
        laserbeams[0] = new ResourceLocation(Ariente.MODID, "textures/effects/laserbeam1.png");
        laserbeams[1] = new ResourceLocation(Ariente.MODID, "textures/effects/laserbeam2.png");
        laserbeams[2] = new ResourceLocation(Ariente.MODID, "textures/effects/laserbeam3.png");
        laserbeams[3] = new ResourceLocation(Ariente.MODID, "textures/effects/laserbeam4.png");
    }

    @Override
    public void render(GenericCableTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (true) {
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ONE);

//            GlStateManager.pushMatrix();
//            GlStateManager.translate((float) x + 0.5F, (float) y + 0.85F, (float) z + 0.5F);
//            this.bindTexture(halo);
//            RenderHelper.renderBillboardQuadBright(1.0f);
//            GlStateManager.popMatrix();

            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayerSP p = mc.player;
            double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * partialTicks;
            double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * partialTicks;
            double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * partialTicks;

            RenderHelper.Vector start = new RenderHelper.Vector(te.getPos().getX() + .5f, te.getPos().getY() + .5f + .3f, te.getPos().getZ() + .5f);
            RenderHelper.Vector player = new RenderHelper.Vector((float) doubleX, (float) doubleY + p.getEyeHeight(), (float) doubleZ);

            GlStateManager.pushMatrix();
            GlStateManager.translate(-doubleX, -doubleY, -doubleZ);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            // ----------------------------------------

            this.bindTexture(laserbeams[random.nextInt(4)]);

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
//            tessellator.setBrightness(240);

            BlockPos relative = new BlockPos(0, 0, 0);
            BlockPos destination = new BlockPos(relative.getX() + te.getPos().getX(), relative.getY() + te.getPos().getY(), relative.getZ() + te.getPos().getZ());
            RenderHelper.Vector end = new RenderHelper.Vector(destination.getX() + .5f, destination.getY() + .5f, destination.getZ() + .5f);
            RenderHelper.drawBeam(start, end, player, .1f);

            tessellator.draw();

            GlStateManager.popMatrix();

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }
    }
}
