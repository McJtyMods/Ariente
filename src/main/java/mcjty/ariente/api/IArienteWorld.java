package mcjty.ariente.api;

import mcjty.lib.varia.ChunkCoord;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IArienteWorld {

    int getDimension();

    BlockPos getNearestTeleportationSpot(BlockPos pos);

    ChunkCoord getNearestCityCenter(ChunkCoord cityCenter);

    BlockPos getNearestDungeon(World world, BlockPos pos);

    double getSoldierCityKeyChance();

    ICityAISystem getCityAISystem(World world);
}
