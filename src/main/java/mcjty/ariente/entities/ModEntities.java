package mcjty.ariente.entities;

import mcjty.ariente.entities.drone.DroneEntity;
import mcjty.ariente.entities.drone.DroneRender;
import mcjty.ariente.entities.drone.SentinelDroneEntity;
import mcjty.ariente.entities.drone.SentinelDroneRender;
import mcjty.ariente.entities.fluxelevator.FluxElevatorEntity;
import mcjty.ariente.entities.fluxelevator.FluxElevatorRender;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.entities.fluxship.FluxShipRender;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.entities.levitator.FluxLevitatorRender;
import mcjty.ariente.entities.soldier.MasterSoldierEntity;
import mcjty.ariente.entities.soldier.SoldierEntity;
import mcjty.ariente.entities.soldier.SoldierRender;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ModEntities {

    public static void initModels() {
        RenderingRegistry.registerEntityRenderingHandler(LaserEntity.class, new LaserRender.Factory());
        RenderingRegistry.registerEntityRenderingHandler(DroneEntity.class, DroneRender.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(SentinelDroneEntity.class, SentinelDroneRender.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(SoldierEntity.class, SoldierRender.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(MasterSoldierEntity.class, SoldierRender.MASTER_FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(FluxLevitatorEntity.class, new FluxLevitatorRender.Factory());
        RenderingRegistry.registerEntityRenderingHandler(FluxElevatorEntity.class, new FluxElevatorRender.Factory());
        RenderingRegistry.registerEntityRenderingHandler(FluxShipEntity.class, new FluxShipRender.Factory());
        RenderingRegistry.registerEntityRenderingHandler(EntityArientePearl.class, RenderArientePearl.FACTORY);
    }
}
