package mcjty.ariente.entities;

import mcjty.ariente.Ariente;
import mcjty.ariente.entities.drone.DroneEntity;
import mcjty.ariente.entities.drone.DroneRender;
import mcjty.ariente.entities.drone.SentinelDroneEntity;
import mcjty.ariente.entities.drone.SentinelDroneRender;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.entities.fluxship.FluxShipRender;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.entities.levitator.FluxLevitatorRender;
import mcjty.ariente.entities.soldier.MasterSoldierEntity;
import mcjty.ariente.entities.soldier.SoldierEntity;
import mcjty.ariente.entities.soldier.SoldierRender;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class ModEntities {

    public static void init() {
        int id = 1;
        EntityRegistry.registerModEntity(new ResourceLocation(Ariente.MODID, "ariente_laser"), LaserEntity.class,
                "ariente_laser", id++, Ariente.instance, 64, 1, false);
        EntityRegistry.registerModEntity(new ResourceLocation(Ariente.MODID, "ariente_flux_levitator"), FluxLevitatorEntity.class,
                "ariente_flux_levitator", id++, Ariente.instance, 64, 1, false);
        EntityRegistry.registerModEntity(new ResourceLocation(Ariente.MODID, "ariente_flux_ship"), FluxShipEntity.class,
                "ariente_flux_ship", id++, Ariente.instance, 64, 1, false);

        id = 100;

        EntityRegistry.registerModEntity(new ResourceLocation(Ariente.MODID, "ariente_drone"), DroneEntity.class, "ariente_drone", id++, Ariente.instance, 64, 3, true, 0x222222, 0x555555);
        EntityRegistry.registerModEntity(new ResourceLocation(Ariente.MODID, "ariente_sentinel_drone"), SentinelDroneEntity.class, "ariente_sentinel_drone", id++, Ariente.instance, 64, 3, true, 0x555555, 0x999999);
        EntityRegistry.registerModEntity(new ResourceLocation(Ariente.MODID, "soldier"), SoldierEntity.class, "ariente_soldier", id++, Ariente.instance, 64, 3, true, 0x996600, 0x00ff00);
        EntityRegistry.registerModEntity(new ResourceLocation(Ariente.MODID, "master_soldier"), MasterSoldierEntity.class, "ariente_master_soldier", id++, Ariente.instance, 64, 3, true, 0x996600, 0x00ff00);
    }

    public static void initModels() {
        RenderingRegistry.registerEntityRenderingHandler(LaserEntity.class, new LaserRender.Factory());
        RenderingRegistry.registerEntityRenderingHandler(DroneEntity.class, DroneRender.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(SentinelDroneEntity.class, SentinelDroneRender.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(SoldierEntity.class, SoldierRender.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(MasterSoldierEntity.class, SoldierRender.MASTER_FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(FluxLevitatorEntity.class, new FluxLevitatorRender.Factory());
        RenderingRegistry.registerEntityRenderingHandler(FluxShipEntity.class, new FluxShipRender.Factory());
    }
}
