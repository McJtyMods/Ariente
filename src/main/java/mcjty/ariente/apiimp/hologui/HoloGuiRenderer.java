package mcjty.ariente.apiimp.hologui;

import mcjty.ariente.api.hologui.IHoloGuiEntity;
import mcjty.ariente.api.hologui.IHoloGuiRenderer;

public class HoloGuiRenderer implements IHoloGuiRenderer {

    @Override
    public void render(IHoloGuiEntity entity, double x, double y, double z, float entityYaw) {
        HoloGuiEntityRender.doActualRender((HoloGuiEntity) entity, x, y, z, entityYaw);
    }

}
