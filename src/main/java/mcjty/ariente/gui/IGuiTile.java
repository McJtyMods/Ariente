package mcjty.ariente.gui;

public interface IGuiTile {

    IGuiComponent createGui(String tag);

    void syncToClient();
}
