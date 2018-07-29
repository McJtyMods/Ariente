package mcjty.ariente.gui;

public interface IGuiTile {

    IGuiComponent createGui(HoloGuiEntity entity, String tag);

    void syncToClient();
}
