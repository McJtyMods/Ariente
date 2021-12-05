package mcjty.ariente.blocks.decorative;

import mcjty.ariente.api.MarbleColor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.material.Material;
import net.minecraft.state.StateContainer;

import static mcjty.ariente.api.MarbleColor.COLOR;

import net.minecraft.block.AbstractBlock.Properties;

public class MarbleSlabBlock extends SlabBlock {

    public MarbleSlabBlock() {
        super(Properties.of(Material.STONE));
        BlockState blockState = this.defaultBlockState();
        this.registerDefaultState(blockState.setValue(COLOR, MarbleColor.BLACK));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(COLOR);
    }
}
