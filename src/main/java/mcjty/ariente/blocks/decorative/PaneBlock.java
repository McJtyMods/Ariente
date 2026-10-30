package mcjty.ariente.blocks.decorative;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.material.MapColor;

public class PaneBlock extends IronBarsBlock {

    public PaneBlock(MapColor materialIn, SoundType soundType) {
        super(Properties.of().mapColor(materialIn)
                .sound(soundType));
    }
}
