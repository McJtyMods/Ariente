package mcjty.ariente.items;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public interface AttributeItem {

    ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack);
}
