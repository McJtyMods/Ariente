package mcjty.ariente.facade;

import mcjty.ariente.cables.NetCableBlock;
import net.minecraft.world.level.material.MapColor;

public class FacadeBlock extends NetCableBlock {

    public static final String FACADE = "facade";

    public FacadeBlock() {
        super(MapColor.METAL);
    }

}
