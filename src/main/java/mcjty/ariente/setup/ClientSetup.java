package mcjty.ariente.setup;

import mcjty.ariente.Ariente;
import mcjty.ariente.ClientForgeEventHandlers;
import mcjty.ariente.bindings.KeyInputHandler;
import mcjty.ariente.entities.ModEntities;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {

    public static void init(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ClientForgeEventHandlers());
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
        OBJLoader.INSTANCE.addDomain(Ariente.MODID);
        ModEntities.initModels();
        // @todo 1.14
//        ModelLoaderRegistry.registerLoader(new BakedModelLoader());
//        ModBlocks.initItemModels();
    }
}
