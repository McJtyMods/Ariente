package mcjty.ariente.biomes;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BiomeArienteForest extends AbstractArienteBiome {

    public BiomeArienteForest(BiomeProperties properties) {
        super(properties);
        this.decorator.treesPerChunk = 15;
        this.decorator.extraTreeChance = 0.1F;
        this.decorator.grassPerChunk = 2;
        this.decorator.flowersPerChunk = 0;
    }

    @Override
    public void decorate(World worldIn, Random random, BlockPos pos) {
        super.decorate(worldIn, random, pos);
        generateFlowers(worldIn, random, 20);
    }

}