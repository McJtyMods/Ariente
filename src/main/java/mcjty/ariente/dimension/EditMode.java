package mcjty.ariente.dimension;

import mcjty.ariente.cities.BuildingPart;
import mcjty.ariente.cities.City;
import mcjty.ariente.cities.CityPlan;
import mcjty.ariente.cities.CityTools;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;

public class EditMode {

    public static boolean editMode = false;

    public static void copyBlock(City city, World world, IBlockState placedBlock, BuildingPart found, int relX, int relY, int relZ) {
        int cx;
        int cz;CityPlan plan = city.getPlan();
        List<String> pattern = plan.getPlan();
        int dimX = pattern.get(0).length();
        int dimZ = pattern.size();

        cx = city.getCenter().getChunkX();
        cz = city.getCenter().getChunkZ();

        ArienteChunkGenerator generator = (ArienteChunkGenerator)(((WorldServer) world).getChunkProvider().chunkGenerator);

        for (int dx = cx - dimX / 2 - 1 ; dx <= cx + dimX / 2 + 1 ; dx++) {
            for (int dz = cz - dimZ / 2 - 1 ; dz <= cz + dimZ / 2 + 1 ; dz++) {
                int y = CityTools.getLowestHeight(city, generator, dx, dz);
                List<BuildingPart> parts = CityTools.getBuildingParts(city, dx, dz);
                for (BuildingPart part : parts) {
                    if (part == found) {
                        world.setBlockState(new BlockPos(dx * 16 + relX, y + relY, dz * 16 + relZ), placedBlock, 3);
                    }
                    y += part.getSliceCount();
                }
            }
        }
    }

    public static void breakBlock(City city, World world, BuildingPart found, int relX, int relY, int relZ) {
        int cx;
        int cz;CityPlan plan = city.getPlan();
        List<String> pattern = plan.getPlan();
        int dimX = pattern.get(0).length();
        int dimZ = pattern.size();

        cx = city.getCenter().getChunkX();
        cz = city.getCenter().getChunkZ();

        ArienteChunkGenerator generator = (ArienteChunkGenerator)(((WorldServer) world).getChunkProvider().chunkGenerator);

        for (int dx = cx - dimX / 2 - 1 ; dx <= cx + dimX / 2 + 1 ; dx++) {
            for (int dz = cz - dimZ / 2 - 1 ; dz <= cz + dimZ / 2 + 1 ; dz++) {
                int y = CityTools.getLowestHeight(city, generator, dx, dz);
                List<BuildingPart> parts = CityTools.getBuildingParts(city, dx, dz);
                for (BuildingPart part : parts) {
                    if (part == found) {
                        world.setBlockToAir(new BlockPos(dx * 16 + relX, y + relY, dz * 16 + relZ));
                    }
                    y += part.getSliceCount();
                }
            }
        }
    }

}
