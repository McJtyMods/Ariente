package mcjty.ariente.dimension;

import com.google.common.collect.ImmutableList;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.cities.BuildingPart;
import mcjty.ariente.cities.City;
import mcjty.ariente.cities.CityTools;
import mcjty.ariente.cities.ICityEquipment;
import mcjty.ariente.varia.ChunkCoord;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.event.terraingen.TerrainGen;

import javax.annotation.Nullable;
import java.util.*;

import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.CAVE;

public class ArienteChunkGenerator implements IChunkGenerator {

    private final World worldObj;
    private Random random;
    private Biome[] biomesForGeneration;

//    private List<Biome.SpawnListEntry> mobs = Lists.newArrayList(new Biome.SpawnListEntry(DroneEntity.class, 20, 1, 1));
    private List<Biome.SpawnListEntry> mobs = Collections.emptyList();

    private MapGenBase caveGenerator = new MapGenCaves();
    private ArienteTerrainGenerator terraingen = new ArienteTerrainGenerator();
    private IslandsTerrainGenerator islandsGen = new IslandsTerrainGenerator();
    private ArienteCityGenerator cityGenerator = new ArienteCityGenerator();

    private Map<ChunkCoord, ChunkPrimer> cachedPrimers = new HashMap<>();
    private Map<ChunkCoord, ChunkHeightmap> cachedHeightmaps = new HashMap<>();

    public ArienteChunkGenerator(World worldObj) {
        this.worldObj = worldObj;
        long seed = worldObj.getSeed();
        this.random = new Random((seed + 516) * 314);
        terraingen.setup(worldObj, random, ModBlocks.marble.getDefaultState());
        islandsGen.setup(worldObj, worldObj.getSeed());
//        islandgen.setup(worldObj, random, this, 40);
//        island2gen.setup(worldObj, new Random((seed + 314) * 516), this, 40);
        caveGenerator = TerrainGen.getModdedMapGen(caveGenerator, CAVE);
        cityGenerator.initialize(this);
    }

    // Get a heightmap for a chunk. If needed calculate (and cache) a primer
    public ChunkHeightmap getHeightmap(int chunkX, int chunkZ) {
        ChunkCoord key = new ChunkCoord(chunkX, chunkZ);
        if (cachedHeightmaps.containsKey(key)) {
            return cachedHeightmaps.get(key);
        } else if (cachedPrimers.containsKey(key)) {
            ChunkHeightmap heightmap = new ChunkHeightmap(cachedPrimers.get(key));
            cachedHeightmaps.put(key, heightmap);
            return heightmap;
        } else {
            ChunkPrimer primer = generatePrimer(chunkX, chunkZ);
            cachedPrimers.put(key, primer);
            ChunkHeightmap heightmap = new ChunkHeightmap(cachedPrimers.get(key));
            cachedHeightmaps.put(key, heightmap);
            return heightmap;
        }
    }

    public ChunkPrimer generatePrimer(int chunkX, int chunkZ) {
        ChunkPrimer chunkprimer = new ChunkPrimer();

        this.biomesForGeneration = worldObj.getBiomeProvider().getBiomesForGeneration(this.biomesForGeneration, chunkX * 4 - 2, chunkZ * 4 - 2, 10, 10);
        terraingen.generate(chunkX, chunkZ, chunkprimer, this.biomesForGeneration);
        islandsGen.setBlocksInChunk(chunkX, chunkZ, chunkprimer);

        return chunkprimer;
    }


    private ChunkPrimer getChunkPrimer(int chunkX, int chunkZ) {
        ChunkPrimer chunkprimer;
        ChunkCoord key = new ChunkCoord(chunkX, chunkZ);
        if (cachedPrimers.containsKey(key)) {
            // We calculated a primer earlier. Reuse it
            chunkprimer = cachedPrimers.get(key);
            cachedPrimers.remove(key);
        } else {
            chunkprimer = generatePrimer(chunkX, chunkZ);
        }
        // Calculate the chunk heightmap in case we need it later
        if (!cachedHeightmaps.containsKey(key)) {
            // We might need this later
            cachedHeightmaps.put(key, new ChunkHeightmap(chunkprimer));
        }
        return chunkprimer;
    }

