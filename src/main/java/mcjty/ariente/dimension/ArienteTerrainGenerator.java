package mcjty.ariente.dimension;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

import java.util.Random;

import static mcjty.ariente.dimension.GeneratorTools.setBlockState;

public class ArienteTerrainGenerator {
    private World world;
    private Random rand;

    private double[] depthBuffer = new double[256];

    private final double[] noiseField;
    private double[] noiseData1;
    private double[] noiseData2;
    private double[] noiseData3;
    private double[] noiseData4;

    private NoiseGeneratorOctaves noiseGen1;
    private NoiseGeneratorOctaves noiseGen2;
    private NoiseGeneratorOctaves noiseGen3;
    private NoiseGeneratorPerlin surfaceNoise;

    // A NoiseGeneratorOctaves used in generating terrain
    private NoiseGeneratorOctaves noiseGen6;

    private final float[] parabolicField;
    private double[] stoneNoise = new double[256];

    private IBlockState baseBlock;
    private boolean amplified;

    public ArienteTerrainGenerator() {
        this.noiseField = new double[825];

        this.parabolicField = new float[25];
        for (int j = -2; j <= 2; ++j) {
            for (int k = -2; k <= 2; ++k) {
                float f = (float) (10.0F / Math.sqrt((j * j + k * k) + 0.2F));
                this.parabolicField[j + 2 + (k + 2) * 5] = f;
            }
        }
    }

    public void setAmplified(boolean a) {
        amplified = a;
    }

    public void setup(World world, Random rand, IBlockState baseBlock) {
        this.rand = rand;
        this.world = world;
        this.baseBlock = baseBlock;
        amplified = false;

        this.noiseGen1 = new NoiseGeneratorOctaves(rand, 16);
        this.noiseGen2 = new NoiseGeneratorOctaves(rand, 16);
        this.noiseGen3 = new NoiseGeneratorOctaves(rand, 8);
        this.surfaceNoise = new NoiseGeneratorPerlin(rand, 4);
        NoiseGeneratorOctaves noiseGen5 = new NoiseGeneratorOctaves(rand, 10);
        this.noiseGen6 = new NoiseGeneratorOctaves(rand, 16);
        NoiseGeneratorOctaves mobSpawnerNoise = new NoiseGeneratorOctaves(rand, 8);

        net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextOverworld ctx =
                new net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextOverworld(noiseGen1, noiseGen2, noiseGen3, surfaceNoise, noiseGen5, noiseGen6, mobSpawnerNoise);
        ctx = net.minecraftforge.event.terraingen.TerrainGen.getModdedNoiseGenerators(world, rand, ctx);
        this.noiseGen1 = ctx.getLPerlin1();
        this.noiseGen2 = ctx.getLPerlin2();
        this.noiseGen3 = ctx.getPerlin();
        this.surfaceNoise = ctx.getHeight();
//        this.field_185983_b = ctx.getScale();
        this.noiseGen6 = ctx.getDepth();
//        this.field_185985_d = ctx.getForest();
    }


