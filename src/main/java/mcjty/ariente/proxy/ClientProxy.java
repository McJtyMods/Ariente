package mcjty.ariente.proxy;

import mcjty.ariente.Ariente;
import mcjty.ariente.ClientForgeEventHandlers;
import mcjty.ariente.bindings.KeyBindings;
import mcjty.ariente.bindings.KeyInputHandler;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.cables.BakedModelLoader;
import mcjty.ariente.entities.ModEntities;
import mcjty.ariente.items.ModItems;
import mcjty.lib.McJtyLibClient;
import mcjty.lib.setup.DefaultClientProxy;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends DefaultClientProxy {

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ClientForgeEventHandlers());
        McJtyLibClient.preInit(e);
        OBJLoader.INSTANCE.addDomain(Ariente.MODID);
        ModelLoaderRegistry.registerLoader(new BakedModelLoader());

//        MinecraftForge.EVENT_BUS.register(new ClientForgeEventHandlers());
//        ModelLoaderRegistry.registerLoader(new TankModelLoader());
//        FluidSetup.initRenderer();
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
//        ModBlocks.initItemModels();
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
        KeyBindings.init();
    }

    @SubscribeEvent
    public void colorHandlerEventBlock(ColorHandlerEvent.Block event) {
        ModBlocks.initColorHandlers(event.getBlockColors());
    }


    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
        ModBlocks.initItemModels();
    }

    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        ModBlocks.initModels();
        ModItems.initModels();
        ModEntities.initModels();
    }
}
