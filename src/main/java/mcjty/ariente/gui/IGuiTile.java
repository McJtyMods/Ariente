package mcjty.ariente.gui;

import mcjty.ariente.entities.HoloGuiEntity;

public interface IGuiTile {

    IGuiComponent createGui(HoloGuiEntity entity, String tag);

    void syncToServer();
}
