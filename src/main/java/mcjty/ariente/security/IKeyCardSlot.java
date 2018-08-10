package mcjty.ariente.security;

import net.minecraft.item.ItemStack;

/**
 * Implement this interface on any TE that needs to be able
 * to access keycards
 */
public interface IKeyCardSlot {

    void acceptKeyCard(ItemStack stack);
}
