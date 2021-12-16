package mcjty.ariente.items;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import mcjty.ariente.setup.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.Callable;

public class BlueprintRenderer extends ItemStackTileEntityRenderer {


    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType p_239207_2_, PoseStack matrixStack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        matrixStack.pushPose();

        // Render our item
        matrixStack.translate(.5, .5, 0);

        ItemRenderer itemRender = Minecraft.getInstance().getItemRenderer();
        ItemStack itm = new ItemStack(Registration.BLUEPRINT.get());
        BakedModel ibakedmodel = itemRender.getModel(itm, Minecraft.getInstance().level, (LivingEntity)null);
        int lightmapValue = 140;
        itemRender.render(itm, ItemTransforms.TransformType.GUI, false, matrixStack, buffer, lightmapValue, OverlayTexture.NO_OVERLAY, ibakedmodel);

        ItemStack destination = BlueprintItem.getDestination(stack);
        if (!destination.isEmpty()) {
            renderItem(destination, matrixStack, buffer);
        }

        matrixStack.popPose();
    }

    private void renderItem(ItemStack stack, PoseStack matrixStack, MultiBufferSource buffer) {
        if (!stack.isEmpty()) {
            // Translate to the center of the block and .9 points higher
            matrixStack.translate(0, 0, .5);
            matrixStack.scale(.5f, .5f, .5f);
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(30));
            long angle = (System.currentTimeMillis() / 50) % 360;
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(angle));

            ItemRenderer itemRender = Minecraft.getInstance().getItemRenderer();
            BakedModel ibakedmodel = itemRender.getModel(stack, Minecraft.getInstance().level, (LivingEntity)null);
            int lightmapValue = 140;
            itemRender.render(stack, ItemTransforms.TransformType.GUI, false, matrixStack, buffer, lightmapValue, OverlayTexture.NO_OVERLAY, ibakedmodel);
        }
    }

    public static Callable createRenderer() {
        return BlueprintRenderer::new;
    }
}
