package mcjty.ariente.dimension;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.entities.DroneEntity;
import mcjty.ariente.varia.ChunkCoord;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
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
//    private MapGenLowTendrils tendrilGenerator = new MapGenLowTendrils(BlockRegister.hardDirtBlock.getDefaultState());
    private ArienteTerrainGenerator terraingen = new ArienteTerrainGenerator();
//    private IslandTerrainGenerator islandgen = new IslandTerrainGenerator(IslandTerrainGenerator.ISLANDS);
    private IslandsTerrainGenerator islandsGen = new IslandsTerrainGenerator();
//    private IslandTerrainGenerator island2gen = new IslandTerrainGenerator(IslandTerrainGenerator.PLATEAUS);
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

    @Override
    public Chunk generateChunk(int x, int z) {
        ChunkPrimer chunkprimer = getChunkPrimer(x, z);

//        this.biomesForGeneration = worldObj.getBiomeProvider().getBiomesForGeneration(this.biomesForGeneration, x * 4 - 2, z * 4 - 2, 10, 10);
//        terraingen.generate(x, z, chunkprimer, this.biomesForGeneration);
//        islandsGen.setBlocksInChunk(x, z, chunkprimer);

        this.biomesForGeneration = worldObj.getBiomeProvider().getBiomes(this.biomesForGeneration, x * 16, z * 16, 16, 16);
        terraingen.replaceBiomeBlocks(x, z, chunkprimer, biomesForGeneration);

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
