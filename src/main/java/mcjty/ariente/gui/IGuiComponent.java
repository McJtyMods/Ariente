package mcjty.ariente.gui;

import mcjty.ariente.entities.HoloGuiEntity;
import net.minecraft.entity.player.EntityPlayer;

public interface IGuiComponent {

    void render(double cursorX, double cursorY);

    void hit(EntityPlayer player, HoloGuiEntity entity, double cursorX, double cursorY);

    void hitClient(EntityPlayer player, HoloGuiEntity entity, double cursorX, double cursorY);

    boolean isInside(double x, double y);

    double getX();

    double getY();

    double getW();

    double getH();
}
