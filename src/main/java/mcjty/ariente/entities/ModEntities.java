package mcjty.ariente.entities;

import mcjty.ariente.Ariente;
import mcjty.ariente.gui.HoloGuiEntity;
import mcjty.ariente.gui.HoloGuiEntityRender;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class ModEntities {

    public static void init() {
        int id = 1;
        EntityRegistry.registerModEntity(new ResourceLocation(Ariente.MODID, "ariente_hologui"), HoloGuiEntity.class,
                "ariente_hologui", id++, Ariente.instance, 64, 1, false);
        EntityRegistry.registerModEntity(new ResourceLocation(Ariente.MODID, "ariente_laser"), LaserEntity.class,
                "ariente_laser", id++, Ariente.instance, 64, 1, false);

        id = 100;

        EntityRegistry.registerModEntity(new ResourceLocation(Ariente.MODID, "ariente_drone"), DroneEntity.class, "ariente_drone", id++, Ariente.instance, 64, 3, true, 0x222222, 0x555555);
        EntityRegistry.registerModEntity(new ResourceLocation(Ariente.MODID, "ariente_sentinel_drone"), SentinelDroneEntity.class, "ariente_sentinel_drone", id++, Ariente.instance, 64, 3, true, 0x555555, 0x999999);
        EntityRegistry.registerModEntity(new ResourceLocation(Ariente.MODID, "soldier"), SoldierEntity.class, "ariente_soldier", id++, Ariente.instance, 64, 3, true, 0x996600, 0x00ff00);
    }

    public static void initModels() {
        RenderingRegistry.registerEntityRenderingHandler(HoloGuiEntity.class, new HoloGuiEntityRender.Factory());
        RenderingRegistry.registerEntityRenderingHandler(LaserEntity.class, new LaserRender.Factory());
        RenderingRegistry.registerEntityRenderingHandler(DroneEntity.class, DroneRender.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(SentinelDroneEntity.class, SentinelDroneRender.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(SoldierEntity.class, SoldierRender.FACTORY);
    }
}
