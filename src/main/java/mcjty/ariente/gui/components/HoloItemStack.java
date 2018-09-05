package mcjty.ariente.gui.components;

import mcjty.ariente.gui.HoloGuiEntity;
import mcjty.ariente.gui.HoloGuiRenderTools;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class HoloItemStack extends AbstractHoloComponent {

    private final ItemStack stack;

    public HoloItemStack(double x, double y, double w, double h, ItemStack stack) {
        super(x, y, w, h);
        this.stack = stack;
    }

    @Override
    public void render(EntityPlayer player, HoloGuiEntity holo, double cursorX, double cursorY) {
        HoloGuiRenderTools.renderItem(x * 1.05, y * 0.85 + .45, stack, null, false);
    }

    @Override
    public void renderTooltip(EntityPlayer player, HoloGuiEntity holo, double cursorX, double cursorY) {
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
