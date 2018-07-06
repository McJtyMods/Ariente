package mcjty.ariente.cities;

import mcjty.ariente.dimension.ArienteChunkGenerator;
import mcjty.ariente.varia.ChunkCoord;

public class City {

    private final ChunkCoord center;
    private final CityPlan plan;
    private final int height;

    public City(ChunkCoord center, CityPlan plan, int height) {
        this.center = center;
        this.plan = plan;
        this.height = height;

    }

    public ChunkCoord getCenter() {
        return center;
    }

    public CityPlan getPlan() {
        return plan;
    }

    public int getHeight() {
        return height;
    }
}
