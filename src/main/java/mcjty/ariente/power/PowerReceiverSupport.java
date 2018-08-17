package mcjty.ariente.power;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class PowerReceiverSupport {

    // @todo can this be done more optimal?
    public static boolean consumePower(World world, BlockPos pos, long amount) {
        PowerSystem powerSystem = PowerSystem.getPowerSystem(world);

        long totalNegarite = 0;
        long totalPosirite = 0;
        Set<Integer> handled = new HashSet<>();
        for (EnumFacing facing : EnumFacing.VALUES) {
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
                                totalNegarite += powerSystem.getTotalPower(id);
                                break;
                            case POSIRITE:
                                totalPosirite += powerSystem.getTotalPower(id);
                                break;
                            case COMBINED:
                                totalPosirite += powerSystem.getTotalPower(id);
                                totalNegarite += powerSystem.getTotalPower(id);
                                break;
                        }
                    }
                }
            }
        }
        if (amount > totalNegarite || amount > totalPosirite) {
            return false;
        }

        long amountNegarite = amount;
        long amountPosirite = amount;
        handled.clear();
        for (EnumFacing facing : EnumFacing.VALUES) {
            BlockPos p = pos.offset(facing);
            TileEntity te = world.getTileEntity(p);
            if (te instanceof IPowerBlob) {
                IPowerBlob blob = (IPowerBlob) te;
                int id = blob.getCableId();
                if (!handled.contains(id)) {
                    handled.add(id);
                    switch (blob.getCableColor()) {
                        case NEGARITE:
                            amountNegarite = powerSystem.consumePower(id, amountNegarite);
                            break;
                        case POSIRITE:
                            amountPosirite = powerSystem.consumePower(id, amountPosirite);
                            break;
                        case COMBINED:
                            amountPosirite = powerSystem.consumePower(id, amountPosirite);
                            amountNegarite = powerSystem.consumePower(id, amountNegarite);
                            break;
                    }
                    if (amountNegarite <= 0 && amountPosirite <= 0) {
                        break;
                    }
                }
            }
        }


        return true;
    }

}
