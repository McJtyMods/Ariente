package mcjty.ariente.setup;

import mcjty.ariente.bindings.Binding;
import mcjty.ariente.client.ArienteRenderType;
import mcjty.ariente.client.FabricClientEvents;
import mcjty.ariente.client.LanceModel;
import mcjty.ariente.client.ObjModelLoader;
import mcjty.ariente.client.textures.SurfaceTextures;
import mcjty.ariente.display.client.DisplayRenderType;
import mcjty.ariente.entities.fluxship.FluxShipWeaponRender;
import mcjty.ariente.network.ClientMessages;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.AtlasRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.client.renderer.texture.OverlayTexture;

import net.minecraft.client.resources.model.sprite.AtlasManager;
import mcjty.ariente.items.BlueprintRenderer;
import mcjty.ariente.facade.FacadeRenderer;
import mcjty.ariente.items.armor.PowerSuitModel;
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
import mcjty.ariente.multipart.MultipartRenderer;
import mcjty.ariente.client.StandaloneModels;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

public class ClientSetup implements ClientModInitializer {

    public static void particleProviders() {
        ParticleProviderRegistry.getInstance().register(Registration.PURIFIER_RING_X.get(), sprites -> new PurifierRingParticle.Provider(sprites, Direction.Axis.X));
        ParticleProviderRegistry.getInstance().register(Registration.PURIFIER_RING_Z.get(), sprites -> new PurifierRingParticle.Provider(sprites, Direction.Axis.Z));
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
        AtlasRegistry.register(new AtlasManager.AtlasConfig(ArienteSpriteUploader.ARIENTE_ATLAS,
                ArienteSpriteUploader.ATLAS_ID, false));
    }

    public static void specialModels() {
        SpecialModelRenderers.ID_MAPPER.put(Identifier.fromNamespaceAndPath("ariente", "blueprint"), BlueprintRenderer.Unbaked.CODEC);
        SpecialModelRenderers.ID_MAPPER.put(Identifier.fromNamespaceAndPath("ariente", "facade"), FacadeRenderer.Unbaked.CODEC);
    }

    private static void registerArmor() {
        ArmorRenderer.register((poses, collector, stack, state, slot, light, context) -> {
            var model = PowerSuitModel.forSlot(slot);

            model.setupAnim(state);
            var texture = Identifier.parse("ariente:textures/entity/equipment/humanoid/powersuit.png");
            collector.submitModel(model, state, poses, RenderTypes.armorCutoutNoCull(texture),
                    light, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
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
        DisplayRenderType.registerPipelines();
        CableModelLoader.register();
        ObjModelLoader.register();
        SurfaceTextures.register();
        Binding.register();
        ClientMessages.register();
        FabricClientEvents.register();
    }
}
