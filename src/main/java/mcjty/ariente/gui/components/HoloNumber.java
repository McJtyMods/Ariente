package mcjty.ariente.gui.components;

import mcjty.ariente.gui.HoloGuiRenderTools;

import java.util.function.Supplier;

public class HoloNumber extends AbstractHoloComponent {

    private final int color;
    private Supplier<Integer> getter;

    public HoloNumber(double x, double y, double w, double h, int color, Supplier<Integer> getter) {
        super(x, y, w, h);
        this.getter = getter;
        this.color = color;
    }

    @Override
    public void render(double cursorX, double cursorY) {
        String text = Integer.toString(getter.get());
        HoloGuiRenderTools.renderText(x, y, text, color);
    }
}
