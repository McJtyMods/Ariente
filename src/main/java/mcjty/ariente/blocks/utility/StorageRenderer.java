package mcjty.ariente.blocks.utility;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.client.CustomRenderTypes;
import mcjty.lib.client.RenderHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import javax.annotation.Nullable;

public class StorageRenderer extends TileEntityRenderer<StorageTile> {

    private static int xx[] = new int[]{11, 36, 11, 36};
    private static int yy[] = new int[]{9, 9, 34, 34};

    public StorageRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(StorageTile te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
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

        // @todo 1.15
//        Minecraft.getInstance().gameRenderer.getLightTexture().disableLightmap();

        BaseBlock gb = (BaseBlock) block;

        matrixStack.push();
        Direction facing = gb.getFrontDirection(gb.getRotationType(), state);

        // @todo 1.15
        float x = 0;
        float y = 0;
        float z = 0;
        matrixStack.translate(x + 0.5F, y + 0.75F, z + 0.5F);

        if (facing == Direction.UP) {
            matrixStack.rotate(Vector3f.XP.rotationDegrees(-90.0F));
            matrixStack.translate(0.0F, 0.0F, -0.68F);
        } else if (facing == Direction.DOWN) {
            matrixStack.rotate(Vector3f.XP.rotationDegrees(90.0F));
            matrixStack.translate(0.0F, 0.0F, -.184F);
        } else {
            float rotY = 0.0F;
            if (facing == Direction.NORTH) {
                rotY = 180.0F;
            } else if (facing == Direction.WEST) {
                rotY = 90.0F;
            } else if (facing == Direction.EAST) {
                rotY = -90.0F;
            }
            matrixStack.rotate(Vector3f.YP.rotationDegrees(-rotY));
            matrixStack.translate(0.0F, -0.2500F, -0.4375F);
        }

        matrixStack.translate(0.0F, 0.0F, 0.9F+.1);

        // @todo 1.15
//        GlStateManager.depthMask(true);
//        GlStateManager.enableDepthTest();

        renderSlotHilight(matrixStack, buffer, index);
        renderSlots(matrixStack, buffer, te);

        matrixStack.pop();
        // @todo 1.15
//        Minecraft.getInstance().gameRenderer.getLightTexture().enableLightmap();
    }

    private void renderSlotHilight(MatrixStack matrixStack, IRenderTypeBuffer buffer, int index) {
        matrixStack.push();

        float factor = 2.0f;
        float f3 = 0.0075F;
        matrixStack.translate(-0.5F, 0.5F, -0.155F);
        matrixStack.scale(f3 * factor, -f3 * factor, f3);

        // @todo 1.15
//        GlStateManager.disableLighting();

        for (int i = 0; i < 4; i++) {
            RenderHelper.fill(matrixStack, buffer, xx[i] - 4, yy[i] - 4, xx[i] + 22, yy[i] - 3, 0xff222222, 0xf000f0);
            RenderHelper.fill(matrixStack, buffer, xx[i] - 4, yy[i] + 21, xx[i] + 22, yy[i] + 22, 0xff222222, 0xf000f0);
            RenderHelper.fill(matrixStack, buffer, xx[i] - 4, yy[i] - 4, xx[i] - 3, yy[i] + 22, 0xff222222, 0xf000f0);
            RenderHelper.fill(matrixStack, buffer, xx[i] + 21, yy[i] - 4, xx[i] + 22, yy[i] + 22, 0xff222222, 0xf000f0);
            if (index == i) {
                RenderHelper.fill(matrixStack, buffer, xx[i] - 3, yy[i] - 3, xx[i] + 21, yy[i] + 21, index == i ? 0x55666666 : 0x55000000, 0xf000f0);
            }
        }

        matrixStack.pop();
    }

