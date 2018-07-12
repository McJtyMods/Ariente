package mcjty.ariente;

import mcjty.ariente.biomes.ModBiomes;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.cities.BuildingPart;
import mcjty.ariente.cities.City;
import mcjty.ariente.cities.CityTools;
import mcjty.ariente.dimension.ArienteChunkGenerator;
import mcjty.ariente.items.ModItems;
import mcjty.lib.McJtyRegister;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;

public class ForgeEventHandlers {

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        McJtyRegister.registerBlocks(Ariente.instance, event.getRegistry());
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        McJtyRegister.registerItems(Ariente.instance, event.getRegistry());
        ModBlocks.initOreDict();
        ModItems.initOreDict();
    }

    @SubscribeEvent
    public void registerBiomes(RegistryEvent.Register<Biome> event) {
        IForgeRegistry<Biome> registry = event.getRegistry();
        ModBiomes.registerBiomes(registry);
    }

    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> sounds) {
        ModSounds.init(sounds.getRegistry());
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.PlaceEvent event) {
        World world = event.getWorld();
        if (!world.isRemote) {
            if (world.provider.getDimension() == 222) {  // @todo config
                ArienteChunkGenerator generator = (ArienteChunkGenerator)(((WorldServer) world).getChunkProvider().chunkGenerator);
                BlockPos pos = event.getPos();
                int cx = pos.getX() >> 4;
                int cz = pos.getZ() >> 4;
                City city = CityTools.getNearestCity(generator, cx, cz);
                if (city != null) {


                    List<BuildingPart> parts = CityTools.getBuildingParts(city, cx, cz);
                    if (!parts.isEmpty()) {
                        NBTTagCompound c;
                        int y = CityTools.getLowestHeight(city, cx, cz);
                        if (pos.getY() >= y) {
                            BuildingPart currentPart = parts.get(0);
                            int currentY = y;
                            for (int i = 1 ; i < parts.size() ; i++) {

                            }
                        }
                    }
                }
            }
        }
    }
}
