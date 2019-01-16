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

    public static final IBlockState BLACK_BUSH = ModBlocks.blackBush.getDefaultState();
    public static final IBlockState DARK_GRASS = ModBlocks.darkGrass.getDefaultState();
    public static final IBlockState SMALL_FLOWER = ModBlocks.smallFlower.getDefaultState();

    public WorldGenArienteFlowers() {
    }


    public boolean generate(World world, Random rand, BlockPos position) {
        for (int i = 0; i < 64; ++i) {
            BlockPos blockpos = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));

            Block block = world.getBlockState(blockpos.down()).getBlock();
            if (world.isAirBlock(blockpos) && (blockpos.getY() < 255) && (block == Blocks.DIRT || block == Blocks.GRASS)) {
                IBlockState flowerState;
                switch (rand.nextInt(10)) {
                    case 0:
                    case 1:
                        flowerState = BLACK_BUSH; break;
                    case 2:
                    case 3:
                        flowerState = DARK_GRASS; break;
                    default:
                        flowerState = SMALL_FLOWER; break;

                }
                world.setBlockState(blockpos, flowerState, 2);
            }
        }

        return true;
    }
}