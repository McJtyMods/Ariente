package mcjty.ariente.cities;

import java.util.Random;

public class City {

    public static boolean isCityCenter(int chunkX, int chunkZ) {
        if (Math.abs(chunkX) < 32 && Math.abs(chunkZ) < 32) {
            return false;
        }

        boolean candidate = (chunkX % 16 == 0) && (chunkZ % 16 == 0);
        if (candidate) {
            Random random = new Random(chunkX * 776531419 + chunkZ * 198491317);
            random.nextFloat();
            random.nextFloat();
            return random.nextFloat() < .5f;
        }
        return false;
    }


}
