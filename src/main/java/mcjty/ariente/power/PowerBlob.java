package mcjty.ariente.power;

import mcjty.ariente.cables.CableColor;

public class PowerBlob {

    private PowerAmount negariteAmount = new PowerAmount();
    private PowerAmount posiriteAmount = new PowerAmount();

    public PowerAmount getPowerAmount(CableColor color) {
        if (color.equals(CableColor.NEGARITE)) {
            return negariteAmount;
        } else {
            return posiriteAmount;
        }
    }

    public void setLastUsedTick(int lastUsedTick) {
        negariteAmount.setLastUsedTick(lastUsedTick);
        posiriteAmount.setLastUsedTick(lastUsedTick);
    }
}
