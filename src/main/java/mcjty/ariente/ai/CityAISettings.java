package mcjty.ariente.ai;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Randomized settings specific to a single city
 */
public class CityAISettings {

    private int numSentinels;

    public int getNumSentinels() {
        return numSentinels;
    }

    public void setNumSentinels(int numSentinels) {
        this.numSentinels = numSentinels;
    }

    public void readFromNBT(NBTTagCompound compound) {
        numSentinels = compound.getInteger("sentinels");
    }

    public void writeToNBT(NBTTagCompound compound) {
        compound.setInteger("sentinels", numSentinels);
    }

}
