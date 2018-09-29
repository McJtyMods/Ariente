package mcjty.ariente;


import mcjty.ariente.api.hologui.IHoloGuiHandler;
import mcjty.ariente.apiimp.hologui.HoloGuiHandler;
import mcjty.ariente.commands.*;
import mcjty.ariente.proxy.CommonProxy;
import mcjty.lib.base.ModBase;
import mcjty.lib.compat.MainCompatHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;


@Mod(modid = Ariente.MODID, name = Ariente.MODNAME,
        dependencies =
                "required-after:mcjtylib_ng@[" + Ariente.MIN_MCJTYLIB_VER + ",);" +
                "after:forge@[" + Ariente.MIN_FORGE11_VER + ",)",
        acceptedMinecraftVersions = "[1.12,1.13)",
        version = Ariente.VERSION,
        guiFactory = "mcjty.ariente.config.ArienteModGuiFactory")
public class Ariente implements ModBase {
    public static final String MODID = "ariente";
    public static final String MODNAME = "Ariente";
    public static final String VERSION = "0.0.2-alpha";
    public static final String MIN_FORGE11_VER = "14.23.3.2694";
    public static final String MIN_MCJTYLIB_VER = "3.0.4";

    @SidedProxy(clientSide = "mcjty.ariente.proxy.ClientProxy", serverSide = "mcjty.ariente.proxy.ServerProxy")
    public static CommonProxy proxy;

    @Mod.Instance
    public static Ariente instance;

    public static IHoloGuiHandler guiHandler = new HoloGuiHandler();

    public static CreativeTabs creativeTab;

    public static Logger logger;

    public Ariente() {
        // This has to be done VERY early
        FluidRegistry.enableUniversalBucket();
    }


    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event){
        logger = event.getModLog();
        creativeTab = new CreativeTabs("ariente") {
            @Override
            public ItemStack getTabIconItem() {
                return new ItemStack(Items.WATER_BUCKET);   // @todo
            }
        };
        proxy.preInit(event);
        MainCompatHandler.registerWaila();
        MainCompatHandler.registerTOP();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        proxy.init(e);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        proxy.postInit(e);
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandSaveCity());
        event.registerServerCommand(new CommandEditMode());
        event.registerServerCommand(new CommandFindCity());
        event.registerServerCommand(new CommandInfo());
        event.registerServerCommand(new CommandSwitchPart());
        event.registerServerCommand(new CommandPacify());
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
