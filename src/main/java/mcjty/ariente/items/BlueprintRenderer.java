package mcjty.ariente.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;

public class BlueprintRenderer extends TileEntityItemStackRenderer {

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        GlStateManager.pushMatrix();

        // Translate to the location of our tile entity
        GlStateManager.disableRescaleNormal();

        // Render our item
        GlStateManager.translate(.5, .5, 0);
        Minecraft.getMinecraft().getRenderItem().renderItem(new ItemStack(ModItems.blueprintItem), ItemCameraTransforms.TransformType.NONE);

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
            GlStateManager.translate(0, 0, .5);
            GlStateManager.scale(.5f, .5f, .5f);
            GlStateManager.rotate(30, 1, 0, 0);
            long angle = (System.currentTimeMillis() / 50) % 360;
            GlStateManager.rotate(angle, 0, 1, 0);

            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
        }
    }


}
