package mcjty.ariente.api.hologui.components;

import mcjty.ariente.api.hologui.IEvent;
import mcjty.ariente.api.hologui.IGuiComponent;

public interface IButton extends IGuiComponent {
    IButton text(String text);

    IButton color(int color);

    IButton hitEvent(IEvent event);

    IButton hitClientEvent(IEvent event);
}
