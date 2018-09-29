package mcjty.ariente.api.hologui.components;

import mcjty.ariente.api.hologui.IEvent;
import mcjty.ariente.api.hologui.IGuiComponent;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.Function;

public interface IIconChoice extends IGuiComponent {

    IIconChoice getter(Function<EntityPlayer, Integer> getter);

    IIconChoice icon(int u, int v);

    IIconChoice hitEvent(IEvent event);
}
