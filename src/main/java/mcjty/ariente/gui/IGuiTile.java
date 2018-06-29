package mcjty.ariente.gui;

import mcjty.ariente.entities.HoloGuiEntity;

public interface IGuiTile {

    public IGuiComponent createGui(HoloGuiEntity entity);

    public void clickGui(HoloGuiEntity entity, int x, int y);
}
