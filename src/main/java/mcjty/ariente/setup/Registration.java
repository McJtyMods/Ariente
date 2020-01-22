package mcjty.ariente.setup;


import mcjty.ariente.ModCrafting;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.entities.EntityArientePearl;
import mcjty.ariente.entities.LaserEntity;
import mcjty.ariente.entities.drone.DroneEntity;
import mcjty.ariente.entities.drone.SentinelDroneEntity;
import mcjty.ariente.entities.fluxelevator.FluxElevatorEntity;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.entities.soldier.MasterSoldierEntity;
import mcjty.ariente.entities.soldier.SoldierEntity;
import mcjty.ariente.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static mcjty.ariente.Ariente.MODID;

public class Registration {

    public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<TileEntityType<?>> TILES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, MODID);
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = new DeferredRegister<>(ForgeRegistries.CONTAINERS, MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = new DeferredRegister<>(ForgeRegistries.SOUND_EVENTS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = new DeferredRegister<>(ForgeRegistries.ENTITIES, MODID);

    public static void register() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<EntityType<LaserEntity>> LASER = ENTITIES.register("ariente_laser", () -> {
        return EntityType.Builder.create(LaserEntity::new, EntityClassification.MISC)
                .size(1, 1)
                .setShouldReceiveVelocityUpdates(false)
                .build("ariente_laser");
    });
    public static final RegistryObject<EntityType<FluxLevitatorEntity>> FLUX_LEVITATOR = ENTITIES.register("ariente_flux_levitator", () -> {
        return EntityType.Builder.create(FluxLevitatorEntity::new, EntityClassification.MISC)
                .size(1.30F, 0.9F)
                .setShouldReceiveVelocityUpdates(false)
                .build("ariente_flux_levitator");
    });
    public static final RegistryObject<EntityType<FluxShipEntity>> FLUX_SHIP = ENTITIES.register("ariente_flux_ship", () -> {
        return EntityType.Builder.create(FluxShipEntity::new, EntityClassification.MISC)
                .size(2.50F, 1.5F)
                .setShouldReceiveVelocityUpdates(false)
                .build("ariente_flux_ship");
    });
    public static final RegistryObject<EntityType<EntityArientePearl>> PEARL = ENTITIES.register("ariente_ariente_pearl", () -> {
        return EntityType.Builder.create(EntityArientePearl::new, EntityClassification.MISC)
                .size(0.25F, 0.25F)
                .setShouldReceiveVelocityUpdates(false)
                .build("ariente_ariente_pearl");
    });
    public static final RegistryObject<EntityType<FluxElevatorEntity>> ELEVATOR = ENTITIES.register("ariente_flux_elevator", () -> {
        return EntityType.Builder.create(FluxElevatorEntity::new, EntityClassification.MISC)
                .size(1.30F, 0.9F)
                .setShouldReceiveVelocityUpdates(false)
                .build("ariente_flux_elevator");
    });
    public static final RegistryObject<EntityType<DroneEntity>> DRONE = ENTITIES.register("ariente_drone", () -> {
        return EntityType.Builder.create(DroneEntity::new, EntityClassification.MISC)
                .size(2.0F, 2.0F)
                .setShouldReceiveVelocityUpdates(false)
                .build("ariente_drone");
    });
    public static final RegistryObject<EntityType<SentinelDroneEntity>> SENTINEL_DRONE = ENTITIES.register("ariente_sentinel_drone", () -> {
        return EntityType.Builder.create(SentinelDroneEntity::new, EntityClassification.MISC)
                .size(1.3F, 1.3F)
                .setShouldReceiveVelocityUpdates(false)
                .build("ariente_sentinel_drone");
    });
    public static final RegistryObject<EntityType<SoldierEntity>> SOLDIER = ENTITIES.register("soldier", () -> {
        return EntityType.Builder.create(SoldierEntity::new, EntityClassification.MISC)
                .size(0.6F, 1.95F)
                .setShouldReceiveVelocityUpdates(false)
                .build("soldier");
    });
    public static final RegistryObject<EntityType<MasterSoldierEntity>> MASTER_SOLDIER = ENTITIES.register("master_soldier", () -> {
        return EntityType.Builder.create(MasterSoldierEntity::new, EntityClassification.MISC)
                .size(0.7F, 2.7F)
                .setShouldReceiveVelocityUpdates(false)
                .build("master_soldier");
    });

    // @todo 1.14
    public static void registerItems() {
        ModBlocks.initOreDict();
        ModItems.initOreDict();
        ModCrafting.init();
    }
}
