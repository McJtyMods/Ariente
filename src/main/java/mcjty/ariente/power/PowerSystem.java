package mcjty.ariente.power;

import mcjty.lib.worlddata.AbstractWorldData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class PowerSystem extends AbstractWorldData<PowerSystem> {

    private static final String NAME = "ArientePowerSystem";

    private int lastId = 0;
    private Map<Integer, PowerBlob> powerBlobs = new HashMap<>();

    public PowerSystem(String name) {
        super(name);
    }

    @Override
    public void clear() {
        powerBlobs.clear();
    }

    public void removeId(int id) {
        powerBlobs.remove(id);
    }

    public int allocateId() {
        lastId++;
        return lastId;
    }

    @Nonnull
    public static PowerSystem getPowerSystem(World world) {
        return getData(world, PowerSystem.class, NAME);
    }


    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        lastId = nbt.getInteger("lastId");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("lastId", lastId);
        return compound;
    }
}
