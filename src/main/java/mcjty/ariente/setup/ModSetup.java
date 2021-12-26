package mcjty.ariente.setup;

import mcjty.ariente.ForgeEventHandlers;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.entities.drone.DroneEntity;
import mcjty.ariente.entities.drone.SentinelDroneEntity;
import mcjty.ariente.entities.soldier.MasterSoldierEntity;
import mcjty.ariente.entities.soldier.SoldierEntity;
import mcjty.ariente.gui.HoloGuiCompatibility;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.oregen.WorldGen;
import mcjty.ariente.potions.ModPotions;
import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.setup.DefaultModSetup;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModSetup extends DefaultModSetup {

    public boolean arienteWorld = false;

    public ModSetup() {
        createTab("ariente", () -> new ItemStack(Registration.POSIRITE_GENERATOR.get()));
    }

    @Override
    public void init(FMLCommonSetupEvent e) {
        super.init(e);

//        MinecraftForge.EVENT_BUS.register(WorldTickHandler.instance); // @todo 1.14
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
//        NetworkRegistry.INSTANCE.registerGuiHandler(Ariente.instance, new GuiProxy());

        ArienteMessages.registerMessages("ariente");

        WorldGen.init();
        ModPotions.init();
    }

    @Override
    protected void setupModCompat() {
        arienteWorld = ModList.get().isLoaded("arienteworld");
        if (arienteWorld) {
            ArienteWorldCompat.register();
        }

        MainCompatHandler.registerWaila();
        MainCompatHandler.registerTOP();
        HoloGuiCompatibility.register();
    }

    public void entityAttributeRegistry(EntityAttributeCreationEvent event)
    {
        event.put(Registration.ENTITY_DRONE.get(), DroneEntity.registerAttributes().build());
        event.put(Registration.ENTITY_SENTINEL_DRONE.get(), SentinelDroneEntity.registerAttributes().build());
        event.put(Registration.ENTITY_SOLDIER.get(), SoldierEntity.registerAttributes().build());
        event.put(Registration.ENTITY_MASTER_SOLDIER.get(), MasterSoldierEntity.registerAttributes().build());
    }
}
