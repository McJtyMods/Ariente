package mcjty.ariente.setup;

import mcjty.ariente.Ariente;
import mcjty.ariente.ClientForgeEventHandlers;
import mcjty.ariente.bindings.KeyInputHandler;
import mcjty.ariente.blocks.generators.NegariteTankRenderer;
import mcjty.ariente.blocks.generators.PosiriteTankRenderer;
import mcjty.ariente.blocks.purifier.client.PurifierRingParticle;
import mcjty.ariente.blocks.purifier.client.PurifierTubeRenderer;
import mcjty.ariente.blocks.storagecube.client.StorageCubeRenderer;
import mcjty.ariente.blocks.utility.ElevatorRenderer;
import mcjty.ariente.blocks.utility.SmelterRenderer;
import mcjty.ariente.blocks.utility.StorageRenderer;
import mcjty.ariente.blocks.utility.WarperRenderer;
import mcjty.ariente.blocks.utility.autofield.AutoFieldRenderer;
import mcjty.ariente.blocks.utility.door.DoorMarkerRenderer;
import mcjty.ariente.blocks.utility.door.InvisibleDoorRenderer;
import mcjty.ariente.cables.CableModelLoader;
import mcjty.ariente.cables.CableRenderer;
import mcjty.ariente.client.ArienteSpriteUploader;
import mcjty.ariente.display.client.CameraDisplayRenderer;
import mcjty.ariente.display.client.DisplayClientEvents;
import mcjty.ariente.display.client.DisplayModuleRenderers;
import mcjty.ariente.display.client.DisplayRenderer;
import mcjty.ariente.entities.LaserRender;
import mcjty.ariente.entities.RenderArientePearl;
import mcjty.ariente.entities.fluxelevator.FluxElevatorRender;
import mcjty.ariente.entities.fluxship.FluxShipPanelRender;
import mcjty.ariente.entities.fluxship.FluxShipRender;
import mcjty.ariente.entities.levitator.FluxLevitatorRender;
import mcjty.ariente.items.BlueprintRenderer;
import mcjty.ariente.multipart.MultipartRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;

public class ClientSetup {

    public static void particleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(Registration.PURIFIER_RING_X.get(), sprites -> new PurifierRingParticle.Provider(sprites, Direction.Axis.X));
        event.registerSpriteSet(Registration.PURIFIER_RING_Z.get(), sprites -> new PurifierRingParticle.Provider(sprites, Direction.Axis.Z));
    }

    public static void init(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new ClientForgeEventHandlers());
        NeoForge.EVENT_BUS.register(new DisplayClientEvents());
        DisplayModuleRenderers.init();
        NeoForge.EVENT_BUS.register(new KeyInputHandler());

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
        event.register(ModelResourceLocation.standalone(FluxShipRender.MODEL));
        event.register(ModelResourceLocation.standalone(BlueprintRenderer.BASE_MODEL));
    }

    public static void modelsBaked(ModelEvent.BakingCompleted event) {
        CameraDisplayRenderer.clearCache();
    }

    public static void initModels(ModelEvent.RegisterGeometryLoaders event) {
        event.register(ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "cableloader"), new CableModelLoader());
    }

    @SubscribeEvent
    public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(Registration.DISPLAY_BASE_TILE.get(), DisplayRenderer::new);
        event.registerBlockEntityRenderer(Registration.PURIFIER_TUBE_TILE.get(), PurifierTubeRenderer::new);
        event.registerBlockEntityRenderer(Registration.HOLO_STORAGE_TILE.get(), StorageCubeRenderer::new);
        event.registerBlockEntityRenderer(Registration.TYPE_MULTIPART.get(), MultipartRenderer::new);
        event.registerEntityRenderer(Registration.ENTITY_LASER.get(), new LaserRender.Factory());
        event.registerEntityRenderer(Registration.ENTITY_FLUX_LEVITATOR.get(), new FluxLevitatorRender.Factory());
        event.registerEntityRenderer(Registration.ENTITY_ELEVATOR.get(), new FluxElevatorRender.Factory());
        event.registerEntityRenderer(Registration.ENTITY_FLUX_SHIP.get(), new FluxShipRender.Factory());
        event.registerEntityRenderer(Registration.ENTITY_SHIP_PANEL.get(), FluxShipPanelRender::new);
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
