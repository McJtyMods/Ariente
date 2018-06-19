package mcjty.ariente.proxy;

import mcjty.ariente.Ariente;
import mcjty.ariente.ForgeEventHandlers;
import mcjty.ariente.TerrainGenEventHandlers;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.dimension.DimensionRegister;
import mcjty.lib.base.GeneralConfig;
import mcjty.lib.network.PacketHandler;
import mcjty.lib.proxy.AbstractCommonProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

/**
 * Created by jorrit on 16.12.16.
 */
public class CommonProxy extends AbstractCommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        MinecraftForge.TERRAIN_GEN_BUS.register(new TerrainGenEventHandlers());

        SimpleNetworkWrapper network = PacketHandler.registerMessages(Ariente.MODID, "ariente");
        GeneralConfig.preInit(e);
//        AMPacketHandler.registerMessages(network);

        DimensionRegister.init();

//        ConfigSetup.preInit(e);

//        FluidSetup.preInitFluids();
        ModBlocks.init();
//        ModItems.init();
//            WorldGen.init();
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);

//        EntityRegistry.registerModEntity(new ResourceLocation(AquaMunda.MODID, "fresh_water_falling"), EntityFallingFreshWaterBlock.class, "fresh_water_falling", 1, AquaMunda.instance, 250, 5, true);

//        ConfigSetup.readRecipesConfig();
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
//        ConfigSetup.postInit();
//        ModBlocks.postInit();
    }
}
