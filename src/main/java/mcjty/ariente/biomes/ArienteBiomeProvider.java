package mcjty.ariente.biomes;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;

public class ArienteBiomeProvider extends BiomeProvider {

    public ArienteBiomeProvider(World world) {
        super(world.getWorldInfo());
        getBiomesToSpawnIn().clear();
        getBiomesToSpawnIn().add(ModBiomes.arientePlains);
        getBiomesToSpawnIn().add(ModBiomes.arienteHills);
        getBiomesToSpawnIn().add(ModBiomes.arienteOcean);
        getBiomesToSpawnIn().add(ModBiomes.arienteForest);
        makeLayers(world.getSeed());
    }

    private void makeLayers(long seed) {
        GenLayer biomes = new GenLayerArienteBiomes(1L);

        biomes = new GenLayerZoom(1000, biomes);
        biomes = new GenLayerZoom(1001, biomes);

        biomes = new GenLayerZoom(1002, biomes);
        biomes = new GenLayerZoom(1003, biomes);
        biomes = new GenLayerZoom(1004, biomes);
        biomes = new GenLayerZoom(1005, biomes);
        biomes = new GenLayerZoom(1006, biomes);

        GenLayer genlayervoronoizoom = new GenLayerVoronoiZoom(10L, biomes);

        biomes.initWorldGenSeed(seed);
        genlayervoronoizoom.initWorldGenSeed(seed);

        genBiomes = biomes;
        biomeIndexLayer = genlayervoronoizoom;
    }

}
