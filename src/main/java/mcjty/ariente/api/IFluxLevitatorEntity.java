package mcjty.ariente.api;

import net.minecraft.util.math.BlockPos;

public interface IFluxLevitatorEntity {

    BlockPos getPosition();

    void changeSpeed(int speed);

    BlockPos getDesiredDestination();

    void setDesiredDestination(BlockPos desiredDestination);
}
