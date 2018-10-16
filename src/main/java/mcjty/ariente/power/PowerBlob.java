package mcjty.ariente.power;

public class PowerBlob {

    private PowerAmount negariteAmount = new PowerAmount();
    private PowerAmount posiriteAmount = new PowerAmount();

    public PowerAmount getPowerAmount(PowerType type) {
        if (type.equals(PowerType.NEGARITE)) {
            return negariteAmount;
        } else {
            return posiriteAmount;
        }
    }
}
