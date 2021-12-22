package mcjty.ariente.api;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public interface IArienteWorld {

    ResourceKey<Level> getDimension();

    BlockPos getNearestTeleportationSpot(BlockPos pos);

    ChunkPos getNearestCityCenter(ChunkPos cityCenter);

    BlockPos getNearestDungeon(Level world, BlockPos pos);

    double getSoldierCityKeyChance();

    ICityAISystem getCityAISystem(Level world);
}
