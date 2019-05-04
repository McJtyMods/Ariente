package mcjty.ariente.api;

import net.minecraft.util.math.BlockPos;

public interface IFluxLevitatorEntity {

    BlockPos getPosition();

    BlockPos getDesiredDestination();

    void setDesiredDestination(BlockPos desiredDestination);
}