    private NoiseGeneratorPerlin varianceNoise;
    private double[] varianceBuffer = new double[256];

    private void fixForNearbyCity(ChunkPrimer primer, int x, int z) {
        if (varianceNoise == null) {
            this.varianceNoise = new NoiseGeneratorPerlin(random, 4);
        }
        this.varianceBuffer = this.varianceNoise.getRegion(this.varianceBuffer, (x * 16), (z * 16), 16, 16, 1.0 / 16.0, 1.0 / 16.0, 1.0D);
        char air = (char) Block.BLOCK_STATE_IDS.get(Blocks.AIR.getDefaultState());

        if (CityTools.isCityChunk(x, z)) {
            ChunkCoord center = CityTools.getNearestCityCenter(x, z);
            City city = CityTools.getCity(center);
            int height = city.getHeight(this);
            for (int dx = 0; dx < 16; dx++) {
                for (int dz = 0; dz < 16; dz++) {
                    int index = (dx << 12) | (dz << 8) + height;
                    PrimerTools.setBlockStateRange(primer, index, index + 128 - height, air);
                }
            }
        } else {
            int[][] cityHeights = new int[3][3];
            boolean hasCity = false;
            int height = 0;
            for (int cx = -1 ; cx <= 1 ; cx++) {
                for (int cz = -1 ; cz <= 1 ; cz++) {
                    if (CityTools.isCityChunk(x+cx, z+cz)) {
                        ChunkCoord center = CityTools.getNearestCityCenter(x+cx, z+cz);
                        City city = CityTools.getCity(center);
                        height = city.getHeight(this)+2;
                        cityHeights[cx+1][cz+1] = height;
                        hasCity = true;
                    } else {
                        cityHeights[cx+1][cz+1] = -1;
                    }
                }
            }
            if (!hasCity) {
                return;
            }
            for (int dx = 0; dx < 16; dx++) {
                for (int dz = 0; dz < 16; dz++) {
                    double vr = varianceBuffer[dx + dz * 16];
                    int mindist = getMinDist(cityHeights, dx, dz);
                    if (mindist <= 1) {
                        vr /= 3.0f;
                    } else if (mindist <= 2) {
                        vr /= 2.0f;
                    } else if (mindist <= 3) {
                        vr /= 1.5f;
                    }
                    int dh = mindist * 2 + height + (int) vr;
                    int index = (dx << 12) | (dz << 8) + dh;
                    PrimerTools.setBlockStateRangeSafe(primer, index, index + 128 - dh, air);
                }
            }
        }
    }

    private int getMinDist(int[][] cityHeights, int dx, int dz) {
        int mindist = 1000;
        if (cityHeights[0][1] != -1) {
            mindist = dx;
        }
        if (cityHeights[0][0] != -1) {
            int dist = Math.max(dx, dz);
            if (dist < mindist) {
                mindist = dist;
            }
        }
        if (cityHeights[0][2] != -1) {
            int dist = Math.max(dx, 15-dz);
            if (dist < mindist) {
                mindist = dist;
            }
        }
        if (cityHeights[2][1] != -1) {
            int dist = 15-dx;
            if (dist < mindist) {
                mindist = dist;
            }
        }
        if (cityHeights[2][0] != -1) {
            int dist = Math.max(15-dx, dz);
            if (dist < mindist) {
                mindist = dist;
            }
        }
        if (cityHeights[2][2] != -1) {
            int dist = Math.max(15-dx, 15-dz);
            if (dist < mindist) {
                mindist = dist;
            }
        }
        if (cityHeights[1][0] != -1) {
            int dist = dz;
            if (dist < mindist) {
                mindist = dist;
            }
        }
        if (cityHeights[1][2] != -1) {
            int dist = 15-dz;
            if (dist < mindist) {
                mindist = dist;
            }
        }
        return mindist;
    }

