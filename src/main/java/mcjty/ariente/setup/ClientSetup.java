package mcjty.ariente.setup;

import mcjty.ariente.Ariente;
import mcjty.ariente.ClientForgeEventHandlers;
import mcjty.ariente.bindings.KeyInputHandler;
import mcjty.ariente.blocks.generators.NegariteTankRenderer;
import mcjty.ariente.blocks.generators.PosiriteTankRenderer;
import mcjty.ariente.blocks.utility.ElevatorRenderer;
import mcjty.ariente.blocks.utility.StorageRenderer;
import mcjty.ariente.blocks.utility.door.DoorMarkerRenderer;
import mcjty.ariente.blocks.utility.door.InvisibleDoorRenderer;
import mcjty.ariente.cables.CableModelLoader;
import mcjty.ariente.cables.CableRenderer;
import mcjty.ariente.client.ArienteSpriteUploader;
import mcjty.ariente.entities.LaserRender;
import mcjty.ariente.entities.RenderArientePearl;
import mcjty.ariente.entities.drone.DroneRender;
import mcjty.ariente.entities.drone.SentinelDroneRender;
import mcjty.ariente.entities.fluxelevator.FluxElevatorRender;
import mcjty.ariente.entities.fluxship.FluxShipRender;
import mcjty.ariente.entities.levitator.FluxLevitatorRender;
import mcjty.ariente.entities.soldier.SoldierRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static mcjty.ariente.blocks.generators.NegariteTankRenderer.NEGARITE_BEAM;
import static mcjty.ariente.blocks.generators.PosiriteTankRenderer.POSIRITE_BEAM;
import static mcjty.ariente.blocks.utility.ElevatorRenderer.ELEVATOR_BEAM;

public class ClientSetup {

    public static void init(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ClientForgeEventHandlers());
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());

        NegariteTankRenderer.register();
        PosiriteTankRenderer.register();
        ElevatorRenderer.register();
        StorageRenderer.register();
        DoorMarkerRenderer.register();
        InvisibleDoorRenderer.register();
        CableRenderer.register(Registration.NETCABLE_TILE.get());
        CableRenderer.register(Registration.CONNECTOR_TILE.get());

        RenderTypeLookup.setRenderLayer(Registration.GLASS_FENCE.get(), RenderType.getTranslucent());
        RenderTypeLookup.setRenderLayer(Registration.BLUE_GLASS_FENCE.get(), RenderType.getTranslucent());
    }

    public static void initModels(ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(new ResourceLocation(Ariente.MODID, "cableloader"), new CableModelLoader());
        RenderingRegistry.registerEntityRenderingHandler(Registration.ENTITY_LASER.get(), new LaserRender.Factory());
        RenderingRegistry.registerEntityRenderingHandler(Registration.ENTITY_DRONE.get(), DroneRender.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(Registration.ENTITY_SENTINEL_DRONE.get(), SentinelDroneRender.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(Registration.ENTITY_SOLDIER.get(), SoldierRender.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(Registration.ENTITY_MASTER_SOLDIER.get(), SoldierRender.MASTER_FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(Registration.ENTITY_FLUX_LEVITATOR.get(), new FluxLevitatorRender.Factory());
        RenderingRegistry.registerEntityRenderingHandler(Registration.ENTITY_ELEVATOR.get(), new FluxElevatorRender.Factory());
        RenderingRegistry.registerEntityRenderingHandler(Registration.ENTITY_FLUX_SHIP.get(), new FluxShipRender.Factory());
        RenderingRegistry.registerEntityRenderingHandler(Registration.ENTITY_PEARL.get(), RenderArientePearl.FACTORY);
    }

    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if (!event.getMap().getTextureLocation().equals(AtlasTexture.LOCATION_BLOCKS_TEXTURE)) {
            return;
        }
        event.addSprite(NEGARITE_BEAM);
        event.addSprite(POSIRITE_BEAM);
        event.addSprite(ELEVATOR_BEAM);
    }

    public static void setupSpriteUploader() {
        IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        if (resourceManager instanceof IReloadableResourceManager) {
            ArienteSpriteUploader.INSTANCE = new ArienteSpriteUploader(Minecraft.getInstance().getTextureManager());
            ((IReloadableResourceManager) resourceManager).addReloadListener(ArienteSpriteUploader.INSTANCE);
        }
    }
}