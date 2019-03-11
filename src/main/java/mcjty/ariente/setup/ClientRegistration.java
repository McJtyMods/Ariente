package mcjty.ariente.setup;


import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.entities.ModEntities;
import mcjty.ariente.items.ModItems;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientRegistration {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModBlocks.initModels();
        ModItems.initModels();
        ModEntities.initModels();
    }

}
