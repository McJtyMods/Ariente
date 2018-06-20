package mcjty.ariente.biomes;

import mcjty.ariente.biomes.features.WorldGenGlassTree;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

import java.util.Random;

public class BiomeArientePlains extends AbstractArienteBiome {

    private static final WorldGenGlassTree GLASS_TREE = new WorldGenGlassTree(false);

    public BiomeArientePlains(BiomeProperties properties) {
        super(properties);
        this.decorator.treesPerChunk = 1;
        this.decorator.extraTreeChance = 0.1F;
        this.decorator.flowersPerChunk = 0;
        this.decorator.grassPerChunk = 0;
    }

    @Override
    public WorldGenAbstractTree getRandomTreeFeature(Random rand) {
        return GLASS_TREE;
    }


}
