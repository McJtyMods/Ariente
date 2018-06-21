package mcjty.ariente.biomes;

public class BiomeArienteOcean extends AbstractArienteBiome {

    public BiomeArienteOcean(BiomeProperties properties) {
        super(properties);
        this.decorator.treesPerChunk = 0;
        this.decorator.extraTreeChance = 0;
        this.decorator.flowersPerChunk = 0;
    }
}
