package mcjty.ariente.biomes;

public class BiomeArienteHills extends AbstractArienteBiome {

    public BiomeArienteHills(BiomeProperties properties) {
        super(properties);
        this.decorator.treesPerChunk = 0;
        this.decorator.extraTreeChance = 0;
        this.decorator.flowersPerChunk = 0;
        this.decorator.grassPerChunk = 0;
    }

}
