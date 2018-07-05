package mcjty.ariente.cables;

import mcjty.ariente.Ariente;
import mcjty.lib.client.RenderHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

@SideOnly(Side.CLIENT)
public class CableRenderer extends TileEntitySpecialRenderer<GenericCableTileEntity> {

    private Random random = new Random();
    private static ResourceLocation laserbeams[] = new ResourceLocation[4];
    static {
        laserbeams[0] = new ResourceLocation(Ariente.MODID, "textures/effects/laserbeam1.png");
        laserbeams[1] = new ResourceLocation(Ariente.MODID, "textures/effects/laserbeam2.png");
        laserbeams[2] = new ResourceLocation(Ariente.MODID, "textures/effects/laserbeam3.png");
        laserbeams[3] = new ResourceLocation(Ariente.MODID, "textures/effects/laserbeam4.png");
    }

    public static final List<BiConsumer<RenderHelper.Vector, BlockPos>> BEAM_RENDERERS_UP_DOWN = new ArrayList<>(4);
    public static final List<BiConsumer<RenderHelper.Vector, BlockPos>> BEAM_RENDERERS_EAST_WEST = new ArrayList<>(4);
    public static final List<BiConsumer<RenderHelper.Vector, BlockPos>> BEAM_RENDERERS_NORTH_SOUTH = new ArrayList<>(4);

    static {
        BEAM_RENDERERS_UP_DOWN.add((player, pos) -> {});
        BEAM_RENDERERS_UP_DOWN.add((player, pos) -> beam(player, pos, .5f, .5f, .5f, .5f, 0f ,.5f));
        BEAM_RENDERERS_UP_DOWN.add((player, pos) -> beam(player, pos, .5f, .5f, .5f, .5f, 1f ,.5f));
        BEAM_RENDERERS_UP_DOWN.add((player, pos) -> beam(player, pos, .5f, 0f, .5f, .5f, 1f ,.5f));

        BEAM_RENDERERS_EAST_WEST.add((player, pos) -> {});
        BEAM_RENDERERS_EAST_WEST.add((player, pos) -> beam(player, pos, .5f, .5f, .5f, 1f, .5f, .5f));
        BEAM_RENDERERS_EAST_WEST.add((player, pos) -> beam(player, pos, .5f, .5f, .5f, 0f, .5f, .5f));
        BEAM_RENDERERS_EAST_WEST.add((player, pos) -> beam(player, pos, 0f, .5f, .5f, 1f, .5f, .5f));

        BEAM_RENDERERS_NORTH_SOUTH.add((player, pos) -> {});
        BEAM_RENDERERS_NORTH_SOUTH.add((player, pos) -> beam(player, pos, .5f, .5f, .5f, .5f, .5f, 1f));
        BEAM_RENDERERS_NORTH_SOUTH.add((player, pos) -> beam(player, pos, .5f, .5f, .5f, .5f, .5f, 0f));
        BEAM_RENDERERS_NORTH_SOUTH.add((player, pos) -> beam(player, pos, .5f, .5f, 0f, .5f, .5f, 1f));
    }

    private static void beam(RenderHelper.Vector player, BlockPos tepos, float sx, float sy, float sz, float dx, float dy, float dz) {
        RenderHelper.drawBeam(
                new RenderHelper.Vector(tepos.getX()+sx, tepos.getY()+sy, tepos.getZ()+sz),
                new RenderHelper.Vector(tepos.getX()+dx, tepos.getY()+dy, tepos.getZ()+dz),
                player, .1f);
    }


    @Override
    public void render(GenericCableTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (true) {
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ONE);

            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayerSP p = mc.player;
            double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * partialTicks;
            double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * partialTicks;
            double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * partialTicks;

            RenderHelper.Vector player = new RenderHelper.Vector((float) doubleX, (float) doubleY + p.getEyeHeight(), (float) doubleZ);

            GlStateManager.pushMatrix();
            GlStateManager.translate(-doubleX, -doubleY, -doubleZ);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            // ----------------------------------------

            this.bindTexture(laserbeams[random.nextInt(4)]);


            IBlockState state = te.getWorld().getBlockState(te.getPos());
            Block block = state.getBlock();
            if (block instanceof GenericCableBlock) {
                int mask_ud = ((GenericCableBlock) block).getUpDownMask(state, te.getWorld(), te.getPos());
                int mask_ew = ((GenericCableBlock) block).getEastWestMask(state, te.getWorld(), te.getPos());
                int mask_ns = ((GenericCableBlock) block).getNorthSouthMask(state, te.getWorld(), te.getPos());

                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

                BEAM_RENDERERS_UP_DOWN.get(mask_ud).accept(player, te.getPos());
                BEAM_RENDERERS_NORTH_SOUTH.get(mask_ns).accept(player, te.getPos());
                BEAM_RENDERERS_EAST_WEST.get(mask_ew).accept(player, te.getPos());

                tessellator.draw();
            }


            GlStateManager.popMatrix();

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.depthMask(true);
        }
    }
}
