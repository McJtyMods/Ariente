package mcjty.ariente.api.hologui;

import net.minecraft.entity.player.EntityPlayer;

public interface IGuiComponent {

    void render(EntityPlayer player, IHoloGuiEntity holo, double cursorX, double cursorY);

    void renderTooltip(EntityPlayer player, IHoloGuiEntity holo, double cursorX, double cursorY);

    IGuiComponent findHoveringWidget(double cursorX, double cursorY);

    void hit(EntityPlayer player, IHoloGuiEntity entity, double cursorX, double cursorY);

    void hitClient(EntityPlayer player, IHoloGuiEntity entity, double cursorX, double cursorY);

    boolean isInside(double x, double y);

    double getX();

    double getY();

    double getW();

    double getH();
}
