package mcjty.ariente.setup;

import mcjty.ariente.bindings.Binding;
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
import mcjty.ariente.client.ArienteRenderType;
import mcjty.ariente.client.ArienteSpriteUploader;
import mcjty.ariente.client.FabricClientEvents;
import mcjty.ariente.client.LanceModel;
import mcjty.ariente.client.ObjModelLoader;
import mcjty.ariente.client.StandaloneModels;
import mcjty.ariente.client.textures.SurfaceTextures;
import mcjty.ariente.display.client.CameraDisplayRenderer;
import mcjty.ariente.display.client.DisplayModuleRenderers;
import mcjty.ariente.display.client.DisplayRenderer;
import mcjty.ariente.entities.LaserRender;
import mcjty.ariente.entities.RenderArientePearl;
import mcjty.ariente.entities.fluxelevator.FluxElevatorRender;
import mcjty.ariente.entities.fluxship.FluxShipPanelRender;
import mcjty.ariente.entities.fluxship.FluxShipRender;
import mcjty.ariente.entities.fluxship.FluxShipWeaponRender;
import mcjty.ariente.entities.levitator.FluxLevitatorRender;
import mcjty.ariente.facade.FacadeRenderer;
import mcjty.ariente.items.BlueprintRenderer;
import mcjty.ariente.items.armor.PowerSuitModel;
import mcjty.ariente.multipart.MultipartRenderer;
import mcjty.ariente.network.ClientMessages;
import mcjty.lib.client.ClientStartup;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;

public class ClientSetup implements ClientModInitializer {

    public static void particleProviders() {
        ParticleFactoryRegistry.getInstance().register(Registration.PURIFIER_RING_X.get(), sprites -> new PurifierRingParticle.Provider(sprites, Direction.Axis.X));
        ParticleFactoryRegistry.getInstance().register(Registration.PURIFIER_RING_Z.get(), sprites -> new PurifierRingParticle.Provider(sprites, Direction.Axis.Z));
    }

    public static void init() {

        DisplayModuleRenderers.init();

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

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.translucent(),
                Registration.GLASS_FENCE.get(), Registration.BLUE_GLASS_FENCE.get(),
                Registration.AICORE.get(), Registration.HOLO_STORAGE_1.get(),
                Registration.HOLO_STORAGE_2.get(), Registration.HOLO_STORAGE_3.get(),
                Registration.PURIFIER_TUBE.get(), Registration.NEGARITE_TANK.get(),
                Registration.POSIRITE_TANK.get(), Registration.CAMERA.get());
        BlockRenderLayerMap.INSTANCE.putBlock(Registration.TECH_FENCE.get(), RenderType.cutout());
    }

    public static void additionalModels() {
        StandaloneModels.register(FluxShipRender.MODEL);
    }

    public static void modelsBaked() {
        CameraDisplayRenderer.clearCache();
    }

    public static void entityRenderers() {
        BlockEntityRendererRegistry.register(Registration.DISPLAY_BASE_TILE.get(), DisplayRenderer::new);
        BlockEntityRendererRegistry.register(Registration.PURIFIER_TUBE_TILE.get(), PurifierTubeRenderer::new);
        BlockEntityRendererRegistry.register(Registration.HOLO_STORAGE_TILE.get(), StorageCubeRenderer::new);
        BlockEntityRendererRegistry.register(Registration.TYPE_MULTIPART.get(), MultipartRenderer::new);
        EntityRendererRegistry.register(Registration.ENTITY_LASER.get(), new LaserRender.Factory());
        EntityRendererRegistry.register(Registration.ENTITY_FLUX_LEVITATOR.get(), new FluxLevitatorRender.Factory());
        EntityRendererRegistry.register(Registration.ENTITY_ELEVATOR.get(), new FluxElevatorRender.Factory());
        EntityRendererRegistry.register(Registration.ENTITY_FLUX_SHIP.get(), new FluxShipRender.Factory());
        EntityRendererRegistry.register(Registration.ENTITY_SHIP_PANEL.get(), FluxShipPanelRender::new);
        EntityRendererRegistry.register(Registration.ENTITY_PEARL.get(), RenderArientePearl.FACTORY);
    }

    public static void setupSpriteUploader() {
        ClientStartup.register(client -> {
            ArienteSpriteUploader.INSTANCE = new ArienteSpriteUploader(client.getTextureManager());
            ((ReloadableResourceManager) client.getResourceManager()).registerReloadListener(ArienteSpriteUploader.INSTANCE);
        });
    }

    public static void specialModels() {
        var mc = Minecraft.getInstance();
        var blueprint = new BlueprintRenderer(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
        var facade = new FacadeRenderer(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
        BuiltinItemRendererRegistry.INSTANCE.register(Registration.BLUEPRINT.get(), blueprint::renderByItem);
        BuiltinItemRendererRegistry.INSTANCE.register(Registration.FACADE_ITEM.get(), facade::renderByItem);
        StandaloneModels.register(BlueprintRenderer.BASE_MODEL);
    }

    private static void registerArmor() {
        ArmorRenderer.register((poses, buffers, stack, entity, slot, light, context) -> {
            var model = PowerSuitModel.forSlot(slot);

            context.copyPropertiesTo(model);
            var texture = ResourceLocation.parse("ariente:textures/armor/suit.png");
            ArmorRenderer.renderPart(poses, buffers, light, stack, model, texture);
        }, Registration.POWERSUIT_HEAD.get(), Registration.POWERSUIT_CHEST.get(), Registration.POWERSUIT_LEGS.get(), Registration.POWERSUIT_FEET.get());
    }

    @Override
    public void onInitializeClient() {
        init();
        entityRenderers();
        particleProviders();
        setupSpriteUploader();
        specialModels();
        registerArmor();
        additionalModels();
        FluxShipWeaponRender.registerModel();
        LanceModel.register();
        ArienteRenderType.registerShaders();

        CableModelLoader.register();
        ObjModelLoader.register();
        SurfaceTextures.register();
        Binding.register();
        ClientMessages.register();
        FabricClientEvents.register();
    }
}
