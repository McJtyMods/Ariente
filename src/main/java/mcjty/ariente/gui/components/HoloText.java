package mcjty.ariente.gui.components;

import mcjty.ariente.gui.HoloGuiEntity;
import mcjty.ariente.gui.HoloGuiRenderTools;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.Supplier;

public class HoloText extends AbstractHoloComponent {

    private final Supplier<String> text;
    private final int color;

    public HoloText(double x, double y, double w, double h, String text, int color) {
        super(x, y, w, h);
        this.text = () -> text;
        this.color = color;
    }

    public HoloText(double x, double y, double w, double h, Supplier<String> text, int color) {
        super(x, y, w, h);
        this.text = text;
        this.color = color;
    }

    @Override
    public void render(EntityPlayer player, HoloGuiEntity holo, double cursorX, double cursorY) {
        HoloGuiRenderTools.renderText(x, y, text.get(), color);
    }
}
