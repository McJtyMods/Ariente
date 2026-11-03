package mcjty.ariente.setup;

import mcjty.ariente.display.client.DisplayClientEvents;
import mcjty.ariente.display.client.DisplayModuleRenderers;
import mcjty.ariente.blocks.storagecube.client.StorageCubeRenderer;
import mcjty.ariente.display.client.DisplayRenderer;

import mcjty.ariente.multipart.MultipartRenderer;
import mcjty.ariente.items.BlueprintRenderer;
import mcjty.ariente.ClientForgeEventHandlers;
import mcjty.ariente.bindings.KeyInputHandler;
import mcjty.ariente.blocks.generators.NegariteTankRenderer;
import mcjty.ariente.blocks.generators.PosiriteTankRenderer;
import mcjty.ariente.blocks.utility.ElevatorRenderer;
import mcjty.ariente.blocks.utility.StorageRenderer;
import mcjty.ariente.blocks.utility.SmelterRenderer;
import mcjty.ariente.blocks.utility.WarperRenderer;
import mcjty.ariente.blocks.utility.autofield.AutoFieldRenderer;
import mcjty.ariente.blocks.utility.door.DoorMarkerRenderer;
import mcjty.ariente.blocks.utility.door.InvisibleDoorRenderer;
import mcjty.ariente.cables.CableModelLoader;
import mcjty.ariente.cables.CableRenderer;
import mcjty.ariente.client.ArienteSpriteUploader;
import mcjty.ariente.entities.LaserRender;
import mcjty.ariente.entities.RenderArientePearl;
import mcjty.ariente.entities.fluxelevator.FluxElevatorRender;
import mcjty.ariente.entities.fluxship.FluxShipRender;
import mcjty.ariente.entities.levitator.FluxLevitatorRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {

    public static void init(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ClientForgeEventHandlers());
        MinecraftForge.EVENT_BUS.register(new DisplayClientEvents());
        DisplayModuleRenderers.init();
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());

        NegariteTankRenderer.register();
        PosiriteTankRenderer.register();
        ElevatorRenderer.register();
        StorageRenderer.register();
        SmelterRenderer.register();
        DoorMarkerRenderer.register();
        InvisibleDoorRenderer.register();
        AutoFieldRenderer.register();
        WarperRenderer.register();
        CableRenderer.register(Registration.NETCABLE_TILE.get());
        CableRenderer.register(Registration.CONNECTOR_TILE.get());

        ItemBlockRenderTypes.setRenderLayer(Registration.GLASS_FENCE.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(Registration.BLUE_GLASS_FENCE.get(), RenderType.translucent());
    }

    public static void additionalModels(ModelEvent.RegisterAdditional event) {
        event.register(FluxShipRender.MODEL);
        event.register(BlueprintRenderer.BASE_MODEL);
    }

    public static void initModels(ModelEvent.RegisterGeometryLoaders event) {
        event.register("cableloader", new CableModelLoader());
    }

    @SubscribeEvent
    public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(Registration.DISPLAY_BASE_TILE.get(), DisplayRenderer::new);
        event.registerBlockEntityRenderer(Registration.HOLO_STORAGE_TILE.get(), StorageCubeRenderer::new);
        event.registerBlockEntityRenderer(Registration.TYPE_MULTIPART.get(), MultipartRenderer::new);
        event.registerEntityRenderer(Registration.ENTITY_LASER.get(), new LaserRender.Factory());
        event.registerEntityRenderer(Registration.ENTITY_FLUX_LEVITATOR.get(), new FluxLevitatorRender.Factory());
        event.registerEntityRenderer(Registration.ENTITY_ELEVATOR.get(), new FluxElevatorRender.Factory());
        event.registerEntityRenderer(Registration.ENTITY_FLUX_SHIP.get(), new FluxShipRender.Factory());
        event.registerEntityRenderer(Registration.ENTITY_PEARL.get(), RenderArientePearl.FACTORY);
    }

    public static void setupSpriteUploader() {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        if (resourceManager instanceof ReloadableResourceManager) {
            ArienteSpriteUploader.INSTANCE = new ArienteSpriteUploader(Minecraft.getInstance().getTextureManager());
            ((ReloadableResourceManager) resourceManager).registerReloadListener(ArienteSpriteUploader.INSTANCE);
        }
    }
}
