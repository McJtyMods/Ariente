package mcjty.ariente.api;

import net.minecraft.core.BlockPos;

public interface IAICoreTile {

    BlockPos getCorePos();

    void setCityName(String name);

    String getCityName();
}
