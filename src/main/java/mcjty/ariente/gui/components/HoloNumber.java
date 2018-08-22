package mcjty.ariente.gui.components;

import mcjty.ariente.gui.HoloGuiRenderTools;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.Function;
import java.util.function.Supplier;

public class HoloNumber extends AbstractHoloComponent {

    private final int color;
    private Function<EntityPlayer, Integer> getter;

    public HoloNumber(double x, double y, double w, double h, int color, Function<EntityPlayer, Integer> getter) {
        super(x, y, w, h);
        this.getter = getter;
        this.color = color;
    }

    @Override
    public void render(EntityPlayer player, double cursorX, double cursorY) {
        String text = Integer.toString(getter.apply(player));
        HoloGuiRenderTools.renderText(x, y+.1, text, color);
    }
}
