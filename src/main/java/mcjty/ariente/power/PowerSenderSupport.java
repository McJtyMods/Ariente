package mcjty.ariente.power;

import mcjty.ariente.cables.CableColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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

    public void fillCableId(World world, BlockPos pos, int id, CableColor color) {
        if (cableId == id) {
            return; // Already ok
        }
        int toReplace = cableId;
        setCableId(id);
        for (EnumFacing facing : EnumFacing.VALUES) {
            BlockPos p = pos.offset(facing);
            TileEntity te = world.getTileEntity(p);
            if (te instanceof IPowerBlob) {
                IPowerBlob blob = (IPowerBlob) te;
                CableColor cableColor = blob.getCableColor();
                if (matchingColor(color, cableColor) && blob.getCableId() == toReplace) {
                    blob.fillCableId(id);
                }
            }
        }
    }


    public static void fixNetworks(World world, BlockPos pos) {
        PowerSystem powerSystem = PowerSystem.getPowerSystem(world);

        // Fix all networks in a 3x3x3 centered around this block
        // First invalidate all of them
        Set<Integer> toRemove = new HashSet<>();
        for (int dx = -1 ; dx <= 1 ; dx++) {
            for (int dy = -1 ; dy <= 1 ; dy++) {
                for (int dz = -1 ; dz <= 1 ; dz++) {
                    BlockPos p = pos.add(dx, dy, dz);
                    TileEntity te = world.getTileEntity(p);
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
                    BlockPos p = pos.add(dx, dy, dz);
                    TileEntity te = world.getTileEntity(p);
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
