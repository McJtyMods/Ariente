package mcjty.ariente.gui;

public interface IGuiComponent {

    void render(double cursorX, double cursorY);

    void hit(double cursorX, double cursorY);

    boolean isInside(double x, double y);

    double getX();

    double getY();

    double getW();

    double getH();
}
