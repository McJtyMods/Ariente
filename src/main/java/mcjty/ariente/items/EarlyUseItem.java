package mcjty.ariente.items;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public interface EarlyUseItem {

    InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context);
}
