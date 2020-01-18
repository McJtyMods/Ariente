package mcjty.ariente.power;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class PowerReceiverSupport {

    // @todo can this be done more optimal?

    /**
     * Check if there is enough power and consume that power if that's the case
     * @return true if there was enough power and exactly the given amount of power is consumed from the network
     */
    public static boolean consumePower(World world, BlockPos pos, long amount, boolean doCombined) {
        long powerAvailable = getPowerAvailable(world, pos, doCombined);
        if (amount > powerAvailable) {
            return false;
        }
        consumerPowerNoCheck(world, pos, amount, doCombined);
        return true;
    }

    /**
     * Consume the given amount of power without checking if that power is actually available. Use this method
     * with care (and call getPowerAvailable first!)
     */
    public static void consumerPowerNoCheck(World world, BlockPos pos, long amount, boolean doCombined) {
        PowerSystem powerSystem = PowerSystem.getPowerSystem(world);
        long amountNegarite = amount;
        long amountPosirite = amount;
        Set<Integer> handled = new HashSet<>();
        for (Direction facing : Direction.VALUES) {
            BlockPos p = pos.offset(facing);
            TileEntity te = world.getTileEntity(p);
            if (te instanceof IPowerBlob) {
                IPowerBlob blob = (IPowerBlob) te;
                int id = blob.getCableId();
                if (!handled.contains(id)) {
                    handled.add(id);
                    switch (blob.getCableColor()) {
                        case NEGARITE:
                            amountNegarite = powerSystem.consumePower(id, amountNegarite, PowerType.NEGARITE);
                            break;
                        case POSIRITE:
                            amountPosirite = powerSystem.consumePower(id, amountPosirite, PowerType.POSIRITE);
                            break;
                        case COMBINED:
                            if (doCombined) {
                                amountPosirite = powerSystem.consumePower(id, amountPosirite, PowerType.POSIRITE);
                                amountNegarite = powerSystem.consumePower(id, amountNegarite, PowerType.NEGARITE);
                            }
                            break;
                    }
                    if (amountNegarite <= 0 && amountPosirite <= 0) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Check how much power is available without c onsuming it
     */
    public static long getPowerAvailable(World world, BlockPos pos, boolean doCombined) {
        PowerSystem powerSystem = PowerSystem.getPowerSystem(world);
        Set<Integer> handled = new HashSet<>();

        long totalNegarite = 0;
        long totalPosirite = 0;
        for (Direction facing : Direction.VALUES) {
            BlockPos p = pos.offset(facing);
            TileEntity te = world.getTileEntity(p);
            if (te instanceof IPowerBlob) {
                IPowerBlob blob = (IPowerBlob) te;
                if (blob.canSendPower()) {
                    int id = blob.getCableId();
                    if (!handled.contains(id)) {
                        handled.add(id);
                        switch (blob.getCableColor()) {
                            case NEGARITE:
                                totalNegarite += powerSystem.getTotalPower(id, PowerType.NEGARITE);
                                break;
                            case POSIRITE:
                                totalPosirite += powerSystem.getTotalPower(id, PowerType.POSIRITE);
                                break;
                            case COMBINED:
                                if (doCombined) {
                                    totalPosirite += powerSystem.getTotalPower(id, PowerType.POSIRITE);
                                    totalNegarite += powerSystem.getTotalPower(id, PowerType.NEGARITE);
                                }
                                break;
                        }
                    }
                }
            }
        }
        return Math.min(totalNegarite, totalPosirite);
    }

}
