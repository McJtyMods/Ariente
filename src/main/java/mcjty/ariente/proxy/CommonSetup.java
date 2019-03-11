package mcjty.ariente.proxy;

import mcjty.ariente.Ariente;
import mcjty.ariente.ForgeEventHandlers;
import mcjty.ariente.TerrainEventHandlers;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.cities.AssetRegistries;
import mcjty.ariente.config.ConfigSetup;
import mcjty.ariente.dimension.DimensionRegister;
import mcjty.ariente.entities.ModEntities;
import mcjty.ariente.gui.HoloGuiCompatibility;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.oregen.WorldGen;
import mcjty.ariente.oregen.WorldTickHandler;
import mcjty.ariente.recipes.RecipeRegistry;
import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.setup.DefaultCommonSetup;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Created by jorrit on 16.12.16.
 */
public class CommonSetup extends DefaultCommonSetup {

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        MinecraftForge.EVENT_BUS.register(WorldTickHandler.instance);
        MinecraftForge.TERRAIN_GEN_BUS.register(new TerrainEventHandlers());
        NetworkRegistry.INSTANCE.registerGuiHandler(Ariente.instance, new GuiProxy());

        ArienteMessages.registerMessages("ariente");

        ConfigSetup.init();
        DimensionRegister.init();
        ModBlocks.init();
        ModItems.init();
        WorldGen.init();
        ModEntities.init();

        RecipeRegistry.init();
    }

    @Override
    protected void setupModCompat() {
        MainCompatHandler.registerWaila();
        MainCompatHandler.registerTOP();
        HoloGuiCompatibility.register();
    }

    @Override
    public void createTabs() {
        createTab("ariente", new ItemStack(Items.WATER_BUCKET));
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        ConfigSetup.postInit();

        AssetRegistries.reset();
        for (String path : ConfigSetup.ASSETS) {
            if (path.startsWith("/")) {
                try(InputStream inputstream = Ariente.class.getResourceAsStream(path)) {
                    AssetRegistries.load(inputstream, path);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            } else if (path.startsWith("$")) {
                File file = new File(getModConfigDir().getPath() + File.separator + path.substring(1));
                AssetRegistries.load(file);
            } else {
                throw new RuntimeException("Invalid path for ariente resource in 'assets' config!");
            }
        }
    }
}
