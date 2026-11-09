package mcjty.ariente.setup;

import mcjty.ariente.ForgeEventHandlers;
import mcjty.ariente.compat.ArienteTOPCompatibility;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.gui.HoloGuiCompatibility;
import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.setup.DefaultModSetup;
import net.fabricmc.loader.api.FabricLoader;

public class ModSetup extends DefaultModSetup {

    public boolean arienteWorld = false;

    @Override
    public void init() {
        super.init();


    }

    @Override
    protected void setupModCompat() {
        arienteWorld = FabricLoader.getInstance().isModLoaded("arienteworld");
        if (arienteWorld) {

        }

        MainCompatHandler.registerWaila();
        MainCompatHandler.registerTOP();
        ArienteTOPCompatibility.register();
        HoloGuiCompatibility.register();
    }
}
