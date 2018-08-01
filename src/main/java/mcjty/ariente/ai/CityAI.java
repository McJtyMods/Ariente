package mcjty.ariente.ai;

import mcjty.ariente.blocks.aicore.AICoreTile;
import mcjty.ariente.blocks.defense.ForceFieldTile;
import mcjty.ariente.cities.City;
import mcjty.ariente.cities.CityPlan;
import mcjty.ariente.cities.CityTools;
import mcjty.ariente.varia.ChunkCoord;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CityAI {

    private final ChunkCoord center;
    private boolean initialized = false;

    private Set<BlockPos> aiCores = new HashSet<>();
    private Set<BlockPos> forceFields = new HashSet<>();

    public CityAI(ChunkCoord center) {
        this.center = center;
    }

    public void tick(AICoreTile tile) {
        // We use the given AICoreTile parameter to make sure only one tick per city happens
        if (!initialized) {
            initialized = true;
            initialize(tile.getWorld());
        } else {
            // If there are no more ai cores the city AI is dead
            if (aiCores.isEmpty()) {
                return;
            }

            // Only tick for the first aicore
            if (!tile.getPos().equals(aiCores.iterator().next())) {
                return;
            }

            handleAI();
        }
    }

    private void handleAI() {

    }

    private void initialize(World world) {
        City city = CityTools.getCity(center);
        assert city != null;
        CityPlan plan = city.getPlan();
        List<String> pattern = plan.getPlan();
        int dimX = pattern.get(0).length();
        int dimZ = pattern.size();
        int cx = center.getChunkX();
        int cz = center.getChunkZ();

        for (int dx = cx - dimX / 2 - 1 ; dx <= cx + dimX / 2 + 1 ; dx++) {
            for (int dz = cz - dimZ / 2 - 1; dz <= cz + dimZ / 2 + 1; dz++) {
                int starty = CityTools.getLowestHeight(city, dx, dz);
                for (int x = dx * 16 ; x < dx * 16 + 16 ; x++) {
                    for (int z = dz * 16 ; z < dz * 16 + 16 ; z++) {
                        for (int y = starty ; y < starty + 100 ; y++) {
                            BlockPos p = new BlockPos(x, y, z);
                            TileEntity te = world.getTileEntity(p);
                            if (te instanceof AICoreTile) {
                                aiCores.add(p);
                            } else if (te instanceof ForceFieldTile) {
                                forceFields.add(p);
                            }
                        }
                    }
                }
            }
        }
    }

    public void readFromNBT(NBTTagCompound nbt) {
        initialized = nbt.getBoolean("initialized");
    }

    public void writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("initialized", initialized);
    }

}