    private void generateHeightmap(int chunkX4, int chunkY4, int chunkZ4, Biome[] biomesForGeneration) {
        this.noiseData4 = this.noiseGen6.generateNoiseOctaves(this.noiseData4, chunkX4, chunkZ4, 5, 5, 200.0D, 200.0D, 0.5D);
        this.noiseData1 = this.noiseGen3.generateNoiseOctaves(this.noiseData1, chunkX4, chunkY4, chunkZ4, 5, 33, 5, 8.555150000000001D, 4.277575000000001D, 8.555150000000001D);
        this.noiseData2 = this.noiseGen1.generateNoiseOctaves(this.noiseData2, chunkX4, chunkY4, chunkZ4, 5, 33, 5, 684.412D, 684.412D, 684.412D);
        this.noiseData3 = this.noiseGen2.generateNoiseOctaves(this.noiseData3, chunkX4, chunkY4, chunkZ4, 5, 33, 5, 684.412D, 684.412D, 684.412D);
        int l = 0;
        int i1 = 0;

        for (int j1 = 0; j1 < 5; ++j1) {
            for (int k1 = 0; k1 < 5; ++k1) {
                float f = 0.0F;
                float f1 = 0.0F;
                float f2 = 0.0F;
                byte b0 = 2;

                Biome biome = biomesForGeneration[j1 + 2 + (k1 + 2) * 10];

                for (int l1 = -b0; l1 <= b0; ++l1) {
                    for (int i2 = -b0; i2 <= b0; ++i2) {
                        float baseHeight = biome.getBaseHeight();
                        float variation = biome.getHeightVariation();

                        if (amplified && baseHeight > 0.0F) {
                            baseHeight = 1.0F + baseHeight * 2.0F;
                            variation = 1.0F + variation * 4.0F;
                        }

                        float f5 = parabolicField[l1 + 2 + (i2 + 2) * 5] / (baseHeight + 2.0F);
                        f += variation * f5;
                        f1 += baseHeight * f5;
                        f2 += f5;
                    }
                }

                f /= f2;
                f1 /= f2;
                f = f * 0.9F + 0.1F;
                f1 = (f1 * 4.0F - 1.0F) / 8.0F;
                double d12 = this.noiseData4[i1] / 8000.0D;

                if (d12 < 0.0D) {
                    d12 = -d12 * 0.3D;
                }

                d12 = d12 * 3.0D - 2.0D;

                if (d12 < 0.0D) {
                    d12 /= 2.0D;

                    if (d12 < -1.0D) {
                        d12 = -1.0D;
                    }

                    d12 /= 1.4D;
                    d12 /= 2.0D;
                } else {
                    if (d12 > 1.0D) {
                        d12 = 1.0D;
                    }

                    d12 /= 8.0D;
                }

                ++i1;
                double d13 = f1;
                double d14 = f;
                d13 += d12 * 0.2D;
                d13 = d13 * 8.5D / 8.0D;
                double d5 = 8.5D + d13 * 4.0D;

                for (int j2 = 0; j2 < 33; ++j2) {
                    double d6 = (j2 - d5) * 12.0D * 128.0D / 256.0D / d14;

                    if (d6 < 0.0D) {
                        d6 *= 4.0D;
                    }

                    double d7 = this.noiseData2[l] / 512.0D;
                    double d8 = this.noiseData3[l] / 512.0D;
                    double d9 = (this.noiseData1[l] / 10.0D + 1.0D) / 2.0D;
                    double d10 = (d9 < 0.0D ? d7 : (d9 > 1.0D ? d8 : d7 + (d8 - d7) * d9)) - d6;

                    if (j2 > 29) {
                        double d11 = ((j2 - 29) / 3.0F);
                        d10 = d10 * (1.0D - d11) + -10.0D * d11;
                    }

                    this.noiseField[l] = d10;
                    ++l;
                }
            }
        }
    }


    public void generate(int chunkX, int chunkZ, ChunkPrimer primer, Biome[] biomesForGeneration) {
        generateHeightmap(chunkX * 4, 0, chunkZ * 4, biomesForGeneration);

        byte waterLevel = 63;
        for (int x4 = 0; x4 < 4; ++x4) {
            int l = x4 * 5;
            int i1 = (x4 + 1) * 5;

            for (int z4 = 0; z4 < 4; ++z4) {
                int k1 = (l + z4) * 33;
                int l1 = (l + z4 + 1) * 33;
                int i2 = (i1 + z4) * 33;
                int j2 = (i1 + z4 + 1) * 33;

                for (int height32 = 0; height32 < 32; ++height32) {
                    double d0 = 0.125D;
                    double d1 = noiseField[k1 + height32];
                    double d2 = noiseField[l1 + height32];
                    double d3 = noiseField[i2 + height32];
                    double d4 = noiseField[j2 + height32];
                    double d5 = (noiseField[k1 + height32 + 1] - d1) * d0;
                    double d6 = (noiseField[l1 + height32 + 1] - d2) * d0;
                    double d7 = (noiseField[i2 + height32 + 1] - d3) * d0;
                    double d8 = (noiseField[j2 + height32 + 1] - d4) * d0;

                    for (int h = 0; h < 8; ++h) {
                        double d9 = 0.25D;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * d9;
                        double d13 = (d4 - d2) * d9;
                        int height = (height32 * 8) + h;

                        for (int x = 0; x < 4; ++x) {
                            int index = ((x + (x4 * 4)) << 12) | ((0 + (z4 * 4)) << 8) | height;
                            short maxheight = 256;
                            index -= maxheight;
                            double d14 = 0.25D;
                            double d16 = (d11 - d10) * d14;
                            double d15 = d10 - d16;

                            for (int z = 0; z < 4; ++z) {
                                index += maxheight;
                                if (height < 2) {
                                    setBlockState(primer, index, Blocks.BEDROCK.getDefaultState());
                                } else if ((d15 += d16) > 0.0D) {
                                    setBlockState(primer, index, baseBlock);
                                } else {
                                    setBlockState(primer, index, Blocks.AIR.getDefaultState());
                                }
                            }

                            d10 += d12;
                            d11 += d13;
                        }

                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }
                }
            }
        }
    }

    public void replaceBiomeBlocks(int x, int z, ChunkPrimer primer, Biome[] biomesIn) {
        double d0 = 0.03125D;
        this.depthBuffer = this.surfaceNoise.getRegion(this.depthBuffer, (x * 16), (z * 16), 16, 16, 0.0625D, 0.0625D, 1.0D);

        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                Biome biome = biomesIn[j + i * 16];
                biome.genTerrainBlocks(this.world, this.rand, primer, x * 16 + i, z * 16 + j, this.depthBuffer[j + i * 16]);
            }
        }
    }

}
