package mcjty.ariente;


import mcjty.ariente.api.IArienteMod;
import mcjty.ariente.api.IArienteSystem;
import mcjty.ariente.apiimpl.ArienteSystem;
import mcjty.ariente.config.Config;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.setup.ClientRegistration;
import mcjty.ariente.setup.ModSetup;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IHoloGuiHandler;
import mcjty.lib.base.ModBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Ariente.MODID)
public class Ariente implements ModBase, IArienteMod {
    public static final String MODID = "ariente";

    public static ModSetup setup = new ModSetup();

    public static Ariente instance;

    public static IHoloGuiHandler guiHandler;
    public static IArienteSystem arienteSystem = new ArienteSystem();

    public Ariente() {
        instance = this;

        // This has to be done VERY early
        // @todo 1.14
//        FluidRegistry.enableUniversalBucket();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);

        Registration.register();
        BlueprintRecipeRegistry.register();
        // The following is needed to make sure our SpriteUploader is setup at exactly the right moment
        FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.NORMAL, false, ColorHandlerEvent.Block.class, event -> ClientRegistration.setupSpriteUploader());

        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLCommonSetupEvent event) -> setup.init(event));
    }

    @Override
    public IArienteSystem getSystem() {
        return arienteSystem;
    }

    @Override
    public String getModId() {
        return Ariente.MODID;
    }

    @Override
    public void openManual(PlayerEntity player, int bookindex, String page) {
        // @todo
    }
}
