package mcjty.ariente;

import mcjty.ariente.client.LanceModel;
import mcjty.ariente.entities.fluxship.FluxShipPilotRender;

import mcjty.ariente.display.client.DisplayRenderType;
import mcjty.ariente.api.IArienteMod;
import mcjty.ariente.api.IArienteSystem;
import mcjty.ariente.apiimpl.ArienteSystem;
import mcjty.ariente.bindings.Binding;
import mcjty.ariente.blocks.decorative.DecorativeBlockModule;
import mcjty.ariente.client.ArienteRenderType;
import mcjty.ariente.config.Config;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.setup.ClientSetup;
import mcjty.ariente.setup.ModSetup;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IHoloGuiHandler;
import mcjty.lib.modules.Modules;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(Ariente.MODID)
public class Ariente implements IArienteMod {
    public static final String MODID = "ariente";

    private final Modules modules = new Modules();
    public static ModSetup setup = new ModSetup();

    public static Ariente instance;

    public static IHoloGuiHandler guiHandler;
    public static IArienteSystem arienteSystem = new ArienteSystem();

    public Ariente(ModContainer mod, IEventBus bus, Dist dist) {
        instance = this;
        setupModules();

        mod.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
        mod.registerConfig(ModConfig.Type.SYNCED, Config.SERVER_CONFIG, "ariente-server.toml");

        Registration.register(bus);
        BlueprintRecipeRegistry.register(bus);
        // The following is needed to make sure our SpriteUploader is setup at exactly the right moment

        bus.addListener(setup::init);
        bus.addListener(ArienteMessages::registerMessages);
        if (dist.isClient()) {
            bus.addListener(ClientSetup::init);
            bus.addListener(LanceModel::register);
            bus.addListener(FluxShipPilotRender::registerModifiers);
            bus.addListener(ClientSetup::initModels);
            bus.addListener(ClientSetup::additionalModels);
            bus.addListener(ClientSetup::modelsBaked);
            bus.addListener(ArienteRenderType::registerShaders);
            bus.addListener(DisplayRenderType::registerPipelines);
            bus.addListener(ClientSetup::entityRenderers);
            bus.addListener(ClientSetup::particleProviders);
            bus.addListener(Binding::register);
            bus.addListener(ClientSetup::setupSpriteUploader);
            bus.addListener(ClientSetup::specialModels);
            bus.addListener(ClientSetup::itemExtensions);
        }
    }

    private void setupModules() {
        modules.register(new DecorativeBlockModule());
    }

    @Override
    public IArienteSystem getSystem() {
        return arienteSystem;
    }
}
