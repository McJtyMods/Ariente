package mcjty.ariente.recipes;

import mcjty.ariente.Ariente;
import mcjty.ariente.items.ItemData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Ariente.MODID)
public class BlueprintExchangeEvents {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void crafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        ItemStack crafted = event.getCrafting();
        CompoundTag data = BlueprintExchangeRecipe.previewData(crafted);
        if (player.level().isClientSide() || data == null) {
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
