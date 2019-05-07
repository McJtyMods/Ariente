package mcjty.ariente.oregen;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class WorldGen {

    public static void init() {
        GameRegistry.registerWorldGenerator(ArienteOreGen.instance, 5);
        MinecraftForge.EVENT_BUS.register(ArienteOreGen.instance);
    }

}
