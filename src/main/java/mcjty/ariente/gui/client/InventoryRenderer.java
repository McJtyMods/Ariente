package mcjty.ariente.gui.client;

import mcjty.ariente.client.ArienteRenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.hologui.gui.HoloGuiRenderTools;
import net.minecraft.client.Minecraft;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import static mcjty.ariente.gui.InventoryLayout.*;

public class InventoryRenderer {

    public static void header(PoseStack poses, MultiBufferSource buffer, String hint) {
        HoloGuiRenderTools.renderLine(poses, buffer, GRID_X, PLAYER_Y - .85, GRID_X + WIDTH, PLAYER_Y - .85, .02, 0x487b9c);
        HoloGuiRenderTools.renderText(poses, buffer, GRID_X, PLAYER_Y - .6, "Inventory    " + hint, 0xffb8f4ff, .3f);
    }

    public static void cell(PoseStack poses, MultiBufferSource buffer, double x, double y, ItemStack stack, int amount, boolean highlight) {
        cell(poses, buffer, x, y, stack, amount, highlight, true);
    }

    public static void cell(PoseStack poses, MultiBufferSource buffer, double x, double y, ItemStack stack, int amount, boolean highlight, boolean enabled) {
        HoloGuiRenderTools.renderBorder(poses, buffer, x, y, CELL, CELL,
                enabled ? (highlight ? 190 : 75) : 45, enabled ? 160 : 80, enabled ? 200 : 100, 170);
        if (amount > 0) {
            item(poses, buffer, x, y, stack, enabled ? 1 : .35f);
            if (amount > 1) {
                count(poses, buffer, x, y, amount, enabled ? 0xffffffff : 0xff777777);
            }
        }
    }

    public static void item(PoseStack poses, MultiBufferSource buffer, double x, double y, ItemStack stack) {
        item(poses, buffer, x, y, stack, 1);
    }

    private static void item(PoseStack poses, MultiBufferSource buffer, double x, double y, ItemStack stack, float brightness) {
        // Keep icons aligned to the borders even in the lower inventory rows.
        poses.pushPose();
        poses.translate((x + .36) * .1 - .42, .42 - (y + .34) * .1, .004);
        poses.scale(.063f, .063f, .000063f);
        ArienteRenderHelper.item(poses, buffer, stack, ItemDisplayContext.GUI, LightCoordsUtil.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, brightness, 1);
        poses.popPose();
    }

    public static void count(PoseStack poses, MultiBufferSource buffer, double x, double y, int amount) {
        count(poses, buffer, x, y, amount, 0xffffffff);
    }

    private static void count(PoseStack poses, MultiBufferSource buffer, double x, double y, int amount, int color) {
        String label = Integer.toString(amount);
        var font = Minecraft.getInstance().font;
        // Account for renderText's .2-unit inset when aligning inside the slot.
        double labelX = x + CELL - .06 - font.width(label) * .03 - .2;
        double labelY = y + CELL - .05 - font.lineHeight * .03 - .2;
        poses.pushPose();
        poses.translate(0, 0, .01);
        HoloGuiRenderTools.renderText(poses, buffer, labelX, labelY, label, color, .3f);
        poses.popPose();
    }
}
