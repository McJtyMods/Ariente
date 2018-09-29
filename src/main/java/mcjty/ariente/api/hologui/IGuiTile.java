package mcjty.ariente.api.hologui;

public interface IGuiTile {

    IGuiComponent createGui(String tag, IGuiComponentRegistry registry);

    void syncToClient();
}
