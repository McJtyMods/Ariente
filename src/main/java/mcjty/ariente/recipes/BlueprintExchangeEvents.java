package mcjty.ariente.recipes;

import mcjty.ariente.items.ItemData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BlueprintExchangeEvents {

    public static void crafted(Player player, ItemStack crafted) {
        CompoundTag data = BlueprintExchangeRecipe.previewData(crafted);
        if (player.level().isClientSide || data == null) {
            return;
        }
        // Shift-click delivers a copy before firing this event with another copy of the result.
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack output = player.getInventory().getItem(slot);
            if (BlueprintExchangeRecipe.isPreview(output) && data.equals(BlueprintExchangeRecipe.previewData(output))) {
                if (BlueprintExchangeRecipe.resolve(output, player.getRandom())) {
                    ItemData.set(crafted, ItemData.get(output).copy());
                }
                player.getInventory().setChanged();
                return;
            }
        }
        BlueprintExchangeRecipe.resolve(crafted, player.getRandom());
        player.getInventory().setChanged();
    }
}
