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

    public void addPower(int id, long power, PowerType type) {
        PowerBlob blob = getPowerBlob(id);
        PowerAmount powerAmount = blob.getPowerAmount(type);
        if (powerAmount.getLastUsedTick() == tickCounter) {
            // Update current power
            powerAmount.addAmount(power);
        } else if (powerAmount.getLastUsedTick() == tickCounter-1) {
            // We added power the previous tick
            powerAmount.setLastAmount(powerAmount.getAmount());
            powerAmount.setAmount(power);
            powerAmount.setLastUsedTick(tickCounter);
        } else {
            // The previous power is lost
            powerAmount.setLastAmount(0);
            powerAmount.setAmount(power);
            powerAmount.setLastUsedTick(tickCounter);
        }
    }

    public long getTotalPower(int id, PowerType type) {
        PowerBlob blob = getPowerBlob(id);
        PowerAmount powerAmount = blob.getPowerAmount(type);
        if (powerAmount.getLastUsedTick() == tickCounter) {
            return powerAmount.getLastAmount() + powerAmount.getAmount();
        } else if (powerAmount.getLastUsedTick() == tickCounter-1) {
            return powerAmount.getAmount();
        } else {
            return 0;
        }
    }

    // Return the amount of power that could not be consumed
    public long consumePower(int id, long amount, PowerType type) {
        if (amount == 0) {
            return 0;
        }
        PowerBlob blob = getPowerBlob(id);
        PowerAmount powerAmount = blob.getPowerAmount(type);
        long total = getTotalPower(id, type);
        long diff;
        if (amount > total) {
            diff = amount - total;
            amount = total;
        } else {
            diff = 0;
        }

        if (powerAmount.getLastUsedTick() == tickCounter) {
            if (amount <= powerAmount.getLastAmount()) {
                powerAmount.addLastAmount(-amount);
            } else {
                amount -= powerAmount.getLastAmount();
                powerAmount.setLastAmount(0);
                powerAmount.removeAmount(amount);
            }
        } else if (powerAmount.getLastUsedTick() == tickCounter-1) {
            powerAmount.removeAmount(amount);
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
