package mcjty.ariente.biomes;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BiomeArientePlains extends AbstractArienteBiome {

    public BiomeArientePlains(BiomeProperties properties) {
        super(properties);
        this.decorator.treesPerChunk = 0;
        this.decorator.extraTreeChance = 0.1F;
        this.decorator.flowersPerChunk = 0;
        this.decorator.grassPerChunk = 10;
    }


    @Override
    public void decorate(World worldIn, Random rand, BlockPos pos) {
        super.decorate(worldIn, rand, pos);
        generateFlowers(worldIn, rand, 5);
    }
}
