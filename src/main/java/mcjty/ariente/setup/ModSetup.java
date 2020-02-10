package mcjty.ariente.setup;

import mcjty.ariente.ForgeEventHandlers;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.gui.HoloGuiCompatibility;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.oregen.WorldGen;
import mcjty.ariente.potions.ModPotions;
import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.setup.DefaultModSetup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModSetup extends DefaultModSetup {

    public boolean arienteWorld = false;

    public ModSetup() {
        createTab("ariente", () -> new ItemStack(ModBlocks.POSIRITE_GENERATOR.get()));
    }

    @Override
    public void init(FMLCommonSetupEvent e) {
        super.init(e);

//        MinecraftForge.EVENT_BUS.register(WorldTickHandler.instance); // @todo 1.14
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
//        NetworkRegistry.INSTANCE.registerGuiHandler(Ariente.instance, new GuiProxy());

        ArienteMessages.registerMessages("ariente");

        ModItems.init();
        WorldGen.init();
        ModPotions.init();
    }

    @Override
    protected void setupModCompat() {
        arienteWorld = ModList.get().isLoaded("arienteworld");
        if (arienteWorld) {
            ArienteWorldCompat.register();
        }

        MainCompatHandler.registerWaila();
        MainCompatHandler.registerTOP();
        HoloGuiCompatibility.register();
    }
}
