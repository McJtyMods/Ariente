package mcjty.ariente.api;

import mcjty.lib.varia.DimensionId;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public interface IArienteWorld {

    DimensionId getDimension();

    BlockPos getNearestTeleportationSpot(BlockPos pos);

    ChunkPos getNearestCityCenter(ChunkPos cityCenter);

    BlockPos getNearestDungeon(World world, BlockPos pos);

    double getSoldierCityKeyChance();

    ICityAISystem getCityAISystem(World world);
}
