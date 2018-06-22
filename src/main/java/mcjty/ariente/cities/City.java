package mcjty.ariente.cities;

import mcjty.ariente.varia.ChunkCoord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class City {

    public static boolean isCityCenter(ChunkCoord c) {
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
    public static CityPlan getRandomCityPlan(ChunkCoord c) {
        int chunkX = c.getChunkX();
        int chunkZ = c.getChunkZ();
        Random random = new Random(chunkX * 198491317L + chunkZ * 776531419L);
        random.nextFloat();
        random.nextFloat();
        return AssetRegistries.CITYPLANS.get(random.nextInt(AssetRegistries.CITYPLANS.getCount()));
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
    public static Optional<ChunkCoord> getNearestCity(int chunkX, int chunkZ) {
        return Optional.ofNullable(getNearestCityCenter(chunkX, chunkZ));
    }

    @Nullable
    public static BuildingPart getBuildingPart(int x, int z) {
        ChunkCoord cityCenter = City.getNearestCityCenter(x, z);
        if (cityCenter != null) {
            CityPlan plan = City.getRandomCityPlan(cityCenter);
            List<String> pattern = plan.getPlan();
            int dimX = pattern.get(0).length();
            int dimZ = pattern.size();
            int ox = (x + dimX/2) - cityCenter.getChunkX();
            int oz = (z + dimZ/2) - cityCenter.getChunkZ();
            if (ox >= 0 && ox < dimX && oz >= 0 && oz < dimZ) {
                Map<Character, List<String>> partPalette = plan.getPartPalette();
                char partChar = pattern.get(oz).charAt(ox);
                if (partChar != ' ') {
                    List<String> parts = partPalette.get(partChar);

                    Random random = new Random(x * 23567813L + z * 923568029L);
                    random.nextFloat();
                    random.nextFloat();

                    return AssetRegistries.PARTS.get(parts.get(random.nextInt(parts.size())));
                }
            }
        }
        return null;
    }
}
