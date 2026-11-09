package mcjty.ariente.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.hologui.gui.HoloRenderBatch;
import net.minecraft.client.gui.Font;
import mcjty.lib.varia.ComponentFactory;
import net.minecraft.locale.Language;
import mcjty.lib.client.RenderBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class ArienteRenderHelper {

    private ArienteRenderHelper() {
    }

    public static void item(PoseStack poses, RenderBufferSource buffer, ItemStack stack, ItemDisplayContext context, int light, int overlay) {
        item(poses, buffer, stack, context, light, overlay, 1, 1);
    }

    public static void item(PoseStack poses, RenderBufferSource buffer, ItemStack stack, ItemDisplayContext context, int light, int overlay, float brightness, float alpha) {
        ((HoloRenderBatch) buffer).item(poses, stack, context, light, overlay, brightness, false, alpha);
    }

    public static void text(PoseStack poses, RenderBufferSource buffer, String text, float x, float y,
                            int color, boolean shadow, int light) {
        var formatted = Language.getInstance().getVisualOrder(ComponentFactory.formatted(text));
        ((HoloRenderBatch) buffer).command(poses, (base, collector) -> collector.submitText(base, x, y, formatted,
                shadow, Font.DisplayMode.NORMAL, light, color, 0, 0));
    }
}
