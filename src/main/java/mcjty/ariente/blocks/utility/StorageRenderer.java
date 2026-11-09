package mcjty.ariente.blocks.utility;

import mcjty.lib.client.RenderBufferSource;
import mcjty.ariente.client.ArienteRenderHelper;
import mcjty.hologui.gui.HoloGuiRenderType;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mcjty.ariente.client.ArienteRenderType;
import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.client.CustomRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import mcjty.ariente.client.ExtractedBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class StorageRenderer extends ExtractedBlockRenderer<StorageTile> {

    protected BlockEntityRendererProvider.Context context;

    public StorageRenderer(BlockEntityRendererProvider.Context pContext) {
        context = pContext;
    }

    @Override
    public void render(StorageTile te, float partialTicks, PoseStack matrixStack, RenderBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
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

        BaseBlock gb = (BaseBlock) block;

        matrixStack.pushPose();
        Direction facing = gb.getFrontDirection(gb.getRotationType(), state);

        // @todo 1.15
        float x = 0;
        float y = 0;
        float z = 0;
        matrixStack.translate(x + 0.5F, y + 0.75F, z + 0.5F);

        if (facing == Direction.UP) {
            matrixStack.rotate(Axis.XP.rotationDegrees(-90.0F));
            matrixStack.translate(0.0F, 0.0F, -0.68F);
        } else if (facing == Direction.DOWN) {
            matrixStack.rotate(Axis.XP.rotationDegrees(90.0F));
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
            matrixStack.rotate(Axis.YP.rotationDegrees(-rotY));
            matrixStack.translate(0.0F, -0.2500F, -0.4375F);
        }

        matrixStack.translate(0.0F, 0.0F, 0.9F+.1);

        renderSlotHilight(matrixStack, buffer, te, index);
        renderSlots(matrixStack, buffer, te);

        matrixStack.popPose();
    }

    private static int slotX(StorageTile te, int slot) {
        return 11 + (te.getGridSize() - 2) * 4 + slot % te.getGridSize() * 25;
    }

    private static int slotY(StorageTile te, int slot) {
        return 9 + (te.getGridSize() - 2) * 3 + slot / te.getGridSize() * 25;
    }

    private void renderSlotHilight(PoseStack matrixStack, RenderBufferSource buffer, StorageTile te, int index) {
        matrixStack.pushPose();

        float factor = 4.0f / te.getGridSize();
        float f3 = 0.0075F;
        matrixStack.translate(-0.5F, 0.5F, -0.155F);
        matrixStack.scale(f3 * factor, -f3 * factor, f3);

        for (int i = 0; i < te.getTypeCount(); i++) {
            int x = slotX(te, i);
            int y = slotY(te, i);
            fill(matrixStack, buffer, x - 4, y - 4, x + 22, y - 3, 0xff222222, 0xf000f0);
            fill(matrixStack, buffer, x - 4, y + 21, x + 22, y + 22, 0xff222222, 0xf000f0);
            fill(matrixStack, buffer, x - 4, y - 4, x - 3, y + 22, 0xff222222, 0xf000f0);
            fill(matrixStack, buffer, x + 21, y - 4, x + 22, y + 22, 0xff222222, 0xf000f0);
            if (index == i) {
                fill(matrixStack, buffer, x - 3, y - 3, x + 21, y + 21, 0x55666666, 0xf000f0);
            }
        }

        matrixStack.popPose();
    }

    private void renderSlots(PoseStack matrixStack, RenderBufferSource buffer, StorageTile te) {
        float factor = 4.0f / te.getGridSize();
        float f3 = 0.0075F;
        matrixStack.translate(-0.5F, 0.5F, -0.15F);
        matrixStack.scale(f3 * factor, -f3 * factor, 0.0001f);

        RenderBufferSource itemBuffer = type -> buffer.getBuffer(ArienteRenderType.displayItem(type));

        for (int i = 0; i < te.getTypeCount(); i++) {
            ItemStack stack = te.getTotalStack(i);
            if (!stack.isEmpty()) {
                matrixStack.pushPose();
                matrixStack.translate(slotX(te, i) + 8, slotY(te, i) + 8, 0);
                matrixStack.scale(16, -16, 16);
                int lightmapValue = 0xf000f0;
                ArienteRenderHelper.item(matrixStack, buffer, stack, ItemDisplayContext.GUI, lightmapValue, OverlayTexture.NO_OVERLAY);
                matrixStack.popPose();
            }
        }

        matrixStack.translate(0F, 0F, 500F);
        matrixStack.scale(.5f, .5f, .5f);
        for (int i = 0; i < te.getTypeCount(); i++) {
            ItemStack stack = te.getTotalStack(i);
            if (!stack.isEmpty()) {
                renderSlotOverlay(matrixStack, buffer, Minecraft.getInstance().font, slotY(te, i) * 2 + 16, stack, slotX(te, i) * 2 + 15, 0xf000f0);
            }
        }
    }

    private int renderSlotOverlay(PoseStack matrixStack, RenderBufferSource buffer, Font fontRenderer, int currenty, ItemStack itm, int x, int lightmapValue) {
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
                ArienteRenderHelper.text(matrixStack, buffer, s1, x + 19 - 2 - fontRenderer.width(s1), currenty + 6 + 3, 0xffffffff, false, lightmapValue);
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
        builder.addVertex((float) x, (float) y, (float) offset).setColor(r, g, b, 255).setLight(0xf000f0);
        builder.addVertex((float) x, (float) (y + height), (float) offset).setColor(r, g, b, 255).setLight(0xf000f0);
        builder.addVertex((float) (x + width), (float) (y + height), (float) offset).setColor(r, g, b, 255).setLight(0xf000f0);
        builder.addVertex((float) (x + width), (float) y, (float) offset).setColor(r, g, b, 255).setLight(0xf000f0);
    }

    public static void register() {
        BlockEntityRenderers.register(Registration.STORAGE_TILE.get(), StorageRenderer::new);
    }

    private static void fill(PoseStack pose, RenderBufferSource buffer, float x0, float y0, float x1, float y1, int color, int light) {
        VertexConsumer vertices = buffer.getBuffer(HoloGuiRenderType.HOLOGUI_COLOR);
        vertices.addVertex(pose.last().pose(), x0, y1, 0).setColor(color);
        vertices.addVertex(pose.last().pose(), x1, y1, 0).setColor(color);
        vertices.addVertex(pose.last().pose(), x1, y0, 0).setColor(color);
        vertices.addVertex(pose.last().pose(), x0, y0, 0).setColor(color);
    }

}
