package mcjty.ariente;


import mcjty.ariente.api.IArienteMod;
import mcjty.ariente.api.IArienteSystem;
import mcjty.ariente.apiimpl.ArienteSystem;
import mcjty.ariente.setup.ModSetup;
import mcjty.hologui.api.IHoloGuiHandler;
import mcjty.lib.base.ModBase;
import mcjty.lib.proxy.IProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;


@Mod(modid = Ariente.MODID, name = Ariente.MODNAME,
        dependencies =
                "required-after:mcjtylib_ng@[" + Ariente.MIN_MCJTYLIB_VER + ",);" +
                "required-after:hologui@[" + Ariente.MIN_HOLOGUI_VER + ",);" +
                "after:forge@[" + Ariente.MIN_FORGE11_VER + ",)",
        acceptedMinecraftVersions = "[1.12,1.13)",
        version = Ariente.VERSION)
public class Ariente implements ModBase, IArienteMod {
    public static final String MODID = "ariente";
    public static final String MODNAME = "Ariente";
    public static final String VERSION = "0.0.21-alpha";
    public static final String MIN_FORGE11_VER = "14.23.3.2694";
    public static final String MIN_MCJTYLIB_VER = "3.5.2";
    public static final String MIN_HOLOGUI_VER = "0.0.8-beta";

    @SidedProxy(clientSide = "mcjty.ariente.setup.ClientProxy", serverSide = "mcjty.ariente.setup.ServerProxy")
    public static IProxy proxy;
    public static ModSetup setup = new ModSetup();

    @Mod.Instance
    public static Ariente instance;

    public static IHoloGuiHandler guiHandler;
    public static IArienteSystem arienteSystem = new ArienteSystem();

    public Ariente() {
        // This has to be done VERY early
        FluidRegistry.enableUniversalBucket();
    }


    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event){
        setup.preInit(event);
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        setup.init(e);
        proxy.init(e);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        setup.postInit(e);
        proxy.postInit(e);
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
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
    public void openManual(EntityPlayer player, int bookindex, String page) {
        // @todo
    }
}
