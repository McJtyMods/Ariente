package mcjty.ariente.facade;

import net.minecraft.world.level.block.state.BlockBehaviour;
import mcjty.ariente.cables.NetCableBlock;

public class FacadeBlock extends NetCableBlock {

    public static final String FACADE = "facade";

    public FacadeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

}