    private static int bipolate(float h00, float h10, float h01, float h11, int dx, int dz) {
        float factor = (15.0f - dx) / 15.0f;
        float h0 = h00 + (h10 - h00) * factor;
        float h1 = h01 + (h11 - h01) * factor;
        float h = h0 + (h1 - h0) * (15.0f - dz) / 15.0f;
        return (int) h;
    }


    @Override
    public Chunk generateChunk(int x, int z) {
        ChunkPrimer chunkprimer = getChunkPrimer(x, z);

        this.biomesForGeneration = worldObj.getBiomeProvider().getBiomes(this.biomesForGeneration, x * 16, z * 16, 16, 16);
        terraingen.replaceBiomeBlocks(x, z, chunkprimer, biomesForGeneration);

        fixForNearbyCity(chunkprimer, x, z);

        this.caveGenerator.generate(this.worldObj, x, z, chunkprimer);

        cityGenerator.generate(this.worldObj, x, z, chunkprimer);

        Chunk chunk = new Chunk(this.worldObj, chunkprimer, x, z);

        byte[] biomeArray = chunk.getBiomeArray();
        for (int i = 0; i < biomeArray.length; ++i) {
            biomeArray[i] = (byte)Biome.getIdForBiome(this.biomesForGeneration[i]);
        }

        chunk.generateSkylightMap();
        return chunk;
    }

    @Override
    public void populate(int x, int z) {
        int i = x * 16;
        int j = z * 16;
        BlockPos blockpos = new BlockPos(i, 0, j);

        this.random.setSeed(this.worldObj.getSeed());
        long k = this.random.nextLong() / 2L * 2L + 1L;
        long l = this.random.nextLong() / 2L * 2L + 1L;
        this.random.setSeed((long)x * k + (long)z * l ^ this.worldObj.getSeed());

        Biome biome = this.worldObj.getBiome(blockpos.add(16, 0, 16));
        biome.decorate(this.worldObj, this.random, new BlockPos(i, 0, j));
        WorldEntitySpawner.performWorldGenSpawning(this.worldObj, biome, i + 8, j + 8, 16, 16, this.random);

        if (CityTools.isCityChunk(x, z)) {
            System.out.println("Fixing tile entities in chunk " + x + "," + z);
            fixTileEntities(x, z);
        }
    }

    private void fixTileEntities(int x, int z) {
        City city = CityTools.getNearestCity(this, x, z);
        List<BuildingPart> parts = CityTools.getBuildingParts(city, x, z);
        int lowestY = CityTools.getLowestHeight(city, this, x, z);
        int y = lowestY;
        // We need the parts again to load the equipment data
        Map<BlockPos, Map<String, Object>> equipment = new HashMap<>();
        for (BuildingPart part : parts) {
            for (Map.Entry<BlockPos, Map<String, Object>> entry : part.getTeInfo().entrySet()) {
                equipment.put(new BlockPos(x*16, y, z*16).add(entry.getKey()), entry.getValue());
            }
            y += part.getSliceCount();
        }

        for (int dx = 0 ; dx < 16 ; dx++) {
            for (int dy = 0 ; dy < 256 ; dy++) {
                for (int dz = 0 ; dz < 16 ; dz++) {
                    BlockPos p = new BlockPos(x*16 + dx, dy, z*16 + dz);
                    TileEntity te = worldObj.getTileEntity(p);
                    if (te instanceof GenericTileEntity) {
                        IBlockState state = worldObj.getBlockState(p);
                        worldObj.setBlockState(p, state, 3);
                        ((GenericTileEntity) te).markDirtyClient();
                    }
                    if (te instanceof ICityEquipment && equipment.containsKey(p)) {
                        ((ICityEquipment)te).load(equipment.get(p));
                    }
                }
            }
        }
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        return false;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        if (creatureType == EnumCreatureType.MONSTER){
            return mobs;
        }
        return ImmutableList.of();
    }

    @Nullable
    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
        return null;
    }

    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
        return false;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {

    }
}
