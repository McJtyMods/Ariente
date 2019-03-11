package mcjty.ariente.setup;


import mcjty.ariente.Ariente;
import mcjty.ariente.ModCrafting;
import mcjty.ariente.biomes.ModBiomes;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.sounds.ModSounds;
import mcjty.lib.McJtyRegister;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber
public class Registration {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        McJtyRegister.registerBlocks(Ariente.instance, event.getRegistry());
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        McJtyRegister.registerItems(Ariente.instance, event.getRegistry());
        ModBlocks.initOreDict();
        ModItems.initOreDict();
        ModCrafting.init();
    }

    @SubscribeEvent
    public static void registerBiomes(RegistryEvent.Register<Biome> event) {
        IForgeRegistry<Biome> registry = event.getRegistry();
        ModBiomes.registerBiomes(registry);
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> sounds) {
        ModSounds.init(sounds.getRegistry());
    }


}
