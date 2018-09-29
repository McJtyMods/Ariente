package mcjty.ariente.api.hologui.components;

import mcjty.ariente.api.hologui.IEvent;
import mcjty.ariente.api.hologui.IGuiComponent;
import mcjty.ariente.apiimp.hologui.components.HoloToggleIcon;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.Function;

public interface IIconToggle extends IGuiComponent {
    IIconToggle getter(Function<EntityPlayer, Boolean> getter);

    IIconToggle image(int u, int v);

    IIconToggle selected(int u, int v);

    IIconToggle hitEvent(IEvent event);
}
