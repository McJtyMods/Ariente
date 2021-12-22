package mcjty.ariente.blocks.utility;

import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.math.Vector3f;
import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.client.CustomRenderTypes;
import mcjty.lib.client.RenderHelper;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;

public class StorageRenderer implements BlockEntityRenderer<StorageTile> {

    private static int xx[] = new int[]{11, 36, 11, 36};
    private static int yy[] = new int[]{9, 9, 34, 34};
    protected BlockEntityRendererProvider.Context context;

    public StorageRenderer(BlockEntityRendererProvider.Context pContext) {
        context = pContext;
    }

    @Override
    public void render(StorageTile te, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        HitResult mouseOver = Minecraft.getInstance().hitResult;
        int index;
        if (mouseOver instanceof BlockHitResult && te.getBlockPos().equals(((BlockHitResult) mouseOver).getBlockPos())) {
            index = StorageTile.getSlot((BlockHitResult)mouseOver, te.getLevel());
        } else {
            index = -2;
        }

        BlockState state = te.getLevel().getBlockState(te.getBlockPos());
        Block block = state.getBlock();
        if (!(block instanceof BaseBlock)) {
            return;
        }

        // @todo 1.15
//        Minecraft.getInstance().gameRenderer.getLightTexture().disableLightmap();

        BaseBlock gb = (BaseBlock) block;

        matrixStack.pushPose();
        Direction facing = gb.getFrontDirection(gb.getRotationType(), state);

        // @todo 1.15
        float x = 0;
        float y = 0;
        float z = 0;
        matrixStack.translate(x + 0.5F, y + 0.75F, z + 0.5F);

        if (facing == Direction.UP) {
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
            matrixStack.translate(0.0F, 0.0F, -0.68F);
        } else if (facing == Direction.DOWN) {
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
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
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(-rotY));
            matrixStack.translate(0.0F, -0.2500F, -0.4375F);
        }

        matrixStack.translate(0.0F, 0.0F, 0.9F+.1);

        // @todo 1.15
//        GlStateManager.depthMask(true);
//        GlStateManager.enableDepthTest();

        renderSlotHilight(matrixStack, buffer, index);
        renderSlots(matrixStack, buffer, te);

        matrixStack.popPose();
        // @todo 1.15
//        Minecraft.getInstance().gameRenderer.getLightTexture().enableLightmap();
    }

    private void renderSlotHilight(PoseStack matrixStack, MultiBufferSource buffer, int index) {
        matrixStack.pushPose();

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

        matrixStack.popPose();
    }

    private void renderSlots(PoseStack matrixStack, MultiBufferSource buffer, StorageTile te) {
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

                matrixStack.pushPose();
                matrixStack.translate(xx[i], yy[i], 0);
                matrixStack.scale(16, 16, 16);
                matrixStack.translate(.5, .5, 0);
                BakedModel ibakedmodel = itemRender.getModel(stack, Minecraft.getInstance().level, null, 1);
                int lightmapValue = 0xf000f0;
                itemRender.render(stack, ItemTransforms.TransformType.GUI, false, matrixStack, buffer, lightmapValue, OverlayTexture.NO_OVERLAY, ibakedmodel);
                matrixStack.popPose();
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
//                renderItemOverlayIntoGUI(matrixStack, Minecraft.getInstance().fontRenderer, stack, xx[i] * 2 + 15, yy[i] * 2 + 16, getSize(stack.getCount()));
                renderSlotOverlay(matrixStack, buffer, Minecraft.getInstance().font, yy[i] * 2 + 16, stack, xx[i] * 2 + 15, 0xf000f0);
            }
        }

