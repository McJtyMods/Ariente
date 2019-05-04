package mcjty.ariente.api;

import mcjty.lib.varia.ChunkCoord;

public interface ICityAISystem {
    ICityAI getCityAI(ChunkCoord cityCenter);

    void saveSystem();
}
