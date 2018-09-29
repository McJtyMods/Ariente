package mcjty.ariente.apiimp.hologui.components;

import mcjty.ariente.api.hologui.IHoloGuiEntity;
import mcjty.ariente.api.hologui.components.INumber;
import mcjty.ariente.apiimp.hologui.HoloGuiRenderTools;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.BiFunction;
import java.util.function.Function;

public class HoloNumber extends AbstractHoloComponent implements INumber {

    private int color;
    private BiFunction<EntityPlayer, IHoloGuiEntity, Integer> getter;
    private Function<EntityPlayer, Integer> colorGetter = player -> getColor();

    HoloNumber(double x, double y, double w, double h) {
        super(x, y, w, h);
    }

    @Override
    public HoloNumber getter(BiFunction<EntityPlayer, IHoloGuiEntity, Integer> getter) {
        this.getter = getter;
        return this;
    }

    @Override
    public HoloNumber color(int color) {
        this.color = color;
        return this;
    }

    @Override
    public HoloNumber colorGetter(Function<EntityPlayer, Integer> getter) {
        colorGetter = getter;
        return this;
    }

    public int getColor() {
        return color;
    }

    @Override
    public void render(EntityPlayer player, IHoloGuiEntity holo, double cursorX, double cursorY) {
        String text = Integer.toString(getter.apply(player, holo));
        HoloGuiRenderTools.renderText(x, y+.1, text, colorGetter.apply(player));
    }
}
