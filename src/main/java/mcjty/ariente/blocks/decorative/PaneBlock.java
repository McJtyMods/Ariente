package mcjty.ariente.blocks.decorative;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.material.Material;

public class PaneBlock extends IronBarsBlock {

    public PaneBlock(Material materialIn, SoundType soundType) {
        super(Properties.of(materialIn)
                .sound(soundType));
    }
}
