package mcjty.ariente.cables;

import mcjty.ariente.Ariente;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.Nullable;

import net.minecraft.item.Item.Properties;

public class CableBlockItem extends BlockItem {

    private final CableColor color;

    public CableBlockItem(Block blockIn, Properties builder, CableColor color) {
        super(blockIn, builder);
        this.color = color;
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockItemUseContext context) {
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
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
        if (Ariente.setup.getTab().equals(group)) {
            items.add(new ItemStack(this));
        }
    }
}
