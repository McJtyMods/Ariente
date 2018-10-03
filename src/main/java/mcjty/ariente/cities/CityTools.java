package mcjty.ariente.cities;

import mcjty.ariente.config.WorldgenConfiguration;
import mcjty.ariente.dimension.ArienteChunkGenerator;
import mcjty.ariente.dimension.ArienteCityGenerator;
import mcjty.ariente.varia.ChunkCoord;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public class CityTools {

    private static final Map<ChunkCoord, City> cities = new HashMap<>();

    // Mostly for editmode purposes
    private static final Map<ChunkCoord, Map<String, Integer>> cachedVariantSelections = new HashMap<>();

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

    public static Map<String, Integer> getVariantSelections(ChunkCoord center) {
        if (!cachedVariantSelections.containsKey(center)) {
            long seed = DimensionManager.getWorld(0).getSeed();
            Random random = new Random(seed + center.getChunkZ() * 198491317L + center.getChunkX() * 776531419L);
            random.nextFloat();
            City city = getCity(center);
            Map<String, Integer> variants = new HashMap<>();
            for (Map.Entry<String, Integer> entry : city.getPlan().getVariants().entrySet()) {
                variants.put(entry.getKey(), random.nextInt(entry.getValue()));
            }
            cachedVariantSelections.put(center, variants);
        }
        return cachedVariantSelections.get(center);
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
//        if (candidate) {
//            long seed = DimensionManager.getWorld(0).getSeed();
//            Random random = new Random(seed + chunkX * 776531419L + chunkZ * 198491317L);
//            random.nextFloat();
//            random.nextFloat();
//            return random.nextFloat() < WorldgenConfiguration.CITY_DUNGEON_CHANCE;
//        }
//        return false;
        // @todo always return a city. But it can be the 0_0 city which is basically empty and takes care of the station below it
        return true;
    }

    public static boolean isCityChunk(int chunkX, int chunkZ) {
        return getCityIndex(chunkX, chunkZ) != null;
    }

    public static boolean isStationChunk(int chunkX, int chunkZ) {
        int cx = chunkX & 0xf;
        int cz = chunkZ & 0xf;
        return cx >= 7 && cx <= 9 && cz >= 7 && cz <= 9;
    }

    @Nullable
    public static CityIndex getCityIndex(int chunkX, int chunkZ) {
        ChunkCoord center = getNearestCityCenter(chunkX, chunkZ);
        if (center == null) {
            return null;
        }
        City city = getCity(center);
        CityPlan plan = city.getPlan();
        return getCityIndex(chunkX, chunkZ, center, plan);
    }

    public static CityIndex getCityIndex(int chunkX, int chunkZ, ChunkCoord center, CityPlan plan) {
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
        CityPlan plan = AssetRegistries.CITYPLANS.get(random.nextInt(AssetRegistries.CITYPLANS.getCount()));
        while (!plan.isCity()) {
            plan = AssetRegistries.CITYPLANS.get(random.nextInt(AssetRegistries.CITYPLANS.getCount()));
        }
        return plan;
    }

    public static City getNearestCity(ArienteChunkGenerator generator, int chunkX, int chunkZ) {
        ChunkCoord center = getNearestCityCenter(chunkX, chunkZ);
        if (center == null) {
            return null;
        }
        City city = cities.get(center);
        if (city == null) {
//            ChunkHeightmap heightmap = generator.getHeightmap(center.getChunkX(), center.getChunkZ());
            city = new City(center, getRandomCityPlan(center), -1);
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

    @Nonnull
    public static ChunkCoord getNearestStationCenter(int chunkX, int chunkZ) {
        int cx = (chunkX & ~0xf) + 8;
        int cz = (chunkZ & ~0xf) + 8;
        return new ChunkCoord(cx, cz);
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
        return new BlockPos(cx * 16 + 8, minHeight + 2, cz * 16 + 8);
    }

    @Nonnull
    public static Optional<ChunkCoord> getNearestCityCenterO(int chunkX, int chunkZ) {
        return Optional.ofNullable(getNearestCityCenter(chunkX, chunkZ));
    }

    public static int getLowestHeight(City city, ArienteChunkGenerator generator, int x, int z) {
        BuildingPart cellar = getPart(x, z, getCityIndex(x, z), city.getPlan(), city.getPlan().getCellar(), 13);
        if (cellar != null) {
            return city.getHeight(generator) - cellar.getSliceCount();
        } else {
            return city.getHeight(generator);
        }
    }

    @Nonnull
    public static List<BuildingPart> getStationParts(int chunkX, int chunkZ) {
        BuildingPart part = getStationPart(chunkX, chunkZ);
        if (part != null) {
            return Collections.singletonList(part);
        } else {
            return Collections.emptyList();
        }
    }

    @Nullable
    public static BuildingPart getStationPart(int chunkX, int chunkZ) {
        CityPlan station = AssetRegistries.CITYPLANS.get("station");
        CityIndex index = CityTools.getCityIndex(chunkX, chunkZ, CityTools.getNearestStationCenter(chunkX, chunkZ), station);
        if (index == null) {
            return null;
        }
        return CityTools.getPart(chunkX, chunkZ, index, station, station.getPlan(), 3939321);
    }

    public static int getStationHeight() {
        return 30;
    }

    @Nonnull
    public static List<PartPalette> getPartPalettes(City city, int x, int z) {
        List<PartPalette> parts = new ArrayList<>();
        CityIndex cityIndex = getCityIndex(x, z);
        CityPlan plan = city.getPlan();

        Stream.of(getPartPalette(x, z, cityIndex, plan, plan.getCellar(), 13),
                getPartPalette(x, z, cityIndex, plan, plan.getPlan(), 123),
                getPartPalette(x, z, cityIndex, plan, plan.getLayer2(), 366670937L * 57),
                getPartPalette(x, z, cityIndex, plan, plan.getTop(), 137777))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(parts::add);
        return parts;
    }

    @Nonnull
    public static List<BuildingPart> getBuildingParts(City city, int x, int z) {
        Map<String, Integer> variantSelections = getVariantSelections(city.getCenter());

        List<PartPalette> partPalettes = getPartPalettes(city, x, z);
        long randomSeed = 7;
        List<BuildingPart> parts = new ArrayList<>();
        long seed = DimensionManager.getWorld(0).getSeed();
        for (PartPalette palette : partPalettes) {
            String variantName = palette.getVariant();
            int variant = (variantName == null || variantName.isEmpty()) ? 0 : variantSelections.getOrDefault(variantName, 0);
            List<String> p = palette.getPalette();
            if (variant >= p.size()) {
                variant = 0;
            }
            parts.add(AssetRegistries.PARTS.get(p.get(variant)));
        }
        return parts;
    }

    public static BuildingPart getPart(int x, int z, CityIndex index, CityPlan plan, List<String> pattern, long randomSeed) {
        if (pattern.isEmpty()) {
            return null;
        }
        if (index == null) {
            return null;
        }
        Map<Character, PartPalette> partPalette = plan.getPartPalette();
        char partChar = pattern.get(index.getZOffset()).charAt(index.getXOffset());
        if (partChar != ' ') {
            List<String> parts = partPalette.get(partChar).getPalette();

            long seed = DimensionManager.getWorld(0).getSeed();
            Random random = new Random(x * 23567813L + z * 923568029L + randomSeed + seed);
            random.nextFloat();
            random.nextFloat();

            return AssetRegistries.PARTS.get(parts.get(random.nextInt(parts.size())));
        }
        return null;
    }

    public static Optional<PartPalette> getPartPalette(int x, int z, CityIndex index, CityPlan plan, List<String> pattern, long randomSeed) {
        if (pattern.isEmpty()) {
            return Optional.empty();
        }
        if (index == null) {
            return Optional.empty();
        }
        Map<Character, PartPalette> partPalette = plan.getPartPalette();
        char partChar = pattern.get(index.getZOffset()).charAt(index.getXOffset());
        if (partChar != ' ') {
            return Optional.ofNullable(partPalette.get(partChar));
        }
        return Optional.empty();
    }

}
