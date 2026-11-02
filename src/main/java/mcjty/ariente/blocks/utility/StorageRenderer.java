package mcjty.ariente.blocks.utility;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.*;
import mcjty.ariente.client.ArienteRenderType;
import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.client.CustomRenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

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

        BaseBlock gb = (BaseBlock) block;

        matrixStack.pushPose();
        Direction facing = gb.getFrontDirection(gb.getRotationType(), state);

        // @todo 1.15
        float x = 0;
        float y = 0;
        float z = 0;
        matrixStack.translate(x + 0.5F, y + 0.75F, z + 0.5F);

        if (facing == Direction.UP) {
            matrixStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            matrixStack.translate(0.0F, 0.0F, -0.68F);
        } else if (facing == Direction.DOWN) {
            matrixStack.mulPose(Axis.XP.rotationDegrees(90.0F));
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
            matrixStack.mulPose(Axis.YP.rotationDegrees(-rotY));
            matrixStack.translate(0.0F, -0.2500F, -0.4375F);
        }

        matrixStack.translate(0.0F, 0.0F, 0.9F+.1);

        renderSlotHilight(matrixStack, buffer, index);
        renderSlots(matrixStack, buffer, te);

        matrixStack.popPose();
    }

    private void renderSlotHilight(PoseStack matrixStack, MultiBufferSource buffer, int index) {
        matrixStack.pushPose();

        float factor = 2.0f;
        float f3 = 0.0075F;
        matrixStack.translate(-0.5F, 0.5F, -0.155F);
        matrixStack.scale(f3 * factor, -f3 * factor, f3);

        for (int i = 0; i < 4; i++) {
            fill(matrixStack, buffer, xx[i] - 4, yy[i] - 4, xx[i] + 22, yy[i] - 3, 0xff222222, 0xf000f0);
            fill(matrixStack, buffer, xx[i] - 4, yy[i] + 21, xx[i] + 22, yy[i] + 22, 0xff222222, 0xf000f0);
            fill(matrixStack, buffer, xx[i] - 4, yy[i] - 4, xx[i] - 3, yy[i] + 22, 0xff222222, 0xf000f0);
            fill(matrixStack, buffer, xx[i] + 21, yy[i] - 4, xx[i] + 22, yy[i] + 22, 0xff222222, 0xf000f0);
            if (index == i) {
                fill(matrixStack, buffer, xx[i] - 3, yy[i] - 3, xx[i] + 21, yy[i] + 21, index == i ? 0x55666666 : 0x55000000, 0xf000f0);
            }
        }

        matrixStack.popPose();
    }

    private void renderSlots(PoseStack matrixStack, MultiBufferSource buffer, StorageTile te) {
        float factor = 2.0f;
        float f3 = 0.0075F;
        matrixStack.translate(-0.5F, 0.5F, -0.15F);
        matrixStack.scale(f3 * factor, -f3 * factor, 0.0001f);

        ItemRenderer itemRender = Minecraft.getInstance().getItemRenderer();
        MultiBufferSource itemBuffer = type -> buffer.getBuffer(ArienteRenderType.displayItem(type));

        for (int i = 0; i < StorageTile.STACKS; i++) {
            ItemStack stack = te.getTotalStack(i);
            if (!stack.isEmpty()) {
                matrixStack.pushPose();
                matrixStack.translate(xx[i] + 8, yy[i] + 8, 0);
                matrixStack.scale(16, -16, 16);
                BakedModel ibakedmodel = itemRender.getModel(stack, Minecraft.getInstance().level, null, 1);
                int lightmapValue = 0xf000f0;
                itemRender.render(stack, ItemDisplayContext.GUI, false, matrixStack, itemBuffer, lightmapValue, OverlayTexture.NO_OVERLAY, ibakedmodel);
                matrixStack.popPose();
            }
        }

        matrixStack.translate(0F, 0F, 500F);
        matrixStack.scale(.5f, .5f, .5f);
        for (int i = 0; i < StorageTile.STACKS; i++) {
            ItemStack stack = te.getTotalStack(i);
            if (!stack.isEmpty()) {
                renderSlotOverlay(matrixStack, buffer, Minecraft.getInstance().font, yy[i] * 2 + 16, stack, xx[i] * 2 + 15, 0xf000f0);
            }
        }
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
                fontRenderer.drawInBatch(s1, x + 19 - 2 - fontRenderer.width(s1), currenty + 6 + 3, 16777215, false, matrixStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, lightmapValue);
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

    public static void register() {
        BlockEntityRenderers.register(Registration.STORAGE_TILE.get(), StorageRenderer::new);
    }

    private static void fill(PoseStack pose, MultiBufferSource buffer, float x0, float y0, float x1, float y1, int color, int light) {
        VertexConsumer vertices = buffer.getBuffer(RenderType.gui());
        vertices.vertex(pose.last().pose(), x0, y1, 0).color(color).endVertex();
        vertices.vertex(pose.last().pose(), x1, y1, 0).color(color).endVertex();
        vertices.vertex(pose.last().pose(), x1, y0, 0).color(color).endVertex();
        vertices.vertex(pose.last().pose(), x0, y0, 0).color(color).endVertex();
    }

}
