package mcjty.ariente.api;

import net.minecraft.world.level.ChunkPos;

public interface ISoldier extends IArienteMob {

    float getMinimumSabreDamage();

    default boolean isReinforcementFor(ChunkPos city) {
        return false;
    }
}
