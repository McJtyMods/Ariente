package mcjty.ariente.gui.components;

import mcjty.ariente.gui.HoloGuiRenderTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class HoloItemStack extends AbstractHoloComponent {

    private final ItemStack stack;

    public HoloItemStack(double x, double y, double w, double h, ItemStack stack) {
        super(x, y, w, h);
        this.stack = stack;
    }

    @Override
    public void render(EntityPlayer player, double cursorX, double cursorY) {
        HoloGuiRenderTools.renderItem(x * 1.05, y * 0.85 + .45, stack, null, false);
    }
}
