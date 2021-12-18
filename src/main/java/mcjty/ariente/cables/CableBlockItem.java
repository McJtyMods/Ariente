package mcjty.ariente.cables;

import mcjty.ariente.Ariente;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class CableBlockItem extends BlockItem {

    private final CableColor color;

    public CableBlockItem(Block blockIn, Properties builder, CableColor color) {
        super(blockIn, builder);
        this.color = color;
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState blockstate = ((GenericCableBlock)this.getBlock()).calculateState(context.getLevel(),
                context.getClickedPos(), this.getBlock().defaultBlockState().setValue(GenericCableBlock.COLOR, color));
        if (canPlace(context, blockstate)) {
            return blockstate;
        } else {
            return null;
        }
    }

    @Override
    public String getDescriptionId() {
        // We don't want the translation key of the block
        return super.getOrCreateDescriptionId();
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (Ariente.setup.getTab().equals(group)) {
            items.add(new ItemStack(this));
        }
    }
}
