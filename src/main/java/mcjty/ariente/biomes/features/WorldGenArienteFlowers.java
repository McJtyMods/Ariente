package mcjty.ariente.biomes.features;

import mcjty.ariente.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public class WorldGenArienteFlowers extends WorldGenerator {
    private IBlockState state;

    public WorldGenArienteFlowers() {
        state = ModBlocks.blackBush.getDefaultState();
    }


    public boolean generate(World world, Random rand, BlockPos position) {
        for (int i = 0; i < 64; ++i) {
            BlockPos blockpos = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));

            Block block = world.getBlockState(blockpos.down()).getBlock();
            if (world.isAirBlock(blockpos) && (blockpos.getY() < 255) && (block == Blocks.DIRT || block == Blocks.GRASS)) {
                world.setBlockState(blockpos, this.state, 2);
            }
        }

        return true;
    }
}