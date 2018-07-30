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
    private int tickCounter = 1;

    public PowerSystem(String name) {
        super(name);
    }

    public void tick() {
        tickCounter++;
    }

    public int getTickCounter() {
        return tickCounter;
    }

    @Override
    public void clear() {
        powerBlobs.clear();
        tickCounter = 0;
    }

    public void removeId(int id) {
        powerBlobs.remove(id);
    }

    public int allocateId() {
        lastId++;
        return lastId;
    }

    public PowerBlob getPowerBlob(int id) {
        if (!powerBlobs.containsKey(id)) {
            powerBlobs.put(id, new PowerBlob());
        }
        return powerBlobs.get(id);
    }

    public void addPower(int id, long power) {
        PowerBlob blob = getPowerBlob(id);
        if (blob.getLastUsedTick() == tickCounter) {
            // Update current power
            blob.addAmount(power);
        } else if (blob.getLastUsedTick() == tickCounter-1) {
            // We added power the previous tick
            blob.setLastAmount(blob.getAmount());
            blob.setAmount(power);
            blob.setLastUsedTick(tickCounter);
        } else {
            // The previous power is lost
            blob.setLastAmount(0);
            blob.setAmount(power);
            blob.setLastUsedTick(tickCounter);
        }
    }

    public long getTotalPower(int id) {
        PowerBlob blob = getPowerBlob(id);
        if (blob.getLastUsedTick() == tickCounter) {
            return blob.getLastAmount() + blob.getAmount();
        } else if (blob.getLastUsedTick() == tickCounter-1) {
            return blob.getAmount();
        } else {
            return 0;
        }
    }

    // Return the amount of power that could not be consumed
    public long consumePower(int id, long amount) {
        if (amount == 0) {
            return 0;
        }
        PowerBlob blob = getPowerBlob(id);
        long total = getTotalPower(id);
        long diff;
        if (amount > total) {
            diff = amount - total;
            amount = total;
        } else {
            diff = 0;
        }

        if (blob.getLastUsedTick() == tickCounter) {
            if (amount <= blob.getLastAmount()) {
                blob.addLastAmount(-amount);
            } else {
                amount -= blob.getLastAmount();
                blob.setLastAmount(0);
                blob.removeAmount(amount);
            }
        } else if (blob.getLastUsedTick() == tickCounter-1) {
            blob.removeAmount(amount);
        }
        return diff;
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
