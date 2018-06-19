package mcjty.ariente.biomes;

import net.minecraft.world.biome.Biome;

public class BiomeArientePlains extends Biome {

    public BiomeArientePlains(BiomeProperties properties) {
        super(properties);
        this.decorator.treesPerChunk = 0;
        this.decorator.extraTreeChance = 0;
        this.decorator.flowersPerChunk = 0;
        this.decorator.grassPerChunk = 0;
    }
}
