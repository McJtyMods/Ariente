package mcjty.ariente.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.world.level.material.Material;

public class BaseOreBlock extends BaseBlock {

    public BaseOreBlock() {
        super(new BlockBuilder()
                .properties(Properties.of(Material.STONE)
//                    .hardnessAndResistance(2.0f, 4.0f)
                )
                // @todo 1.18 .harvestLevel(ToolType.PICKAXE, 1)
        );
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }
}
