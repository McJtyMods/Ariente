package mcjty.ariente.blocks.decorative;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

public class PaneBlock extends StainedGlassPaneBlock {

    public PaneBlock(Material materialIn, SoundType soundType) {
        super(Properties.of(materialIn)
                .sound(soundType));
    }
}
