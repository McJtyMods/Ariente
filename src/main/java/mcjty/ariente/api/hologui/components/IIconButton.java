package mcjty.ariente.api.hologui.components;

import mcjty.ariente.api.hologui.IEvent;
import mcjty.ariente.api.hologui.IGuiComponent;

public interface IIconButton extends IGuiComponent {

    IIconButton image(int u, int v);

    IIconButton hover(int u, int v);

    IIconButton hitEvent(IEvent event);

    IIconButton hitClientEvent(IEvent event);
}
