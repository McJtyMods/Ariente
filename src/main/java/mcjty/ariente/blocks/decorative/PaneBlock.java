package mcjty.ariente.blocks.decorative;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

import net.minecraft.block.AbstractBlock.Properties;

public class PaneBlock extends net.minecraft.block.PaneBlock {

    public PaneBlock(Material materialIn, SoundType soundType) {
        super(Properties.of(materialIn)
                .sound(soundType));
    }
}
