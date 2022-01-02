package mcjty.ariente.setup;

import mcjty.ariente.Ariente;
import mcjty.ariente.ClientForgeEventHandlers;
import mcjty.ariente.bindings.KeyInputHandler;
import mcjty.ariente.blocks.generators.NegariteTankRenderer;
import mcjty.ariente.blocks.generators.PosiriteTankRenderer;
import mcjty.ariente.blocks.utility.ElevatorRenderer;
import mcjty.ariente.blocks.utility.StorageRenderer;
import mcjty.ariente.blocks.utility.WarperRenderer;
import mcjty.ariente.blocks.utility.autofield.AutoFieldRenderer;
import mcjty.ariente.blocks.utility.door.DoorMarkerRenderer;
import mcjty.ariente.blocks.utility.door.InvisibleDoorRenderer;
import mcjty.ariente.cables.CableModelLoader;
import mcjty.ariente.cables.CableRenderer;
import mcjty.ariente.client.ArienteSpriteUploader;
import mcjty.ariente.entities.LaserRender;
import mcjty.ariente.entities.RenderArientePearl;
import mcjty.ariente.entities.drone.DroneModel;
import mcjty.ariente.entities.drone.DroneRender;
import mcjty.ariente.entities.drone.SentinelDroneRender;
import mcjty.ariente.entities.fluxelevator.FluxElevatorRender;
import mcjty.ariente.entities.fluxship.FluxShipRender;
import mcjty.ariente.entities.levitator.FluxLevitatorRender;
import mcjty.ariente.entities.soldier.SoldierRender;
import mcjty.ariente.items.armor.PowerSuitModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
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
        AutoFieldRenderer.register();
        WarperRenderer.register();
        CableRenderer.register(Registration.NETCABLE_TILE.get());
        CableRenderer.register(Registration.CONNECTOR_TILE.get());

        ItemBlockRenderTypes.setRenderLayer(Registration.NETCABLE.get(), (RenderType) -> true);
        ItemBlockRenderTypes.setRenderLayer(Registration.GLASS_FENCE.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(Registration.BLUE_GLASS_FENCE.get(), RenderType.translucent());
    }

    public static void initModels(ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(new ResourceLocation(Ariente.MODID, "cableloader"), new CableModelLoader());
    }

    public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(Registration.ENTITY_LASER.get(), new LaserRender.Factory());
        event.registerEntityRenderer(Registration.ENTITY_DRONE.get(), DroneRender::new);
        event.registerEntityRenderer(Registration.ENTITY_SENTINEL_DRONE.get(), SentinelDroneRender::new);
        event.registerEntityRenderer(Registration.ENTITY_SOLDIER.get(), SoldierRender.FACTORY);
        event.registerEntityRenderer(Registration.ENTITY_MASTER_SOLDIER.get(), SoldierRender.MASTER_FACTORY);
        event.registerEntityRenderer(Registration.ENTITY_FLUX_LEVITATOR.get(), new FluxLevitatorRender.Factory());
        event.registerEntityRenderer(Registration.ENTITY_ELEVATOR.get(), new FluxElevatorRender.Factory());
        event.registerEntityRenderer(Registration.ENTITY_FLUX_SHIP.get(), new FluxShipRender.Factory());
        event.registerEntityRenderer(Registration.ENTITY_PEARL.get(), RenderArientePearl.FACTORY);
    }

    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        DroneModel.layerDefinitions(event);
        PowerSuitModel.layerDefinitions(event);
    }

    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if (!event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
            return;
        }
        event.addSprite(NEGARITE_BEAM);
        event.addSprite(POSIRITE_BEAM);
        event.addSprite(ELEVATOR_BEAM);
        for (int i = 0; i < AutoFieldRenderer.BEAMS.length; i++) {
            event.addSprite(AutoFieldRenderer.BEAMS[i]);
        }
        event.addSprite(WarperRenderer.HALO);
    }

    public static void setupSpriteUploader() {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        if (resourceManager instanceof ReloadableResourceManager) {
            ArienteSpriteUploader.INSTANCE = new ArienteSpriteUploader(Minecraft.getInstance().getTextureManager());
            ((ReloadableResourceManager) resourceManager).registerReloadListener(ArienteSpriteUploader.INSTANCE);
        }
    }
}