    private void renderSlots(MatrixStack matrixStack, IRenderTypeBuffer buffer, StorageTile te) {
        // @todo 1.15
//        RenderHelper.enableGUIStandardItemLighting();

        float factor = 2.0f;
        float f3 = 0.0075F;
        matrixStack.translate(-0.5F, 0.5F, -0.15F);
        matrixStack.scale(f3 * factor, -f3 * factor, 0.0001f);

        ItemRenderer itemRender = Minecraft.getInstance().getItemRenderer();

        for (int i = 0; i < StorageTile.STACKS; i++) {
            ItemStack stack = te.getTotalStack(i);
            if (!stack.isEmpty()) {
//                itemRender.renderItemAndEffectIntoGUI(stack, xx[i], yy[i]);

                matrixStack.push();
                matrixStack.translate(xx[i], yy[i], 0);
                matrixStack.scale(16, 16, 16);
                matrixStack.translate(.5, .5, 0);
                IBakedModel ibakedmodel = itemRender.getItemModelWithOverrides(stack, Minecraft.getInstance().world, null);
                int lightmapValue = 0xf000f0;
                itemRender.renderItem(stack, ItemCameraTransforms.TransformType.GUI, false, matrixStack, buffer, lightmapValue, OverlayTexture.NO_OVERLAY, ibakedmodel);
                matrixStack.pop();
            }
        }

        matrixStack.translate(0F, 0F, 500F);
        matrixStack.scale(.5f, .5f, .5f);
        // @todo 1.15
//        GlStateManager.depthMask(true);
//        GlStateManager.enableDepthTest();
        for (int i = 0; i < StorageTile.STACKS; i++) {
            ItemStack stack = te.getTotalStack(i);
            if (!stack.isEmpty()) {
//                renderItemOverlayIntoGUI(Minecraft.getInstance().fontRenderer, stack, xx[i] * 2 + 15, yy[i] * 2 + 16, getSize(stack.getCount()));
                renderSlotOverlay(matrixStack, buffer, Minecraft.getInstance().fontRenderer, yy[i] * 2 + 16, stack, xx[i] * 2 + 15, 0xf000f0);
            }
        }

//        RenderHelper.enableStandardItemLighting();
    }

    private int renderSlotOverlay(MatrixStack matrixStack, IRenderTypeBuffer buffer, FontRenderer fontRenderer, int currenty, ItemStack itm, int x, int lightmapValue) {
        if (!itm.isEmpty()) {
            int size = itm.getCount();
            if (size > 1) {
                String s1;
                if (size < 10000) {
                    s1 = String.valueOf(size);
                } else if (size < 1000000) {
                    s1 = String.valueOf(size / 1000) + "k";
                } else if (size < 1000000000) {
                    s1 = String.valueOf(size / 1000000) + "m";
                } else {
                    s1 = String.valueOf(size / 1000000000) + "g";
                }
                fontRenderer.renderString(s1, x + 19 - 2 - fontRenderer.getStringWidth(s1), currenty + 6 + 3, 16777215, false, matrixStack.getLast().getMatrix(), buffer, false, 0, lightmapValue);
            }

            if (itm.getItem().showDurabilityBar(itm)) {
                double health = itm.getItem().getDurabilityForDisplay(itm);
                int j1 = (int) Math.round(13.0D - health * 13.0D);
                int k = (int) Math.round(255.0D - health * 255.0D);
                IVertexBuilder builder = buffer.getBuffer(CustomRenderTypes.QUADS_NOTEXTURE);
                int r1 = 255 - k;
                int g1 = k;
                int b1 = 0;
                int r2 = (255-k)/4;
                int g2 = 0x3f;
                int b2 = 0;

                renderQuad(builder, x + 2, currenty + 13, 13, 2, 0, 0, 0, 0.0D);
                renderQuad(builder, x + 2, currenty + 13, 12, 1, r2, g2, b2, 0.02D);
                renderQuad(builder, x + 2, currenty + 13, j1, 1, r1, g1, b1, 0.04D);
            }
        }
        x += 30;
        return x;
    }

    private static void renderQuad(IVertexBuilder builder, int x, int y, int width, int height, int r, int g, int b, double offset) {
        builder.pos(x, y, offset).color(r, g, b, 255).lightmap(0xf000f0).endVertex();
        builder.pos(x, (y + height), offset).color(r, g, b, 255).lightmap(0xf000f0).endVertex();
        builder.pos((x + width), (y + height), offset).color(r, g, b, 255).lightmap(0xf000f0).endVertex();
        builder.pos((x + width), y, offset).color(r, g, b, 255).lightmap(0xf000f0).endVertex();
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
        ClientRegistry.bindTileEntityRenderer(Registration.STORAGE_TILE.get(), StorageRenderer::new);
    }
}
