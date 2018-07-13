package mcjty.ariente.cities;

import mcjty.ariente.dimension.ArienteChunkGenerator;
import mcjty.ariente.dimension.ChunkHeightmap;
import mcjty.ariente.varia.ChunkCoord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class CityTools {

    private static final Map<ChunkCoord, City> cities = new HashMap<>();

    private static City getCity(ChunkCoord center) {
        return cities.get(center);
    }

    private static void cacheCity(ChunkCoord center, City city) {
        cities.put(center, city);
    }

    private static boolean isCityCenter(ChunkCoord c) {
        int chunkX = c.getChunkX();
        int chunkZ = c.getChunkZ();
        if (Math.abs(chunkX) < 32 && Math.abs(chunkZ) < 32) {
            return false;
        }

        boolean candidate = ((chunkX & 0xf) == 8) && ((chunkZ & 0xf) == 8);
        if (candidate) {
            Random random = new Random(chunkX * 776531419L + chunkZ * 198491317L);
            random.nextFloat();
            random.nextFloat();
            return random.nextFloat() < .5f;
        }
        return false;
    }

    // Return a random city plan. Use a valid city center as chunk coordinate parameter
    private static CityPlan getRandomCityPlan(ChunkCoord c) {
        int chunkX = c.getChunkX();
        int chunkZ = c.getChunkZ();
        Random random = new Random(chunkX * 198491317L + chunkZ * 776531419L);
        random.nextFloat();
        random.nextFloat();
        return AssetRegistries.CITYPLANS.get(random.nextInt(AssetRegistries.CITYPLANS.getCount()));
    }

    public static City getNearestCity(ArienteChunkGenerator generator, int chunkX, int chunkZ) {
        ChunkCoord center = getNearestCityCenter(chunkX, chunkZ);
        if (center == null) {
            return null;
        }
        City city = getCity(center);
        if (city == null) {
            ChunkHeightmap heightmap = generator.getHeightmap(center.getChunkX(), center.getChunkZ());
            city = new City(center, getRandomCityPlan(center), heightmap.getAverageHeight());
            cacheCity(center, city);
        }
        return city;
    }

    @Nullable
    private static ChunkCoord getNearestCityCenter(int chunkX, int chunkZ) {
        int cx = (chunkX & ~0xf) + 8;
        int cz = (chunkZ & ~0xf) + 8;
        ChunkCoord cc = new ChunkCoord(cx, cz);
        if (isCityCenter(cc)) {
            return cc;
        } else {
            return null;
        }
    }

    @Nonnull
    public static Optional<ChunkCoord> getNearestCityCenterO(int chunkX, int chunkZ) {
        return Optional.ofNullable(getNearestCityCenter(chunkX, chunkZ));
    }

    private static int getCityHeight(int x, int z) {
        return 70;
    }

    public static int getLowestHeight(City city, int x, int z) {
        BuildingPart cellar = getCellarBuildingPart(city, x, z);
        if (cellar != null) {
            return city.getHeight() - cellar.getSliceCount();
        } else {
            return city.getHeight();
        }
    }

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

        Random random = new Random(x * 6668353L + z * 666672943L);
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
        int dimX = pattern.get(0).length();
        int dimZ = pattern.size();
        int ox = (x + dimX / 2) - cityCenter.getChunkX();
        int oz = (z + dimZ / 2) - cityCenter.getChunkZ();
        if (ox >= 0 && ox < dimX && oz >= 0 && oz < dimZ) {
            Map<Character, List<String>> partPalette = plan.getPartPalette();
            char partChar = pattern.get(oz).charAt(ox);
            if (partChar != ' ') {
                List<String> parts = partPalette.get(partChar);

                Random random = new Random(x * 23567813L + z * 923568029L + randomSeed);
                random.nextFloat();
                random.nextFloat();

                return AssetRegistries.PARTS.get(parts.get(random.nextInt(parts.size())));
            }
        }
        return null;
    }
}
