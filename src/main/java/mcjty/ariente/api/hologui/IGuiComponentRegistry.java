package mcjty.ariente.api.hologui;

import mcjty.ariente.api.hologui.components.*;

public interface IGuiComponentRegistry {

    IIconButton iconButton(double x, double y, double w, double h);

    IIcon icon(double x, double y, double w, double h);

    IIconChoice iconChoice(double x, double y, double w, double h);

    IIconToggle iconToggle(double x, double y, double w, double h);

    IModeToggle modeToggle(double x, double y, double w, double h);

    INumber number(double x, double y, double w, double h);

    IPanel panel(double x, double y, double w, double h);

    IStackIcon stackIcon(double x, double y, double w, double h);

    IStackToggle stackToggle(double x, double y, double w, double h);

    IText text(double x, double y, double w, double h);

    IButton button(double x, double y, double w, double h);
}
