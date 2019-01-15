package mcjty.ariente.biomes;

import mcjty.ariente.biomes.features.WorldGenArienteFlowers;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BiomeArienteForest extends AbstractArienteBiome {

    private WorldGenArienteFlowers flowers = new WorldGenArienteFlowers();

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

        for (int i = 0; i < 10; ++i) {
            int x = random.nextInt(16) + 8;
            int z = random.nextInt(16) + 8;
            int y = worldIn.getHeight(decorator.chunkPos.add(x, 0, z)).getY() + 32;

            if (y > 0) {
                int y2 = random.nextInt(y);
                BlockPos blockpos1 = decorator.chunkPos.add(x, y2, z);
                flowers.generate(worldIn, random, blockpos1);
            }
        }

    }
}