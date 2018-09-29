package mcjty.ariente.apiimp.hologui.components;

import mcjty.ariente.api.hologui.IHoloGuiEntity;
import mcjty.ariente.api.hologui.components.IStackIcon;
import mcjty.ariente.apiimp.hologui.HoloGuiRenderTools;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class HoloStackIcon extends AbstractHoloComponent implements IStackIcon {

    private ItemStack stack;

    HoloStackIcon(double x, double y, double w, double h) {
        super(x, y, w, h);
    }

    @Override
    public HoloStackIcon itemStack(@Nonnull ItemStack stack) {
        this.stack = stack;
        return this;
    }

    @Override
    public void render(EntityPlayer player, IHoloGuiEntity holo, double cursorX, double cursorY) {
        HoloGuiRenderTools.renderItem(x * 1.05, y * 0.85 + .45, stack, null, false);
    }

    @Override
    public void renderTooltip(EntityPlayer player, IHoloGuiEntity holo, double cursorX, double cursorY) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.01, 0.01, 0.01);
        GlStateManager.rotate(180, 0, 1, 0);
        GlStateManager.rotate(180, 0, 0, 1);
        GlStateManager.translate(0, 0, -10);
        GlStateManager.scale(0.4, 0.4, 0.0);
        HoloGuiRenderTools.renderToolTip(stack, (int) (x * 30 - 120), (int) (y * 30 - 120));
        GlStateManager.popMatrix();
    }

}
