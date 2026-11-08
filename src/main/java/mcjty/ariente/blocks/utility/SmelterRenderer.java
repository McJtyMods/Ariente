package mcjty.ariente.blocks.utility;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mcjty.ariente.client.ArienteRenderType;
import mcjty.ariente.setup.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.joml.Matrix4f;

public class SmelterRenderer implements BlockEntityRenderer<SmelterTile> {

    public SmelterRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SmelterTile tile, float partialTicks, PoseStack poses, MultiBufferSource buffer, int light, int overlay) {
        poses.pushPose();
        poses.translate(.5, .5, .5);
        poses.mulPose(Axis.YP.rotationDegrees(switch (Registration.SMELTER.get().getFrontDirection(tile.getBlockState())) {
            case SOUTH -> 0;
            case WEST -> -90;
            case EAST -> 90;
            default -> 180;
        }));
        poses.translate(0, 0, .385);
        renderSlotHighlight(tile, poses, buffer);
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        MultiBufferSource itemBuffer = type -> buffer.getBuffer(ArienteRenderType.displayItem(type));
        for (int slot = 0; slot < 2; slot++) {
            ItemStack stack = tile.getStack(slot);
            if (stack.isEmpty()) {
                continue;
            }
            poses.pushPose();
            poses.translate(slot == SmelterTile.SLOT_INPUT ? -4.25 / 16 : 4.25 / 16, 0, 0);
            poses.pushPose();
            poses.scale(.22f, .22f, .0001f);
            renderer.render(stack, ItemDisplayContext.GUI, false, poses, itemBuffer, 0xf000f0,
                    OverlayTexture.NO_OVERLAY, renderer.getModel(stack, tile.getLevel(), null, 1));
            poses.popPose();
            if (stack.getCount() > 1) {
                Font font = Minecraft.getInstance().font;
                String count = Integer.toString(stack.getCount());
                poses.translate(.125, -.065, .005);
                poses.scale(.005f, -.005f, .005f);
                font.drawInBatch(count, -font.width(count), 0, 0xffffff, true, poses.last().pose(), buffer,
                        Font.DisplayMode.NORMAL, 0, 0xf000f0);
            }
            poses.popPose();
        }
        poses.popPose();
    }

    private void renderSlotHighlight(SmelterTile tile, PoseStack poses, MultiBufferSource buffer) {
        if (!(Minecraft.getInstance().hitResult instanceof BlockHitResult hit)) {
            return;
        }
        int slot = tile.getSlot(hit);
        if (slot < 0) {
            return;
        }
        float center = slot == SmelterTile.SLOT_INPUT ? -4.25f / 16 : 4.25f / 16;
        float halfSize = 2f / 16;
        int color = 0x55666666;
        Matrix4f matrix = poses.last().pose();
        VertexConsumer vertices = buffer.getBuffer(RenderType.gui());
        vertices.addVertex(matrix, center - halfSize, -halfSize, -.004f).setColor(color);
        vertices.addVertex(matrix, center + halfSize, -halfSize, -.004f).setColor(color);
        vertices.addVertex(matrix, center + halfSize, halfSize, -.004f).setColor(color);
        vertices.addVertex(matrix, center - halfSize, halfSize, -.004f).setColor(color);
    }

    public static void register() {
        BlockEntityRenderers.register(Registration.SMELTER_TILE.get(), SmelterRenderer::new);
    }
}
