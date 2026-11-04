package mcjty.ariente.entities.fluxship;

import net.minecraft.util.Mth;

/** Stored flux units: twenty minutes at full thrust, one minute on a powered landing pad. */
public class FluxShipEnergy {

    public static final int CAPACITY = 1_200_000;
    public static final int CHARGE_PER_TICK = CAPACITY / (60 * 20);
    public static final int FULL_THRUST_COST = CAPACITY / (20 * 60 * 20);
    public static final String CHARGE_TAG = "FluxShipCharge";

    public static int flightCost(double horizontalSpeed, double climbSpeed) {
        double thrust = Math.max(horizontalSpeed / 5.2, Math.max(0, climbSpeed) / .35);
        return 2 + (int) Math.round((FULL_THRUST_COST - 2) * Mth.clamp(thrust, 0, 1));
    }
}
