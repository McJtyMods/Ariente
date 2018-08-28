package mcjty.ariente.blocks.utility;

import mcjty.ariente.gui.IGuiComponent;
import mcjty.ariente.gui.IGuiTile;
import mcjty.ariente.gui.components.HoloPanel;
import mcjty.ariente.gui.components.HoloText;
import mcjty.lib.tileentity.GenericTileEntity;

public class LevelMarkerTile extends GenericTileEntity implements IGuiTile {

    @Override
    public IGuiComponent createGui(String tag) {
        // @todo implement this
        return new HoloPanel(0, 0, 8, 8)
                .add(new HoloText(0, 2, 1, 1, "Floor name (WIP)", 0xaaccff));
    }

    @Override
    public void syncToClient() {

    }
}
