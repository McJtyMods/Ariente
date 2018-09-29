package mcjty.ariente.api.hologui;

import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nullable;
import java.util.function.Function;

public interface IGuiRegistry {

    void registerGui(String id, Function<EntityPlayer, IGuiComponent> factory);

    @Nullable
    IGuiComponent createGui(String id, EntityPlayer player);
}
