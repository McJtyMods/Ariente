package mcjty.ariente.api;

import net.minecraft.util.math.BlockPos;

public interface IAICoreTile {

    BlockPos getCorePos();

    void setCityName(String name);

    String getCityName();
}
