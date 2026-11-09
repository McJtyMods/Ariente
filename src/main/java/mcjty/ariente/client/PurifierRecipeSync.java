package mcjty.ariente.client;

import java.util.List;
import mcjty.ariente.Ariente;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.recipes.PurifierRecipes;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

@EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public final class PurifierRecipeSync {

    @SubscribeEvent
    public static void received(RecipesReceivedEvent event) {
        PurifierRecipes.receivedRecipes(event.getRecipeMap().byType(BlueprintRecipeRegistry.PURIFYING.get()).stream().map(RecipeHolder::value).toList());
    }

    @SubscribeEvent
    public static void disconnected(ClientPlayerNetworkEvent.LoggingOut event) {
        PurifierRecipes.receivedRecipes(List.of());
    }
}
