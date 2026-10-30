package mcjty.ariente.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.world.level.material.MapColor;

public class BaseOreBlock extends BaseBlock {

    public BaseOreBlock() {
        super(new BlockBuilder()
                .properties(Properties.of().mapColor(MapColor.STONE).strength(2.0f, 4.0f).requiresCorrectToolForDrops())
        );
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }
}
