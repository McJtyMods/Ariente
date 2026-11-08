package mcjty.ariente.items;

import javax.annotation.Nullable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;

public class BlockStateItem extends BlockItem {

    private final BlockState state;

    public BlockStateItem(BlockState state, Properties properties) {
        super(state.getBlock(), properties);
        this.state = state;
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        return canPlace(context, state) ? state : null;
    }
}
