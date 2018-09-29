package mcjty.ariente.apiimp.hologui.components;

import mcjty.ariente.api.hologui.IHoloGuiEntity;
import mcjty.ariente.api.hologui.components.IText;
import mcjty.ariente.apiimp.hologui.HoloGuiRenderTools;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.Supplier;

public class HoloText extends AbstractHoloComponent implements IText {

    private Supplier<String> text;
    private int color;

    HoloText(double x, double y, double w, double h) {
        super(x, y, w, h);
    }

    @Override
    public HoloText text(String text) {
        this.text = () -> text;
        return this;
    }

    @Override
    public HoloText text(Supplier<String> text) {
        this.text = text;
        return this;
    }

    @Override
    public HoloText color(int color) {
        this.color = color;
        return this;
    }


    @Override
    public void render(EntityPlayer player, IHoloGuiEntity holo, double cursorX, double cursorY) {
        HoloGuiRenderTools.renderText(x, y, text.get(), color);
    }
}
