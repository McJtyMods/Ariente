package mcjty.ariente.cables;

import mcjty.ariente.Ariente;
import mcjty.ariente.setup.Registration;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.Nullable;

public class CableBlockItem extends BlockItem {

    private final CableColor color;

    public CableBlockItem(Block blockIn, Properties builder, CableColor color) {
        super(blockIn, builder);
        this.color = color;
    }

    @Nullable
    @Override
    protected BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState blockstate = ((GenericCableBlock)this.getBlock()).calculateState(context.getWorld(),
                context.getPos(), this.getBlock().getDefaultState().with(GenericCableBlock.COLOR, color));
        if (canPlace(context, blockstate)) {
            return blockstate;
        } else {
            return null;
        }
    }

    @Override
    public String getTranslationKey() {
        // We don't want the translation key of the block
        return super.getDefaultTranslationKey();
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (Ariente.setup.getTab().equals(group)) {
            items.add(new ItemStack(Registration.NETCABLE_NEGARITE.get()));
            items.add(new ItemStack(Registration.NETCABLE_POSIRITE.get()));
            items.add(new ItemStack(Registration.NETCABLE_COMBINED.get()));
            items.add(new ItemStack(Registration.NETCABLE_DATA.get()));
            items.add(new ItemStack(Registration.CONNECTOR_NEGARITE.get()));
            items.add(new ItemStack(Registration.CONNECTOR_POSIRITE.get()));
            items.add(new ItemStack(Registration.CONNECTOR_COMBINED.get()));
            items.add(new ItemStack(Registration.CONNECTOR_DATA.get()));
        }
    }
}
