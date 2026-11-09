package mcjty.ariente.setup;

import mcjty.ariente.ForgeEventHandlers;
import mcjty.ariente.compat.ArienteTOPCompatibility;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.gui.HoloGuiCompatibility;
import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.setup.DefaultModSetup;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

public class ModSetup extends DefaultModSetup {

    public boolean arienteWorld = false;

    @Override
    public void init(FMLCommonSetupEvent e) {
        super.init(e);

        NeoForge.EVENT_BUS.register(new ForgeEventHandlers());

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
