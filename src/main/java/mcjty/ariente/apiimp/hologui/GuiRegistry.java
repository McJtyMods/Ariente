package mcjty.ariente.apiimp.hologui;

import mcjty.ariente.api.hologui.IGuiComponent;
import mcjty.ariente.api.hologui.IGuiRegistry;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class GuiRegistry implements IGuiRegistry {

    private final Map<String, Function<EntityPlayer, IGuiComponent>> registry = new HashMap<>();

    @Override
    public void registerGui(String id, Function<EntityPlayer, IGuiComponent> factory) {
        registry.put(id, factory);
    }

    @Override
    @Nullable
    public IGuiComponent createGui(String id, EntityPlayer player) {
        return  registry.getOrDefault(id, (p) -> null).apply(player);
    }
}
