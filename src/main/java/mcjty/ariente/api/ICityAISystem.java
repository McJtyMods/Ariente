package mcjty.ariente.api;

import net.minecraft.world.level.ChunkPos;

public interface ICityAISystem {
    ICityAI getCityAI(ChunkPos cityCenter);

    void saveSystem();
}
