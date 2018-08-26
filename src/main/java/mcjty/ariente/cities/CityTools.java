package mcjty.ariente.cities;

import mcjty.ariente.config.WorldgenConfiguration;
import mcjty.ariente.dimension.ArienteChunkGenerator;
import mcjty.ariente.dimension.ArienteCityGenerator;
import mcjty.ariente.dimension.ChunkHeightmap;
import mcjty.ariente.varia.ChunkCoord;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class CityTools {

    private static final Map<ChunkCoord, City> cities = new HashMap<>();

    public static City getCity(ChunkCoord center) {
        if (!cities.containsKey(center)) {
            City city = new City(center, getRandomCityPlan(center), -1);
            cacheCity(center, city);
        }
        return cities.get(center);
    }

    private static void cacheCity(ChunkCoord center, City city) {
        cities.put(center, city);
    }

    public static boolean isPortalChunk(int chunkX, int chunkZ) {
//        if (Math.abs(chunkX) < 32 && Math.abs(chunkZ) < 32) {
//            return false;
//        }

        return ((chunkX & 0xf) == 0) && ((chunkZ & 0xf) == 0);
    }

    private static boolean isCityCenter(ChunkCoord c) {
        int chunkX = c.getChunkX();
        int chunkZ = c.getChunkZ();
//        if (Math.abs(chunkX) < 32 && Math.abs(chunkZ) < 32) {
//            return false;
//        }

        boolean candidate = ((chunkX & 0xf) == 8) && ((chunkZ & 0xf) == 8);
        if (candidate) {
            long seed = DimensionManager.getWorld(0).getSeed();
            Random random = new Random(seed + chunkX * 776531419L + chunkZ * 198491317L);
            random.nextFloat();
            random.nextFloat();
            return random.nextFloat() < WorldgenConfiguration.CITY_DUNGEON_CHANCE;
        }
        return false;
    }

    public static boolean isCityChunk(int chunkX, int chunkZ) {
        return getCityIndex(chunkX, chunkZ) != null;
    }

    @Nullable
    public static CityIndex getCityIndex(int chunkX, int chunkZ) {
        ChunkCoord center = getNearestCityCenter(chunkX, chunkZ);
        if (center == null) {
            return null;
        }
        City city = getCity(center);
        CityPlan plan = city.getPlan();
        List<String> pattern = plan.getPlan();
        int dimX = pattern.get(0).length();
        int dimZ = pattern.size();
        int ox = (chunkX + dimX / 2) - center.getChunkX();
        int oz = (chunkZ + dimZ / 2) - center.getChunkZ();

        if (ox >= 0 && ox < dimX && oz >= 0 && oz < dimZ) {
            return new CityIndex(dimX, dimZ, ox, oz);
        } else {
            return null;
        }
    }

    // Return a random city plan. Use a valid city center as chunk coordinate parameter
    private static CityPlan getRandomCityPlan(ChunkCoord c) {
        int chunkX = c.getChunkX();
        int chunkZ = c.getChunkZ();
        long seed = DimensionManager.getWorld(0).getSeed();
        Random random = new Random(seed + chunkX * 198491317L + chunkZ * 776531419L);
        random.nextFloat();
        random.nextFloat();
        return AssetRegistries.CITYPLANS.get(random.nextInt(AssetRegistries.CITYPLANS.getCount()));
    }

    public static City getNearestCity(ArienteChunkGenerator generator, int chunkX, int chunkZ) {
        ChunkCoord center = getNearestCityCenter(chunkX, chunkZ);
        if (center == null) {
            return null;
        }
        City city = cities.get(center);
        if (city == null) {
            ChunkHeightmap heightmap = generator.getHeightmap(center.getChunkX(), center.getChunkZ());
            city = new City(center, getRandomCityPlan(center), heightmap.getAverageHeight());
            cacheCity(center, city);
        }
        return city;
    }

    @Nullable
    public static ChunkCoord getNearestCityCenter(int chunkX, int chunkZ) {
        int cx = (chunkX & ~0xf) + 8;
        int cz = (chunkZ & ~0xf) + 8;
        ChunkCoord cc = new ChunkCoord(cx, cz);
        if (isCityCenter(cc)) {
            return cc;
        } else {
            return null;
        }
    }

    public static BlockPos getNearestTeleportationSpot(BlockPos overworldPos) {
        ChunkCoord cc = ChunkCoord.getChunkCoordFromPos(overworldPos);
        int chunkX = cc.getChunkX();
        int chunkZ = cc.getChunkZ();
        int cx = (chunkX & ~0xf);
        int cz = (chunkZ & ~0xf);
        MinecraftServer server = DimensionManager.getWorld(0).getMinecraftServer();
        WorldServer world = server.getWorld(WorldgenConfiguration.DIMENSION_ID);
        ArienteChunkGenerator generator = (ArienteChunkGenerator) (world.getChunkProvider().chunkGenerator);
        int minHeight = ArienteCityGenerator.getPortalHeight(generator, cx, cz);
        return new BlockPos(cx * 16 + 8, minHeight+2, cz * 16 + 8);
    }

    @Nonnull
    public static Optional<ChunkCoord> getNearestCityCenterO(int chunkX, int chunkZ) {
        return Optional.ofNullable(getNearestCityCenter(chunkX, chunkZ));
    }

    public static int getLowestHeight(City city, ArienteChunkGenerator generator, int x, int z) {
        BuildingPart cellar = getCellarBuildingPart(city, x, z);
        if (cellar != null) {
            return city.getHeight(generator) - cellar.getSliceCount();
        } else {
            return city.getHeight(generator);
        }
    }

    @Nonnull
    public static List<BuildingPart> getBuildingParts(City city, int x, int z) {
        List<BuildingPart> parts = new ArrayList<>();
        BuildingPart cellar = getCellarBuildingPart(city, x, z);
        if (cellar != null) {
            parts.add(cellar);
        }
        BuildingPart part = getBuildingPart(city, x, z);
        if (part != null) {
            parts.add(part);
        }

        long seed = DimensionManager.getWorld(0).getSeed();
        Random random = new Random(seed + x * 6668353L + z * 666672943L);
        random.nextFloat();
        random.nextFloat();
        CityPlan plan = city.getPlan();
        int levels = plan.getMinLayer2() + random.nextInt(plan.getMaxLayer2() - plan.getMinLayer2() + 1);
        for (int i = 0 ; i < levels ; i++) {
            BuildingPart level2 = getLevel2BuildingPart(city, x, z, i);
            if (level2 != null) {
                parts.add(level2);
            }
        }

        part = getTopBuildingPart(city, x, z);
        if (part != null) {
            parts.add(part);
        }

        return parts;
    }

    @Nullable
    public static BuildingPart getLevel2BuildingPart(City city, int x, int z, int level) {
        CityPlan plan = city.getPlan();
        ChunkCoord cityCenter = city.getCenter();
        List<String> pattern = plan.getLayer2();
        if (pattern.isEmpty()) {
            return null;
        }
        return getCorrectPart(x, z, plan, cityCenter, pattern, 366670937L * (level+1L));
    }

    @Nullable
    public static BuildingPart getCellarBuildingPart(City city, int x, int z) {
        CityPlan plan = city.getPlan();
        ChunkCoord cityCenter = city.getCenter();
        List<String> pattern = plan.getCellar();
        if (pattern.isEmpty()) {
            return null;
        }

        return getCorrectPart(x, z, plan, cityCenter, pattern, 13);
    }

    @Nullable
    public static BuildingPart getTopBuildingPart(City city, int x, int z) {
        CityPlan plan = city.getPlan();
        ChunkCoord cityCenter = city.getCenter();
        List<String> pattern = plan.getTop();
        if (pattern.isEmpty()) {
            return null;
        }

        return getCorrectPart(x, z, plan, cityCenter, pattern, 137777);
    }

    @Nullable
    public static BuildingPart getBuildingPart(City city, int x, int z) {
        CityPlan plan = city.getPlan();
        ChunkCoord cityCenter = city.getCenter();
        List<String> pattern = plan.getPlan();
        if (pattern.isEmpty()) {
            return null;
        }
        return getCorrectPart(x, z, plan, cityCenter, pattern, 123);
    }

    private static BuildingPart getCorrectPart(int x, int z, CityPlan plan, ChunkCoord cityCenter, List<String> pattern,
                                               long randomSeed) {
        CityIndex index = getCityIndex(x, z);
        if (index != null) {
            Map<Character, List<String>> partPalette = plan.getPartPalette();
            char partChar = pattern.get(index.getZOffset()).charAt(index.getXOffset());
            if (partChar != ' ') {
                List<String> parts = partPalette.get(partChar);

                long seed = DimensionManager.getWorld(0).getSeed();
                Random random = new Random(x * 23567813L + z * 923568029L + randomSeed + seed);
                random.nextFloat();
                random.nextFloat();

                return AssetRegistries.PARTS.get(parts.get(random.nextInt(parts.size())));
            }
        }
        return null;
    }
}
