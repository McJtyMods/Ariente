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
    private IBlockState[] states = new IBlockState[2];

    public WorldGenArienteFlowers() {
        states[0] = ModBlocks.blackBush.getDefaultState();
        states[1] = ModBlocks.darkGrass.getDefaultState();
    }


    public boolean generate(World world, Random rand, BlockPos position) {
        for (int i = 0; i < 64; ++i) {
            BlockPos blockpos = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));

            Block block = world.getBlockState(blockpos.down()).getBlock();
            if (world.isAirBlock(blockpos) && (blockpos.getY() < 255) && (block == Blocks.DIRT || block == Blocks.GRASS)) {
                world.setBlockState(blockpos, this.states[rand.nextInt(2)], 2);
            }
        }

        return true;
    }
}