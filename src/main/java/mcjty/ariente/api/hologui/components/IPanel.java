package mcjty.ariente.api.hologui.components;

import mcjty.ariente.api.hologui.IGuiComponent;

public interface IPanel extends IGuiComponent {
    IPanel add(IGuiComponent... components);
}
