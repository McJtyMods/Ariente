package mcjty.ariente.biomes;

import mcjty.ariente.Ariente;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class ModBiomes {

    @GameRegistry.ObjectHolder(Ariente.MODID + ":ariente_plains")
    public static BiomeArientePlains arientePlains;

    @GameRegistry.ObjectHolder(Ariente.MODID + ":ariente_hills")
    public static BiomeArienteHills arienteHills;


    public static void registerBiomes(IForgeRegistry<Biome> registry) {
        setupBiome(registry, "ariente_plains", new BiomeArientePlains(
                new Biome.BiomeProperties("Ariente Plains")
                        .setBaseHeight(0.125F)
                        .setHeightVariation(0.05F)
                        .setTemperature(0.8F)
                        .setRainfall(0.4F)),
                BiomeDictionary.Type.DEAD);
        setupBiome(registry, "ariente_hills", new BiomeArientePlains(
                new Biome.BiomeProperties("Ariente Hills")
                        .setBaseHeight(0.45F)
                        .setHeightVariation(0.3F)
                        .setTemperature(0.8F)
                        .setRainfall(0.4F)),
                BiomeDictionary.Type.DEAD, BiomeDictionary.Type.HILLS);
    }

    private static void setupBiome(IForgeRegistry<Biome> registry, String name, Biome biome, BiomeDictionary.Type... types) {
        biome.setRegistryName(Ariente.MODID, name);
        registry.register(biome);
        BiomeDictionary.addTypes(biome, types);
    }
}
