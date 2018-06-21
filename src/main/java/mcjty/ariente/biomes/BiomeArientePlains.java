package mcjty.ariente.biomes;

public class BiomeArientePlains extends AbstractArienteBiome {

    public BiomeArientePlains(BiomeProperties properties) {
        super(properties);
        this.decorator.treesPerChunk = 0;
        this.decorator.extraTreeChance = 0.1F;
        this.decorator.flowersPerChunk = 0;
        this.decorator.grassPerChunk = 10;
    }
}
