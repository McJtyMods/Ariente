package mcjty.ariente.setup;

import mcjty.ariente.ForgeEventHandlers;
import mcjty.ariente.compat.ArienteTOPCompatibility;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.gui.HoloGuiCompatibility;
import mcjty.ariente.network.ArienteMessages;
import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.setup.DefaultModSetup;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModSetup extends DefaultModSetup {

    public boolean arienteWorld = false;

    @Override
    public void init(FMLCommonSetupEvent e) {
        super.init(e);

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());

        ArienteMessages.registerMessages("ariente");
    }

    @Override
    protected void setupModCompat() {
        arienteWorld = ModList.get().isLoaded("arienteworld");
        if (arienteWorld) {
            ArienteWorldCompat.register();
        }

        MainCompatHandler.registerWaila();
        MainCompatHandler.registerTOP();
        ArienteTOPCompatibility.register();
        HoloGuiCompatibility.register();
    }
}
