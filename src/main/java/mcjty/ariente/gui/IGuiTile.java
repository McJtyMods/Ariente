package mcjty.ariente.gui;

import mcjty.ariente.entities.HoloGuiEntity;

public interface IGuiTile {

    public void renderGui(HoloGuiEntity entity);

    public void clickGui(HoloGuiEntity entity, int x, int y);
}
