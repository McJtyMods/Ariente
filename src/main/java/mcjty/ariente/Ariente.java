package mcjty.ariente;

import mcjty.ariente.api.IArienteMod;
import mcjty.ariente.api.IArienteSystem;
import mcjty.ariente.apiimpl.ArienteSystem;
import mcjty.ariente.blocks.decorative.DecorativeBlockModule;
import mcjty.ariente.config.Config;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.setup.ClientSetup;
import mcjty.ariente.setup.ModSetup;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IHoloGuiHandler;
import mcjty.lib.modules.Modules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Ariente.MODID)
public class Ariente implements IArienteMod {
    public static final String MODID = "ariente";

    private final Modules modules = new Modules();
    public static ModSetup setup = new ModSetup();

    public static Ariente instance;

    public static IHoloGuiHandler guiHandler;
    public static IArienteSystem arienteSystem = new ArienteSystem();

    public Ariente() {
        instance = this;
        setupModules();

        // This has to be done VERY early
        // @todo 1.14
//        FluidRegistry.enableUniversalBucket();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);

        Registration.register();
        BlueprintRecipeRegistry.register();
        // The following is needed to make sure our SpriteUploader is setup at exactly the right moment
        FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.NORMAL, false, ColorHandlerEvent.Block.class, event -> ClientSetup.setupSpriteUploader());

        FMLJavaModLoadingContext.get().getModEventBus().addListener(setup::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(setup::entityAttributeRegistry);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::initModels);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::onTextureStitch);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::entityRenderers);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::layerDefinitions);
        });
    }

    private void setupModules() {
        modules.register(new DecorativeBlockModule());
    }

    @Override
    public IArienteSystem getSystem() {
        return arienteSystem;
    }
}
