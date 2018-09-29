package mcjty.ariente.api.hologui.components;

import mcjty.ariente.api.hologui.IGuiComponent;

import java.util.function.Supplier;

public interface IText extends IGuiComponent {
    IText text(String text);

    IText text(Supplier<String> text);

    IText color(int color);
}
