package mcjty.ariente.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.hologui.gui.HoloGuiRenderTools;
import mcjty.hologui.gui.components.AbstractHoloComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;

import java.util.function.Supplier;

/** A dynamic label that keeps long player-supplied names inside its bounds. */
public class FittedText extends AbstractHoloComponent<FittedText> {

    private final Supplier<String> text;
    private final float scale;

    public FittedText(double x, double y, double w, double h, Supplier<String> text, float scale) {
        super(x, y, w, h);
        this.text = text;
        this.scale = scale;
    }

    @Override
    public void render(PoseStack poses, MultiBufferSource buffer, Player player, IHoloGuiEntity holo, double cursorX, double cursorY) {
        HoloGuiRenderTools.renderControlText(poses, buffer, x, y, w, h, text.get(), 0xffffff, scale);
    }
}
