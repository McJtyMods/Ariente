package mcjty.ariente.blocks.decorative;

import mcjty.ariente.api.MarbleColor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.material.Material;
import net.minecraft.state.StateContainer;

import static mcjty.ariente.api.MarbleColor.COLOR;

public class MarbleSlabBlock extends SlabBlock {

    public MarbleSlabBlock() {
        super(Properties.create(Material.ROCK));
        BlockState BlockState = this.getDefaultState();
        this.setDefaultState(BlockState.with(COLOR, MarbleColor.BLACK));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(COLOR);
    }
}
