package mcjty.ariente.ai;

import mcjty.ariente.blocks.aicore.AICoreTile;
import mcjty.ariente.blocks.defense.ForceFieldTile;
import mcjty.ariente.blocks.generators.NegariteGeneratorTile;
import mcjty.ariente.blocks.generators.PosiriteGeneratorTile;
import mcjty.ariente.cities.City;
import mcjty.ariente.cities.CityPlan;
import mcjty.ariente.cities.CityTools;
import mcjty.ariente.entities.SentinelDroneEntity;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.power.PowerSenderSupport;
import mcjty.ariente.varia.ChunkCoord;
import mcjty.lib.varia.RedstoneMode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class CityAI {

    private final ChunkCoord center;
    private boolean initialized = false;

    private boolean foundEquipment = false;
    private Set<BlockPos> aiCores = new HashSet<>();
    private Set<BlockPos> forceFields = new HashSet<>();
    private Set<BlockPos> negariteGenerators = new HashSet<>();
    private Set<BlockPos> posiriteGenerators = new HashSet<>();

    private int[] sentinels = null;
    private int sentinelMovementTicks = 6;
    private int sentinelAngleOffset = 0;

    private int onAlert = 0;

    private static Random random = new Random();

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
            findEquipment(tile.getWorld());

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
                if (generator.getStackInSlot(NegariteGeneratorTile.SLOT_NEGARITE_INPUT).isEmpty()) {
                    generator.setInventorySlotContents(NegariteGeneratorTile.SLOT_NEGARITE_INPUT, new ItemStack(ModItems.negariteDust, 1));
                    generator.markDirtyClient();
                }
            }
        }
        for (BlockPos pos : posiriteGenerators) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof PosiriteGeneratorTile) {
                PosiriteGeneratorTile generator = (PosiriteGeneratorTile) te;
                if (generator.getStackInSlot(PosiriteGeneratorTile.SLOT_POSIRITE_INPUT).isEmpty()) {
                    generator.setInventorySlotContents(PosiriteGeneratorTile.SLOT_POSIRITE_INPUT, new ItemStack(ModItems.posiriteDust, 1));
                    generator.markDirtyClient();
                }
            }
        }

        // Sentinel movement
        sentinelMovementTicks--;
        if (sentinelMovementTicks <= 0) {
            sentinelMovementTicks = 6;
            sentinelAngleOffset++;
            if (sentinelAngleOffset >= 12) {
                sentinelAngleOffset = 0;
            }
        }

        // Small chance to revive sentinels if they are missing
        if (random.nextFloat() < .1f) {
            System.out.println("REVIVE EVENT");
            for (int i = 0; i < sentinels.length; i++) {
                if (sentinels[i] == 0 || world.getEntityByID(sentinels[i]) == null) {
                    System.out.println("    revive " + i);
                    createSentinel(world, i);
                }
            }
        }

        // Handle alert mode
        if (onAlert > 0) {
            onAlert--;
        }

        if (onAlert > 0) {
            // Turn on forcefields if present
            for (BlockPos pos : forceFields) {
                TileEntity te = world.getTileEntity(pos);
                if (te instanceof ForceFieldTile) {
                    ForceFieldTile forcefield = (ForceFieldTile) te;
                    if (forcefield.getRSMode() != RedstoneMode.REDSTONE_IGNORED) {
                        forcefield.setRSMode(RedstoneMode.REDSTONE_IGNORED);
                    }
                }
            }
        } else {
            // Turn off forcefields if present
            for (BlockPos pos : forceFields) {
                TileEntity te = world.getTileEntity(pos);
                if (te instanceof ForceFieldTile) {
                    ForceFieldTile forcefield = (ForceFieldTile) te;
                    if (forcefield.getRSMode() != RedstoneMode.REDSTONE_ONREQUIRED) {
                        forcefield.setRSMode(RedstoneMode.REDSTONE_ONREQUIRED);
                    }
                }
            }
        }
    }

    public BlockPos requestNewSentinelPosition(int sentinelId) {
        if (sentinels == null) {
            return null;
        }
        if (aiCores.isEmpty()) {
            return null;
        }
        int angleI = (sentinelAngleOffset + sentinelId * 12 / sentinels.length) % 12;
        int cx = center.getChunkX() * 16 + 8;
        int cy = aiCores.iterator().next().getY() + 20;     // Use the height of one of the ai cores as a base
        int cz = center.getChunkZ() * 16 + 8;

        float angle = angleI * 360.0f / 12;
        // @todo sentinel radius depends on city size
        float distance = 40;
        cx = (int) (cx + Math.cos(angle) * distance);
        cz = (int) (cz + Math.sin(angle) * distance);
        return new BlockPos(cx, cy, cz);
    }

    public void playerSpotted(EntityPlayer player) {
        onAlert = 100; //600;      // 5 minutes alert @todo configurable
        System.out.println("CityAI.playerSpotted");
    }

    private void findEquipment(World world) {
        if (foundEquipment) {
            return;
        }
        City city = CityTools.getCity(center);
        assert city != null;
        CityPlan plan = city.getPlan();
        List<String> pattern = plan.getPlan();
        int dimX = pattern.get(0).length();
        int dimZ = pattern.size();
        int cx = center.getChunkX();
        int cz = center.getChunkZ();

        for (int dx = cx - dimX / 2 - 1; dx <= cx + dimX / 2 + 1; dx++) {
            for (int dz = cz - dimZ / 2 - 1; dz <= cz + dimZ / 2 + 1; dz++) {
                int starty = 30;    // @todo is this a safe minimum height to assume?
                for (int x = dx * 16; x < dx * 16 + 16; x++) {
                    for (int z = dz * 16; z < dz * 16 + 16; z++) {
                        for (int y = starty; y < starty + 100; y++) {
                            BlockPos p = new BlockPos(x, y, z);
                            TileEntity te = world.getTileEntity(p);
                            if (te instanceof AICoreTile) {
                                aiCores.add(p);
                            } else if (te instanceof ForceFieldTile) {
                                forceFields.add(p);
                            } else if (te instanceof NegariteGeneratorTile) {
                                negariteGenerators.add(p);
                            } else if (te instanceof PosiriteGeneratorTile) {
                                posiriteGenerators.add(p);
                            }
                        }
                    }
                }
            }
        }
        foundEquipment = true;
    }

    private void initialize(World world) {
        findEquipment(world);
        initCityEquipment(world);
        initSentinels(world);
    }

    private void initCityEquipment(World world) {
        for (BlockPos p : forceFields) {
            TileEntity te = world.getTileEntity(p);
            if (te instanceof ForceFieldTile) {
                ForceFieldTile forcefield = (ForceFieldTile) te;
                forcefield.setRSMode(RedstoneMode.REDSTONE_IGNORED);
                forcefield.setScale(38);    // @todo, base on city size
            }
        }
        for (BlockPos p : negariteGenerators) {
            TileEntity te = world.getTileEntity(p);
            if (te instanceof NegariteGeneratorTile) {
                NegariteGeneratorTile generator = (NegariteGeneratorTile) te;
                PowerSenderSupport.fixNetworks(world, p);
                generator.setRSMode(RedstoneMode.REDSTONE_IGNORED);
            }
        }
        for (BlockPos p : posiriteGenerators) {
            TileEntity te = world.getTileEntity(p);
            if (te instanceof PosiriteGeneratorTile) {
                posiriteGenerators.add(p);
                PosiriteGeneratorTile generator = (PosiriteGeneratorTile) te;
                PowerSenderSupport.fixNetworks(world, p);
                generator.setRSMode(RedstoneMode.REDSTONE_IGNORED);
            }
        }
    }

    private void initSentinels(World world) {
        int numSentinels = 3;
        sentinels = new int[numSentinels];
        for (int i = 0 ; i < numSentinels ; i++) {
            createSentinel(world, i);
        }
    }

    private void createSentinel(World world, int i) {
        SentinelDroneEntity entity = new SentinelDroneEntity(world, i, center);
        int cx = center.getChunkX() * 16 + 8;
        int cy = aiCores.iterator().next().getY() + 50; // @todo make more consistent?
        int cz = center.getChunkZ() * 16 + 8;
        entity.setPosition(cx, cy, cz);
        world.spawnEntity(entity);
        sentinels[i] = entity.getEntityId();
    }

    public void readFromNBT(NBTTagCompound nbt) {
        initialized = nbt.getBoolean("initialized");
        if (nbt.hasKey("sentinels")) {
            sentinels = nbt.getIntArray("sentinels");
        } else {
            sentinels = null;
        }
        sentinelMovementTicks = nbt.getInteger("sentinelMovementTicks");
        sentinelAngleOffset = nbt.getInteger("sentinelAngleOffset");
        onAlert = nbt.getInteger("onAlert");
    }

    public void writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("initialized", initialized);
        if (sentinels != null) {
            compound.setIntArray("sentinels", sentinels);
        }
        compound.setInteger("sentinelMovementTicks", sentinelMovementTicks);
        compound.setInteger("sentinelAngleOffset", sentinelAngleOffset);
        compound.setInteger("onAlert", onAlert);
    }

}
