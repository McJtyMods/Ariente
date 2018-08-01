package mcjty.ariente.ai;

import mcjty.ariente.blocks.aicore.AICoreTile;
import mcjty.ariente.blocks.defense.ForceFieldTile;
import mcjty.ariente.blocks.generators.NegariteGeneratorTile;
import mcjty.ariente.blocks.generators.PosiriteGeneratorTile;
import mcjty.ariente.cities.City;
import mcjty.ariente.cities.CityPlan;
import mcjty.ariente.cities.CityTools;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.power.PowerSenderSupport;
import mcjty.ariente.varia.ChunkCoord;
import mcjty.lib.varia.RedstoneMode;
import net.minecraft.item.ItemStack;
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
    private Set<BlockPos> negariteGenerators = new HashSet<>();
    private Set<BlockPos> posiriteGenerators = new HashSet<>();

    public CityAI(ChunkCoord center) {
        this.center = center;
    }

    // Return true if we potentially have to save the city system state
    public boolean tick(AICoreTile tile) {
        // We use the given AICoreTile parameter to make sure only one tick per city happens
        if (!initialized) {
            initialized = true;
            initialize(tile.getWorld());
            return true;
        } else {
            // If there are no more ai cores the city AI is dead
            if (aiCores.isEmpty()) {
                return false;
            }

            // Only tick for the first aicore
            if (!tile.getPos().equals(aiCores.iterator().next())) {
                return false;
            }

            handleAI(tile.getWorld());
            return true;
        }
    }

    private void handleAI(World world) {
        // Handle power
        for (BlockPos pos : negariteGenerators) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof NegariteGeneratorTile) {
                NegariteGeneratorTile generator = (NegariteGeneratorTile) te;
                generator.setInventorySlotContents(NegariteGeneratorTile.SLOT_NEGARITE_INPUT, new ItemStack(ModItems.negariteDust, 1));
                generator.markDirtyClient();
            }
        }
        for (BlockPos pos : posiriteGenerators) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof PosiriteGeneratorTile) {
                PosiriteGeneratorTile generator = (PosiriteGeneratorTile) te;
                generator.setInventorySlotContents(PosiriteGeneratorTile.SLOT_POSIRITE_INPUT, new ItemStack(ModItems.posiriteDust, 1));
                generator.markDirtyClient();
            }
        }
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
                                ForceFieldTile forcefield = (ForceFieldTile) te;
                                forcefield.setRSMode(RedstoneMode.REDSTONE_IGNORED);
                                forcefield.setScale(38);    // @todo, base on city size
                            } else if (te instanceof NegariteGeneratorTile) {
                                negariteGenerators.add(p);
                                NegariteGeneratorTile generator = (NegariteGeneratorTile) te;
                                PowerSenderSupport.fixNetworks(world, p);
                                generator.setRSMode(RedstoneMode.REDSTONE_IGNORED);
                            } else if (te instanceof PosiriteGeneratorTile) {
                                posiriteGenerators.add(p);
                                PosiriteGeneratorTile generator = (PosiriteGeneratorTile) te;
                                PowerSenderSupport.fixNetworks(world, p);
                                generator.setRSMode(RedstoneMode.REDSTONE_IGNORED);
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
