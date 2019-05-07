package mcjty.ariente.setup;

import mcjty.ariente.Ariente;
import mcjty.ariente.ForgeEventHandlers;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.config.ConfigSetup;
import mcjty.ariente.entities.ModEntities;
import mcjty.ariente.gui.HoloGuiCompatibility;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.oregen.WorldGen;
import mcjty.ariente.oregen.WorldTickHandler;
import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.setup.DefaultModSetup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class ModSetup extends DefaultModSetup {

    public boolean arienteWorld = false;

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);

        MinecraftForge.EVENT_BUS.register(WorldTickHandler.instance);
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        NetworkRegistry.INSTANCE.registerGuiHandler(Ariente.instance, new GuiProxy());

        ArienteMessages.registerMessages("ariente");

        ModBlocks.init();
        ModItems.init();
        ModEntities.init();
        WorldGen.init();
    }

    @Override
    protected void setupModCompat() {
        arienteWorld = Loader.isModLoaded("arienteworld");
        if (arienteWorld) {
            ArienteWorldCompat.register();
        }

        MainCompatHandler.registerWaila();
        MainCompatHandler.registerTOP();
        HoloGuiCompatibility.register();
    }

    @Override
    protected void setupConfig() {
        ConfigSetup.init();
    }

    @Override
    public void createTabs() {
        createTab("ariente", () -> new ItemStack(ModBlocks.posiriteGeneratorBlock));
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        ConfigSetup.postInit();

    }
}
