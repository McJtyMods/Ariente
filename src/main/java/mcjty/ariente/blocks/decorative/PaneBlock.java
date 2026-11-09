package mcjty.ariente.blocks.decorative;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class PaneBlock extends IronBarsBlock {

    public PaneBlock(BlockBehaviour.Properties properties, MapColor materialIn, SoundType soundType) {
        super(properties.mapColor(materialIn)
                .sound(soundType));
    }
}
