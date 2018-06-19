package mcjty.ariente;

import mcjty.ariente.biomes.ArienteBiomeProvider;
import net.minecraftforge.event.terraingen.WorldTypeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TerrainGenEventHandlers {

    @SubscribeEvent
    public void onInitBiomeGens(WorldTypeEvent.InitBiomeGens event) {
//        System.out.println("event.getWorldType().getName() = " + event.getWorldType().getName());
//        System.out.println("event.getWorldType().getId() = " + event.getWorldType().getId());
//        if (event.getWorldType().getId() == 222 /*@todo config*/) {
//            event.setNewBiomeGens(ArienteBiomeProvider.makeLayers());
//        }
    }
}
