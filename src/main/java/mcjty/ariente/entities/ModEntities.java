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
    }

    public static void initModels() {
        RenderingRegistry.registerEntityRenderingHandler(HoloGuiEntity.class, new HoloGuiEntityRender.Factory());
    }
}
