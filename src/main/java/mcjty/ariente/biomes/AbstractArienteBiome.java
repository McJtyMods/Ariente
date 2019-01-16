package mcjty.ariente.biomes;

import mcjty.ariente.biomes.features.WorldGenArianteVariantTree;
import mcjty.ariente.biomes.features.WorldGenArienteFlowers;
import mcjty.ariente.biomes.features.WorldGenArienteSmallTree;
import mcjty.ariente.biomes.features.WorldGenBlueTree;
import mcjty.ariente.blocks.ModBlocks;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockSand;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenHugeTrees;

import java.util.Random;

public abstract class AbstractArienteBiome extends Biome {

    private static final IBlockState GLOW_TRUNK = ModBlocks.glowlog.getDefaultState();
    private static final IBlockState GLOW_LEAVES = ModBlocks.glowleaves.getDefaultState();
    private static final IBlockState BLUE_TRUNK = ModBlocks.bluelog.getDefaultState();
    private static final IBlockState DARK_LEAVES = ModBlocks.darkleaves.getDefaultState();
    private static final IBlockState BLUE_LEAVES = ModBlocks.blueleaves.getDefaultState();

    private static final WorldGenArienteSmallTree GLASS_TREE = new WorldGenArienteSmallTree(GLOW_TRUNK, GLOW_LEAVES);
    private static final WorldGenArienteSmallTree DARK_TREE = new WorldGenArienteSmallTree(BLUE_TRUNK, DARK_LEAVES);
    private static final WorldGenArienteSmallTree SMALL_TREE = new WorldGenArienteSmallTree(BLUE_TRUNK, BLUE_LEAVES);
    private static final WorldGenArienteSmallTree DARK_TREE_V2 = new WorldGenArienteSmallTree(6, BLUE_TRUNK, DARK_LEAVES, false);
    private static final WorldGenArienteSmallTree SMALL_TREE_V2 = new WorldGenArienteSmallTree(6, BLUE_TRUNK, BLUE_LEAVES, false);
    private static final WorldGenHugeTrees HUGE_TREE = new WorldGenBlueTree(10, 20, BLUE_TRUNK, BLUE_LEAVES);
    private static final WorldGenArianteVariantTree VARIANT_TREE = new WorldGenArianteVariantTree();

    private WorldGenArienteFlowers flowers = new WorldGenArienteFlowers();

    public AbstractArienteBiome(BiomeProperties properties) {
        super(properties);
//        this.topBlock = Blocks.DIAMOND_BLOCK.getDefaultState();
//        this.fillerBlock = Blocks.MELON_BLOCK.getDefaultState();
    }

    @Override
    public void genTerrainBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal) {
        generateBlocks(worldIn, rand, chunkPrimerIn, x, z, noiseVal);
    }

    @Override
    public WorldGenAbstractTree getRandomTreeFeature(Random rand) {
        switch (rand.nextInt(10)) {
            case 0:
                return HUGE_TREE;
            case 1:
            case 2:
                return GLASS_TREE;
            case 3:
            case 4:
                return SMALL_TREE;
            case 5:
                return SMALL_TREE_V2;
            case 6:
                return DARK_TREE_V2;
            case 7:
                return VARIANT_TREE;
            default:
                return DARK_TREE;
        }
    }

    @Override
    public int getGrassColorAtPos(BlockPos pos) {
        return 0x448877;
//        return 0x3377aa;
    }

    @Override
    public int getWaterColorMultiplier() {
        return 0x55ff88;
    }

    @Override
    public int getFoliageColorAtPos(BlockPos pos) {
        return 0x224477;
    }

    protected final void generateBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal) {
        int i = worldIn.getSeaLevel();
        IBlockState iblockstate = this.topBlock;
        IBlockState iblockstate1 = this.fillerBlock;
        int j = -1;
        int k = (int) (noiseVal / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
        int dz = x & 15;    // swapped?
        int dx = z & 15;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int y = 255; y >= 0; --y) {
            if (y <= rand.nextInt(5)) {
                chunkPrimerIn.setBlockState(dx, y, dz, BEDROCK);
            } else {
                IBlockState iblockstate2 = chunkPrimerIn.getBlockState(dx, y, dz);

                if (iblockstate2.getMaterial() == Material.AIR) {
                    j = -1;
                } else if (iblockstate2.getBlock() == ModBlocks.marble) {
                    if (j == -1) {
                        if (k <= 0) {
                            iblockstate = AIR;
                            iblockstate1 = ModBlocks.marble.getDefaultState();
                        } else if (y >= i - 4 && y <= i + 1) {
                            iblockstate = this.topBlock;
                            iblockstate1 = this.fillerBlock;
                        }

                        if (y < i && (iblockstate == null || iblockstate.getMaterial() == Material.AIR)) {
                            if (this.getTemperature(blockpos$mutableblockpos.setPos(x, y, z)) < 0.15F) {
                                iblockstate = ICE;
                            } else {
                                iblockstate = WATER;
                            }
                        }

                        j = k;

                        if (y >= i - 1) {
                            chunkPrimerIn.setBlockState(dx, y, dz, iblockstate);
                        } else if (y < i - 7 - k) {
                            iblockstate = AIR;
                            iblockstate1 = ModBlocks.marble.getDefaultState();
                            chunkPrimerIn.setBlockState(dx, y, dz, GRAVEL);
                        } else {
                            chunkPrimerIn.setBlockState(dx, y, dz, iblockstate1);
                        }
                    } else if (j > 0) {
                        --j;
                        chunkPrimerIn.setBlockState(dx, y, dz, iblockstate1);

                        if (j == 0 && iblockstate1.getBlock() == Blocks.SAND && k > 1) {
                            j = rand.nextInt(4) + Math.max(0, y - 63);
                            iblockstate1 = iblockstate1.getValue(BlockSand.VARIANT) == BlockSand.EnumType.RED_SAND ? RED_SANDSTONE : SANDSTONE;
                        }
                    }
                }
            }
        }
    }

    protected void generateFlowers(World worldIn, Random random, int cnt) {
        for (int i = 0; i < cnt; ++i) {
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
