package mcjty.ariente.api.hologui.components;

import mcjty.ariente.api.hologui.IGuiComponent;
import mcjty.ariente.api.hologui.IHoloGuiEntity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface INumber extends IGuiComponent {
    INumber getter(BiFunction<EntityPlayer, IHoloGuiEntity, Integer> getter);

    INumber color(int color);

    INumber colorGetter(Function<EntityPlayer, Integer> getter);
}
