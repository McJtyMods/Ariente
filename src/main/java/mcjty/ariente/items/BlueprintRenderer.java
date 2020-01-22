package mcjty.ariente.items;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;

public class BlueprintRenderer extends ItemStackTileEntityRenderer {

    @Override
    public void renderByItem(ItemStack stack) {
        GlStateManager.pushMatrix();

        // Translate to the location of our tile entity
        GlStateManager.disableRescaleNormal();

        // Render our item
        GlStateManager.translated(.5, .5, 0);
        Minecraft.getInstance().getItemRenderer().renderItem(new ItemStack(ModItems.blueprintItem), ItemCameraTransforms.TransformType.NONE);

        ItemStack destination = BlueprintItem.getDestination(stack);
        if (!destination.isEmpty()) {
            renderItem(destination);
        }

        GlStateManager.popMatrix();

        GlStateManager.enableBlend();       // Restore GL state

    }

    private void renderItem(ItemStack stack) {
        if (!stack.isEmpty()) {
//            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableLighting();
            // Translate to the center of the block and .9 points higher
            GlStateManager.translated(0, 0, .5);
            GlStateManager.scalef(.5f, .5f, .5f);
            GlStateManager.rotatef(30, 1, 0, 0);
            long angle = (System.currentTimeMillis() / 50) % 360;
            GlStateManager.rotatef(angle, 0, 1, 0);

            Minecraft.getInstance().getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
        }
    }


}
