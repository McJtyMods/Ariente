package mcjty.ariente.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;
import mcjty.hologui.api.IColor;
import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.hologui.api.components.INumber;
import mcjty.hologui.gui.HoloGuiRenderTools;
import mcjty.hologui.gui.components.AbstractHoloComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;

public class CompactNumber extends AbstractHoloComponent<INumber> implements INumber {

    private IColor color = () -> 0xffffff;
    private BiFunction<Player, IHoloGuiEntity, Integer> getter = (player, holo) -> 0;
    private Function<Player, Integer> colorGetter = player -> color.getColor();

    public CompactNumber(double x, double y, double w, double h) {
        super(x, y, w, h);
    }

    @Override
    public INumber getter(BiFunction<Player, IHoloGuiEntity, Integer> getter) {
        this.getter = getter;
        return this;
    }

    @Override
    public INumber color(int color) {
        return color(() -> color);
    }

    @Override
    public INumber color(IColor color) {
        this.color = color;
        return this;
    }

    @Override
    public INumber colorGetter(Function<Player, Integer> getter) {
        colorGetter = getter;
        return this;
    }

    @Override
    public void render(PoseStack poses, MultiBufferSource buffer, Player player, IHoloGuiEntity holo, double cursorX, double cursorY) {
        HoloGuiRenderTools.renderFittedText(poses, buffer, x, y + .1,
                NumberFormatting.compact(getter.apply(player, holo)), colorGetter.apply(player), .7f, w);
    }

    @Override
    public void renderTooltip(PoseStack poses, MultiBufferSource buffer, Player player, IHoloGuiEntity holo, double cursorX, double cursorY) {
        int value = getter.apply(player, holo);
        if (value > -1000 && value < 1000) {
            return;
        }
        double left = Math.min(x, 4);
        poses.pushPose();
        poses.translate(0, 0, .012);
        HoloGuiRenderTools.renderControl(poses, buffer, left, y + h + .1, 4, .65, false);
        HoloGuiRenderTools.renderControlText(poses, buffer, left + .1, y + h + .2, 3.8, .45,
                String.format(Locale.ROOT, "%,d", value), colorGetter.apply(player), .45f);
        poses.popPose();
    }
}
