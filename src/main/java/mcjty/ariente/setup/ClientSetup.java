package mcjty.ariente.setup;

import mcjty.ariente.ClientForgeEventHandlers;
import mcjty.ariente.bindings.KeyInputHandler;
import mcjty.ariente.entities.ModEntities;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {

    public static void init(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ClientForgeEventHandlers());
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
        ModEntities.initModels();
        // @todo 1.14
//        ModelLoaderRegistry.registerLoader(new BakedModelLoader());
//        ModBlocks.initItemModels();
    }
}
