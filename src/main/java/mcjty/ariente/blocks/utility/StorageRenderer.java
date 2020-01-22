package mcjty.ariente.blocks.utility;

import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.lib.blocks.BaseBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public class StorageRenderer extends TileEntityRenderer<StorageTile> {

    private static int xx[] = new int[]{11, 36, 11, 36};
    private static int yy[] = new int[]{9, 9, 34, 34};

    @Override
    public void render(StorageTile te, double x, double y, double z, float partialTicks, int destroyStage) {
        RayTraceResult mouseOver = Minecraft.getInstance().objectMouseOver;
        int index;
        if (mouseOver instanceof BlockRayTraceResult && te.getPos().equals(((BlockRayTraceResult) mouseOver).getPos())) {
            index = StorageTile.getSlot((BlockRayTraceResult)mouseOver, te.getWorld());
        } else {
            index = -2;
        }

        BlockState state = te.getWorld().getBlockState(te.getPos());
        Block block = state.getBlock();
        if (!(block instanceof BaseBlock)) {
            return;
        }

        Minecraft.getInstance().gameRenderer.disableLightmap();

        BaseBlock gb = (BaseBlock) block;

        GlStateManager.pushMatrix();
        Direction facing = gb.getFrontDirection(gb.getRotationType(), state);

        GlStateManager.translatef((float) x + 0.5F, (float) y + 0.75F, (float) z + 0.5F);

        if (facing == Direction.UP) {
            GlStateManager.rotatef(-90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translatef(0.0F, 0.0F, -0.68F);
        } else if (facing == Direction.DOWN) {
            GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translatef(0.0F, 0.0F, -.184F);
        } else {
            float rotY = 0.0F;
            if (facing == Direction.NORTH) {
                rotY = 180.0F;
            } else if (facing == Direction.WEST) {
                rotY = 90.0F;
            } else if (facing == Direction.EAST) {
                rotY = -90.0F;
            }
            GlStateManager.rotatef(-rotY, 0.0F, 1.0F, 0.0F);
            GlStateManager.translatef(0.0F, -0.2500F, -0.4375F);
        }

        GlStateManager.translatef(0.0F, 0.0F, 0.9F);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepthTest();

        renderSlotHilight(index);
        renderSlots(te);

        GlStateManager.popMatrix();
        Minecraft.getInstance().gameRenderer.enableLightmap();
    }

    private void renderSlotHilight(int index) {
        GlStateManager.pushMatrix();

        float factor = 2.0f;
        float f3 = 0.0075F;
        GlStateManager.translatef(-0.5F, 0.5F, -0.155F);
        GlStateManager.scalef(f3 * factor, -f3 * factor, f3);
        GlStateManager.disableLighting();

        for (int i = 0; i < 4; i++) {
//            Gui.drawRect(xx[i] - 4, yy[i] - 4, xx[i] + 22, yy[i] - 3, 0xff222222);
//            Gui.drawRect(xx[i] - 4, yy[i] + 21, xx[i] + 22, yy[i] + 22, 0xff222222);
//            Gui.drawRect(xx[i] - 4, yy[i] - 4, xx[i] - 3, yy[i] + 22, 0xff222222);
//            Gui.drawRect(xx[i] + 21, yy[i] - 4, xx[i] + 22, yy[i] + 22, 0xff222222);
            if (index == i) {
                Screen.fill(xx[i] - 3, yy[i] - 3, xx[i] + 21, yy[i] + 21, index == i ? 0x55666666 : 0x55000000);
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

        ItemRenderer itemRender = Minecraft.getInstance().getItemRenderer();

        for (int i = 0; i < StorageTile.STACKS; i++) {
            ItemStack stack = te.getTotalStack(i);
            if (!stack.isEmpty()) {
                itemRender.renderItemAndEffectIntoGUI(stack, xx[i], yy[i]);
            }
        }

        GL11.glTranslatef(0F, 0F, 500F);
        GlStateManager.scaled(.5, .5, .5);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepthTest();
        for (int i = 0; i < StorageTile.STACKS; i++) {
            ItemStack stack = te.getTotalStack(i);
            if (!stack.isEmpty()) {
                renderItemOverlayIntoGUI(Minecraft.getInstance().fontRenderer, stack, xx[i] * 2 + 15, yy[i] * 2 + 16, getSize(stack.getCount()));
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
                GlStateManager.enableDepthTest();
//                GlStateManager.disableDepth();
                GlStateManager.disableBlend();
                fr.drawString(s, (xPosition + 19 - 2 - fr.getStringWidth(s)), (yPosition + 6 + 3), 16777215);
//                fr.drawStringWithShadow(s, (float) (xPosition + 19 - 2 - fr.getStringWidth(s)), (float) (yPosition + 6 + 3), 16777215);
                GlStateManager.enableLighting();
                GlStateManager.enableDepthTest();
                // Fixes opaque cooldown overlay a bit lower
                // TODO: check if enabled blending still screws things up down the line.
                GlStateManager.enableBlend();
            }

            if (stack.getItem().showDurabilityBar(stack)) {
                GlStateManager.disableLighting();
                GlStateManager.enableDepthTest();
//                GlStateManager.disableDepth();
                GlStateManager.disableTexture();
                GlStateManager.disableAlphaTest();
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
                GlStateManager.enableAlphaTest();
                GlStateManager.enableTexture();
                GlStateManager.enableLighting();
                GlStateManager.enableDepthTest();
            }

            PlayerEntity entityplayersp = Minecraft.getInstance().player;
            float f3 = entityplayersp == null ? 0.0F : entityplayersp.getCooldownTracker().getCooldown(stack.getItem(), Minecraft.getInstance().getRenderPartialTicks());

            if (f3 > 0.0F) {
                GlStateManager.disableLighting();
                GlStateManager.enableDepthTest();
//                GlStateManager.disableDepth();
                GlStateManager.disableTexture();
                Tessellator tessellator1 = Tessellator.getInstance();
                BufferBuilder bufferbuilder1 = tessellator1.getBuffer();
                draw(bufferbuilder1, xPosition, yPosition + MathHelper.floor(16.0F * (1.0F - f3)), 16, MathHelper.ceil(16.0F * f3), 255, 255, 255, 127);
                GlStateManager.enableTexture();
                GlStateManager.enableLighting();
                GlStateManager.enableDepthTest();
            }
        }
    }

    private static void draw(BufferBuilder renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
        renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos((x + 0), (y + 0), 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos((x + 0), (y + height), 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos((x + width), (y + height), 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos((x + width), (y + 0), 0.0D).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().draw();
    }

    public static void register() {
        ClientRegistry.bindTileEntitySpecialRenderer(StorageTile.class, new StorageRenderer());
    }
}
