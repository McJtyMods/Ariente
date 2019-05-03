package mcjty.ariente.api;

import mcjty.ariente.varia.ChunkCoord;

public interface ICityAISystem {
    ICityAI getCityAI(ChunkCoord cityCenter);

    void saveSystem();
}
