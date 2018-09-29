package mcjty.ariente.apiimp.hologui.components;

import mcjty.ariente.api.hologui.IGuiComponentRegistry;
import mcjty.ariente.api.hologui.components.*;

public class GuiComponentRegistry implements IGuiComponentRegistry {

    @Override
    public IIconButton iconButton(double x, double y, double w, double h) {
        return new HoloButton(x, y, w, h);
    }

    @Override
    public IIcon icon(double x, double y, double w, double h) {
        return new HoloIcon(x, y, w, h);
    }

    @Override
    public IIconChoice iconChoice(double x, double y, double w, double h) {
        return new HoloIcons(x, y, w, h);
    }

    @Override
    public IIconToggle iconToggle(double x, double y, double w, double h) {
        return new HoloToggleIcon(x, y, w, h);
    }

    @Override
    public IModeToggle modeToggle(double x, double y, double w, double h) {
        return new HoloMode(x, y, w, h);
    }

    @Override
    public INumber number(double x, double y, double w, double h) {
        return new HoloNumber(x, y, w, h);
    }

    @Override
    public IPanel panel(double x, double y, double w, double h) {
        return new HoloPanel(x, y, w, h);
    }

    @Override
    public IStackIcon stackIcon(double x, double y, double w, double h) {
        return new HoloStackIcon(x, y, w, h);
    }

    @Override
    public IStackToggle stackToggle(double x, double y, double w, double h) {
        return new HoloStackToggle(x, y, w, h);
    }

    @Override
    public IText text(double x, double y, double w, double h) {
        return new HoloText(x, y, w, h);
    }

    @Override
    public IButton button(double x, double y, double w, double h) {
        return new HoloTextButton(x, y, w, h);
    }
}
