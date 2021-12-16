package mcjty.ariente.api;

import net.minecraft.core.BlockPos;

public interface IFluxLevitatorEntity {

    void changeSpeed(int speed);

    BlockPos getDesiredDestination();

    void setDesiredDestination(BlockPos desiredDestination);
}
