package mcjty.ariente.power;

import mcjty.ariente.cables.CableColor;
import mcjty.lib.varia.OrientationTools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;

public class PowerSenderSupport {

    private int cableId = -1;

    public int getCableId() {
        return cableId;
    }

    public void setCableId(int cableId) {
        this.cableId = cableId;
    }

    private boolean matchingColor(CableColor c1, CableColor c2) {
        if (c1 == c2) {
            return true;
        }
        if (c1 == CableColor.COMBINED || c2 == CableColor.COMBINED) {
            return true;
        }
        return false;
    }

    public void fillCableId(Level world, BlockPos pos, int id, CableColor color) {
        if (cableId == id) {
            return; // Already ok
        }
        int toReplace = cableId;
        setCableId(id);
        for (Direction facing : OrientationTools.DIRECTION_VALUES) {
            BlockPos p = pos.relative(facing);
            BlockEntity te = world.getBlockEntity(p);
            if (te instanceof IPowerBlob) {
                IPowerBlob blob = (IPowerBlob) te;
                CableColor cableColor = blob.getCableColor();
                if (matchingColor(color, cableColor) && blob.getCableId() == toReplace) {
                    blob.fillCableId(id);
                }
            }
        }
    }


    public static void fixNetworks(Level world, BlockPos pos) {
        PowerSystem powerSystem = PowerSystem.getPowerSystem(world);

        // Fix all networks in a 3x3x3 centered around this block
        // First invalidate all of them
        Set<Integer> toRemove = new HashSet<>();
        for (int dx = -1 ; dx <= 1 ; dx++) {
            for (int dy = -1 ; dy <= 1 ; dy++) {
                for (int dz = -1 ; dz <= 1 ; dz++) {
                    BlockPos p = pos.offset(dx, dy, dz);
                    BlockEntity te = world.getBlockEntity(p);
                    if (te instanceof IPowerBlob) {
                        IPowerBlob blob = (IPowerBlob) te;
                        int cableId = blob.getCableId();
                        if (cableId != -1) {
                            blob.fillCableId(-1);
                            toRemove.add(cableId);
                        }
                    }
                }
            }
        }

        // From the ids are about to remove we see which ones we can reuse for new networks
        for (int dx = -1 ; dx <= 1 ; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos p = pos.offset(dx, dy, dz);
                    BlockEntity te = world.getBlockEntity(p);
                    if (te instanceof IPowerBlob) {
                        IPowerBlob blob = (IPowerBlob) te;
                        int cableId = blob.getCableId();
                        if (cableId == -1) {
                            if (toRemove.isEmpty()) {
                                cableId = powerSystem.allocateId();
                            } else {
                                cableId = toRemove.iterator().next();
                                toRemove.remove(cableId);
                            }
                            blob.fillCableId(cableId);
                        }
                    }
                }
            }
        }

        // Remove all remaining ids
        for (Integer id : toRemove) {
            powerSystem.removeId(id);
        }

        powerSystem.save();
    }

}
