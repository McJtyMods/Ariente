package mcjty.ariente.proxy;

import mcjty.ariente.Ariente;
import mcjty.ariente.ForgeEventHandlers;
import mcjty.ariente.TerrainEventHandlers;
import mcjty.ariente.TerrainGenEventHandlers;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.cities.AssetRegistries;
import mcjty.ariente.config.ArienteConfiguration;
import mcjty.ariente.dimension.DimensionRegister;
import mcjty.ariente.entities.ModEntities;
import mcjty.ariente.gui.GuiProxy;
import mcjty.ariente.gui.ModGuis;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.oregen.WorldGen;
import mcjty.ariente.oregen.WorldTickHandler;
import mcjty.lib.base.GeneralConfig;
import mcjty.lib.network.PacketHandler;
import mcjty.lib.proxy.AbstractCommonProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Created by jorrit on 16.12.16.
 */
public abstract class CommonProxy extends AbstractCommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        MinecraftForge.TERRAIN_GEN_BUS.register(new TerrainGenEventHandlers());

        SimpleNetworkWrapper network = PacketHandler.registerMessages(Ariente.MODID, "ariente");
        GeneralConfig.preInit(e);
        ArienteMessages.registerNetworkMessages(network);

        DimensionRegister.init();

        mainConfig = new Configuration(new File(modConfigDir.getPath(), "ariente.cfg"));
        ArienteConfiguration.init(mainConfig);

//        FluidSetup.preInitFluids();
        ModBlocks.init();
        ModItems.init();
        WorldGen.init();
        ModEntities.init();
        ModGuis.init();
    }

    public Configuration getConfig() {
        return mainConfig;
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);

//        EntityRegistry.registerModEntity(new ResourceLocation(AquaMunda.MODID, "fresh_water_falling"), EntityFallingFreshWaterBlock.class, "fresh_water_falling", 1, AquaMunda.instance, 250, 5, true);
        MinecraftForge.EVENT_BUS.register(WorldTickHandler.instance);
        MinecraftForge.TERRAIN_GEN_BUS.register(new TerrainEventHandlers());
        NetworkRegistry.INSTANCE.registerGuiHandler(Ariente.instance, new GuiProxy());

//        ConfigSetup.readRecipesConfig();
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
//        ConfigSetup.postInit();
//        ModBlocks.postInit();
        if (mainConfig.hasChanged()) {
            mainConfig.save();
        }
//        mainConfig = null;

        AssetRegistries.reset();
        for (String path : ArienteConfiguration.ASSETS) {
            if (path.startsWith("/")) {
                try(InputStream inputstream = Ariente.class.getResourceAsStream(path)) {
                    AssetRegistries.load(inputstream, path);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            } else if (path.startsWith("$")) {
                File file = new File(modConfigDir.getPath() + File.separator + path.substring(1));
                AssetRegistries.load(file);
            } else {
                throw new RuntimeException("Invalid path for ariente resource in 'assets' config!");
            }
        }
    }

    public abstract boolean isJumpKeyDown();
}
