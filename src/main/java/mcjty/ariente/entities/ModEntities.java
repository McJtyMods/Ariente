package mcjty.ariente.entities;

import mcjty.ariente.Ariente;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class ModEntities {

    public static void init() {
        int id = 1;
        EntityRegistry.registerModEntity(new ResourceLocation(Ariente.MODID, "hologui"), HoloGuiEntity.class,
                "hologui", id++, Ariente.instance,64,1,false);

        id = 100;

        EntityRegistry.registerModEntity(new ResourceLocation(Ariente.MODID, "drone"), Drone.class, "drone", id, Ariente.instance, 64, 3, true, 0x222222, 0x555555);
    }

    public static void initModels() {
        RenderingRegistry.registerEntityRenderingHandler(HoloGuiEntity.class, new HoloGuiEntityRender.Factory());
        RenderingRegistry.registerEntityRenderingHandler(Drone.class, DroneRender.FACTORY);
    }
}
