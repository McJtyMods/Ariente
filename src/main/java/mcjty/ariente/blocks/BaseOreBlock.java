package mcjty.ariente.blocks;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.world.level.material.MapColor;

public class BaseOreBlock extends BaseBlock {

    public BaseOreBlock(BlockBehaviour.Properties properties) {
        super(new BlockBuilder().properties(properties.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
                .properties(properties.mapColor(MapColor.STONE).strength(2.0f, 4.0f).requiresCorrectToolForDrops())
        );
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }
}