//        RenderHelper.enableStandardItemLighting();
    }

    private int renderSlotOverlay(PoseStack matrixStack, MultiBufferSource buffer, Font fontRenderer, int currenty, ItemStack itm, int x, int lightmapValue) {
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
                fontRenderer.drawInBatch(s1, x + 19 - 2 - fontRenderer.width(s1), currenty + 6 + 3, 16777215, false, matrixStack.last().pose(), buffer, false, 0, lightmapValue);
            }

            if (itm.getItem().isBarVisible(itm)) {
                int health = itm.getItem().getBarWidth(itm);
                int k = (int) Math.round(255.0D - health * (255.0D/13.0D));
                VertexConsumer builder = buffer.getBuffer(CustomRenderTypes.QUADS_NOTEXTURE);
                int r1 = 255 - k;
                int g1 = k;
                int b1 = 0;
                int r2 = (255-k)/4;
                int g2 = 0x3f;
                int b2 = 0;

                renderQuad(builder, x + 2, currenty + 13, 13, 2, 0, 0, 0, 0.0D);
                renderQuad(builder, x + 2, currenty + 13, 12, 1, r2, g2, b2, 0.02D);
                renderQuad(builder, x + 2, currenty + 13, health, 1, r1, g1, b1, 0.04D);
            }
        }
        x += 30;
        return x;
    }

    private static void renderQuad(VertexConsumer builder, int x, int y, int width, int height, int r, int g, int b, double offset) {
        builder.vertex(x, y, offset).color(r, g, b, 255).uv2(0xf000f0).endVertex();
        builder.vertex(x, (y + height), offset).color(r, g, b, 255).uv2(0xf000f0).endVertex();
        builder.vertex((x + width), (y + height), offset).color(r, g, b, 255).uv2(0xf000f0).endVertex();
        builder.vertex((x + width), y, offset).color(r, g, b, 255).uv2(0xf000f0).endVertex();
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

    private static void renderItemOverlayIntoGUI(PoseStack matrixStack, Font fr, ItemStack stack, int xPosition, int yPosition, @Nullable String text) {
        if (!stack.isEmpty()) {
            if (stack.getCount() != 1 || text != null) {
                String s = text == null ? String.valueOf(stack.getCount()) : text;
                // @todo 1.18 GlStateManager._disableLighting();
                GlStateManager._enableDepthTest();
//                GlStateManager.disableDepth();
                GlStateManager._disableBlend();
                fr.draw(matrixStack, s, (xPosition + 19 - 2 - fr.width(s)), (yPosition + 6 + 3), 16777215);
//                fr.drawStringWithShadow(s, (float) (xPosition + 19 - 2 - fr.getStringWidth(s)), (float) (yPosition + 6 + 3), 16777215);
                // @todo 1.18 GlStateManager._enableLighting();
                GlStateManager._enableDepthTest();
                // Fixes opaque cooldown overlay a bit lower
                // TODO: check if enabled blending still screws things up down the line.
                GlStateManager._enableBlend();
            }

            if (stack.getItem().isBarVisible(stack)) {
                // @todo 1.18 GlStateManager._disableLighting();
                GlStateManager._enableDepthTest();
//                GlStateManager.disableDepth();
                GlStateManager._disableTexture();
                // @todo 1.18 GlStateManager._disableAlphaTest();
                GlStateManager._disableBlend();
                Tesselator tessellator = Tesselator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuilder();
                int health = stack.getItem().getBarWidth(stack);
                int rgbfordisplay = stack.getItem().getBarColor(stack);
                int j = rgbfordisplay;
                draw(bufferbuilder, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
                draw(bufferbuilder, xPosition + 2, yPosition + 13, health, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
                GlStateManager._enableBlend();
                // @todo 1.18 GlStateManager._enableAlphaTest();
                GlStateManager._enableTexture();
                // @todo 1.18 GlStateManager._enableLighting();
                GlStateManager._enableDepthTest();
            }

            Player entityplayersp = Minecraft.getInstance().player;
            float f3 = entityplayersp == null ? 0.0F : entityplayersp.getCooldowns().getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());

            if (f3 > 0.0F) {
                // @todo 1.18 GlStateManager._disableLighting();
                GlStateManager._enableDepthTest();
//                GlStateManager.disableDepth();
                GlStateManager._disableTexture();
                Tesselator tessellator1 = Tesselator.getInstance();
                BufferBuilder bufferbuilder1 = tessellator1.getBuilder();
                draw(bufferbuilder1, xPosition, yPosition + Mth.floor(16.0F * (1.0F - f3)), 16, Mth.ceil(16.0F * f3), 255, 255, 255, 127);
                GlStateManager._enableTexture();
                // @todo 1.18 GlStateManager._enableLighting();
                GlStateManager._enableDepthTest();
            }
        }
    }

    private static void draw(BufferBuilder renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
        renderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        renderer.vertex((x + 0), (y + 0), 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.vertex((x + 0), (y + height), 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.vertex((x + width), (y + height), 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.vertex((x + width), (y + 0), 0.0D).color(red, green, blue, alpha).endVertex();
        Tesselator.getInstance().end();
    }

    public static void register() {
        BlockEntityRenderers.register(Registration.STORAGE_TILE.get(), StorageRenderer::new);
    }
}
