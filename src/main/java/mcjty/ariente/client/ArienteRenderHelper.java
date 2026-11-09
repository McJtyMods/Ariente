package mcjty.ariente.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.hologui.gui.HoloRenderBatch;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class ArienteRenderHelper {

    private ArienteRenderHelper() {
    }

    public static void item(PoseStack poses, MultiBufferSource buffer, ItemStack stack, ItemDisplayContext context, int light, int overlay) {
        item(poses, buffer, stack, context, light, overlay, 1, 1);
    }

    public static void item(PoseStack poses, MultiBufferSource buffer, ItemStack stack, ItemDisplayContext context, int light, int overlay, float brightness, float alpha) {
        ((HoloRenderBatch) buffer).item(poses, stack, context, light, overlay, brightness, false, alpha);
    }
}
