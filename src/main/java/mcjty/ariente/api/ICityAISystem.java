package mcjty.ariente.api;

import net.minecraft.util.math.ChunkPos;

public interface ICityAISystem {
    ICityAI getCityAI(ChunkPos cityCenter);

    void saveSystem();
}
