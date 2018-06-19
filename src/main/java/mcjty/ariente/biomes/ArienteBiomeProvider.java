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
        makeLayers(world.getSeed());
    }

    private void makeLayers(long seed) {
        GenLayer biomes = new GenLayerArienteBiomes(1L);
//        biomes = new GenLayerTFKeyBiomes(1000L, biomes);
//        biomes = new GenLayerTFCompanionBiomes(1000L, biomes);

        biomes = new GenLayerZoom(1000L, biomes);
        biomes = new GenLayerZoom(1001, biomes);

//        biomes = new GenLayerTFBiomeStabilize(700L, biomes);

//        biomes = new GenLayerTFThornBorder(500L, biomes);

        biomes = new GenLayerZoom(1002, biomes);
        biomes = new GenLayerZoom(1003, biomes);
        biomes = new GenLayerZoom(1004, biomes);
        biomes = new GenLayerZoom(1005, biomes);

//        GenLayer riverLayer = new GenLayerTFStream(1L, biomes);
//        riverLayer = new GenLayerSmooth(7000L, riverLayer);
//        biomes = new GenLayerTFRiverMix(100L, biomes, riverLayer);

        // do "voronoi" zoom
        GenLayer genlayervoronoizoom = new GenLayerVoronoiZoom(10L, biomes);

        biomes.initWorldGenSeed(seed);
        genlayervoronoizoom.initWorldGenSeed(seed);

        genBiomes = biomes;
        biomeIndexLayer = genlayervoronoizoom;
    }

}
