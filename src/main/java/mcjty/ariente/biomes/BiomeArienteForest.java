package mcjty.ariente.biomes;

public class BiomeArienteForest extends AbstractArienteBiome {

    public BiomeArienteForest(BiomeProperties properties) {
        super(properties);
        this.decorator.treesPerChunk = 10;
        this.decorator.extraTreeChance = 0.1F;
        this.decorator.grassPerChunk = 2;
        this.decorator.flowersPerChunk = 0;
    }
}