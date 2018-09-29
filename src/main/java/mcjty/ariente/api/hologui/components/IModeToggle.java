package mcjty.ariente.api.hologui.components;

import mcjty.ariente.api.hologui.IEvent;
import mcjty.ariente.api.hologui.IGuiComponent;

import java.util.function.Supplier;

public interface IModeToggle extends IGuiComponent {
    IModeToggle getter(Supplier<Integer> getter);

    IModeToggle hitEvent(IEvent event);

    IModeToggle choice(int u, int v);
}
