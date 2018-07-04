package mcjty.ariente.blocks.utility;

import mcjty.lib.blocks.GenericBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class StorageRenderer extends TileEntitySpecialRenderer<StorageTile> {

//    private static final ResourceLocation texture = new ResourceLocation(Ariente.MODID, "textures/blocks/gearSwapperFront.png");

    //    private static int xx[] = new int[] { 9, 40, 9, 40 };
//    private static int yy[] = new int[] { 7, 7, 36, 36 };
    private static int xx[] = new int[]{11, 36, 11, 36};
    private static int yy[] = new int[]{9, 9, 34, 34};

    @Override
    public void render(StorageTile te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

        RayTraceResult mouseOver = Minecraft.getMinecraft().objectMouseOver;
        int index;
        if (mouseOver != null && te.getPos().equals(mouseOver.getBlockPos())) {
            index = StorageTile.getSlot(mouseOver, te.getWorld());
        } else {
            index = -2;
        }

        IBlockState state = te.getWorld().getBlockState(te.getPos());
        Block block = state.getBlock();
        if (!(block instanceof GenericBlock)) {
            return;
        }

        Minecraft.getMinecraft().entityRenderer.disableLightmap();

        GenericBlock gb = (GenericBlock) block;

        GlStateManager.pushMatrix();
        EnumFacing facing = gb.getFrontDirection(gb.getRotationType(), state);

        GlStateManager.translate((float) x + 0.5F, (float) y + 0.75F, (float) z + 0.5F);

        if (facing == EnumFacing.UP) {
            GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.0F, 0.0F, -0.68F);
        } else if (facing == EnumFacing.DOWN) {
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.0F, 0.0F, -.184F);
        } else {
            float rotY = 0.0F;
            if (facing == EnumFacing.NORTH) {
                rotY = 180.0F;
            } else if (facing == EnumFacing.WEST) {
                rotY = 90.0F;
            } else if (facing == EnumFacing.EAST) {
                rotY = -90.0F;
            }
            GlStateManager.rotate(-rotY, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, -0.2500F, -0.4375F);
        }

        GlStateManager.translate(0.0F, 0.0F, 0.9F);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();

        renderSlotHilight(index);
        renderSlots(te);

        GlStateManager.popMatrix();
        Minecraft.getMinecraft().entityRenderer.enableLightmap();
    }

    private void renderSlotHilight(int index) {
        GlStateManager.pushMatrix();

        float factor = 2.0f;
        float f3 = 0.0075F;
        GlStateManager.translate(-0.5F, 0.5F, -0.155F);
        GlStateManager.scale(f3 * factor, -f3 * factor, f3);
        GlStateManager.disableLighting();

        for (int i = 0; i < 4; i++) {
//            Gui.drawRect(xx[i] - 4, yy[i] - 4, xx[i] + 22, yy[i] - 3, 0xff222222);
//            Gui.drawRect(xx[i] - 4, yy[i] + 21, xx[i] + 22, yy[i] + 22, 0xff222222);
//            Gui.drawRect(xx[i] - 4, yy[i] - 4, xx[i] - 3, yy[i] + 22, 0xff222222);
//            Gui.drawRect(xx[i] + 21, yy[i] - 4, xx[i] + 22, yy[i] + 22, 0xff222222);
            if (index == i) {
                Gui.drawRect(xx[i] - 3, yy[i] - 3, xx[i] + 21, yy[i] + 21, index == i ? 0x55666666 : 0x55000000);
            }
        }

        GlStateManager.popMatrix();
    }

    private void renderSlots(StorageTile te) {
        RenderHelper.enableGUIStandardItemLighting();

        float factor = 2.0f;
        float f3 = 0.0075F;
        GL11.glTranslatef(-0.5F, 0.5F, -0.15F);
        GL11.glScalef(f3 * factor, -f3 * factor, 0.0001f);

        RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();

        for (int i = 0; i < StorageTile.STACKS; i++) {
            ItemStack stack = te.getTotalStack(i);
            if (!stack.isEmpty()) {
                itemRender.renderItemAndEffectIntoGUI(stack, xx[i], yy[i]);
            }
        }

        GL11.glTranslatef(0F, 0F, 500F);
        GlStateManager.scale(.5, .5, .5);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        for (int i = 0; i < StorageTile.STACKS; i++) {
            ItemStack stack = te.getTotalStack(i);
            if (!stack.isEmpty()) {
                renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRenderer, stack, xx[i] * 2 + 15, yy[i] * 2 + 16, getSize(stack.getCount()));
            }
        }

        RenderHelper.enableStandardItemLighting();
    }


    private static String getSize(int size) {
        if (size <= 1) {
            return "";
        } else if (size < 10000) {
            return String.valueOf(size);
        } else if (size < 1000000) {
            return String.valueOf(size / 1000) + "k";
        } else if (size < 1000000000) {
            return String.valueOf(size / 1000000) + "m";
        } else {
            return String.valueOf(size / 1000000000) + "g";
        }
    }

    private static void renderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, @Nullable String text) {
        if (!stack.isEmpty()) {
            if (stack.getCount() != 1 || text != null) {
                String s = text == null ? String.valueOf(stack.getCount()) : text;
                GlStateManager.disableLighting();
                GlStateManager.enableDepth();
//                GlStateManager.disableDepth();
                GlStateManager.disableBlend();
                fr.drawString(s, (float) (xPosition + 19 - 2 - fr.getStringWidth(s)), (float) (yPosition + 6 + 3), 16777215, false);
//                fr.drawStringWithShadow(s, (float) (xPosition + 19 - 2 - fr.getStringWidth(s)), (float) (yPosition + 6 + 3), 16777215);
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
                // Fixes opaque cooldown overlay a bit lower
                // TODO: check if enabled blending still screws things up down the line.
                GlStateManager.enableBlend();
            }

            if (stack.getItem().showDurabilityBar(stack)) {
                GlStateManager.disableLighting();
                GlStateManager.enableDepth();
//                GlStateManager.disableDepth();
                GlStateManager.disableTexture2D();
                GlStateManager.disableAlpha();
                GlStateManager.disableBlend();
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuffer();
                double health = stack.getItem().getDurabilityForDisplay(stack);
                int rgbfordisplay = stack.getItem().getRGBDurabilityForDisplay(stack);
                int i = Math.round(13.0F - (float) health * 13.0F);
                int j = rgbfordisplay;
                draw(bufferbuilder, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
                draw(bufferbuilder, xPosition + 2, yPosition + 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
                GlStateManager.enableBlend();
                GlStateManager.enableAlpha();
                GlStateManager.enableTexture2D();
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }

            EntityPlayerSP entityplayersp = Minecraft.getMinecraft().player;
            float f3 = entityplayersp == null ? 0.0F : entityplayersp.getCooldownTracker().getCooldown(stack.getItem(), Minecraft.getMinecraft().getRenderPartialTicks());

            if (f3 > 0.0F) {
                GlStateManager.disableLighting();
                GlStateManager.enableDepth();
//                GlStateManager.disableDepth();
                GlStateManager.disableTexture2D();
                Tessellator tessellator1 = Tessellator.getInstance();
                BufferBuilder bufferbuilder1 = tessellator1.getBuffer();
                draw(bufferbuilder1, xPosition, yPosition + MathHelper.floor(16.0F * (1.0F - f3)), 16, MathHelper.ceil(16.0F * f3), 255, 255, 255, 127);
                GlStateManager.enableTexture2D();
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }
        }
    }

    private static void draw(BufferBuilder renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
        renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos((double) (x + 0), (double) (y + 0), 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos((double) (x + 0), (double) (y + height), 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos((double) (x + width), (double) (y + height), 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos((double) (x + width), (double) (y + 0), 0.0D).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().draw();
    }


}
