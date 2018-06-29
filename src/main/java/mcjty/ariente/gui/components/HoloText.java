package mcjty.ariente.gui.components;

import mcjty.ariente.gui.HoloGuiRenderTools;

public class HoloText extends AbstractHoloComponent {

    private final String text;
    private final int color;

    public HoloText(double x, double y, double w, double h, String text, int color) {
        super(x, y, w, h);
        this.text = text;
        this.color = color;
    }

    @Override
    public void render(double cursorX, double cursorY) {
        HoloGuiRenderTools.renderText(x, y, text, color);
    }
}
