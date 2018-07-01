package mcjty.ariente.gui.components;

import mcjty.ariente.entities.HoloGuiEntity;
import mcjty.ariente.gui.IGuiComponent;
import net.minecraft.entity.player.EntityPlayer;

public interface IEvent {

    void hit(IGuiComponent component, EntityPlayer player, HoloGuiEntity entity, double x, double y);
}
