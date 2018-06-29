package mcjty.ariente.gui.components;

import mcjty.ariente.gui.HoloGuiRenderTools;

public class HoloButton extends AbstractHoloComponent {

    private final String text;
    private final int color;
    private final int hoverColor;

    public HoloButton(double x, double y, double w, double h, String text, int color, int hoverColor) {
        super(x, y, w, h);
        this.text = text;
        this.color = color;
        this.hoverColor = hoverColor;
    }

    @Override
    public void render(double cursorX, double cursorY) {
        int c = color;
        if (isInside(cursorX, cursorY)) {
            c = hoverColor;
        }
        HoloGuiRenderTools.renderBox(x-1, y, w, h, 0xffff0000);
        HoloGuiRenderTools.renderBox(x, y, w, h, 0xffffffff);
        HoloGuiRenderTools.renderText(x, y, text, c);
    }
}
