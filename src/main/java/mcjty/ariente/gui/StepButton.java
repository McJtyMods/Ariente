package mcjty.ariente.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.IntConsumer;
import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.hologui.gui.HoloGuiRenderTools;
import mcjty.hologui.gui.HoloGuiSounds;
import mcjty.hologui.gui.components.AbstractHoloComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** A compact, framed single or double arrow for an exact numeric adjustment. */
public class StepButton extends AbstractHoloComponent<StepButton> {

    private final int step;
    private final IntConsumer change;

    public StepButton(double x, double y, double w, double h, int step, IntConsumer change) {
        super(x, y, w, h);
        this.step = step;
        this.change = change;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(PoseStack poses, MultiBufferSource buffer, Player player, IHoloGuiEntity holo, double cursorX, double cursorY) {
        boolean hovered = isInside(cursorX, cursorY);
        HoloGuiRenderTools.renderControl(poses, buffer, x, y, w, h, hovered);
        int color = hovered ? 0xffffff : 0xc1f5ff;
        double centerX = x + w / 2;
        double centerY = y + h / 2;
        if (Math.abs(step) > 1) {
            HoloGuiRenderTools.renderChevron(poses, buffer, centerX - .1, centerY, step > 0, color);
            HoloGuiRenderTools.renderChevron(poses, buffer, centerX + .1, centerY, step > 0, color);
        } else {
            HoloGuiRenderTools.renderChevron(poses, buffer, centerX, centerY, step > 0, color);
        }
    }

    @Override
    public void hit(Player player, IHoloGuiEntity entity, double cursorX, double cursorY) {
        change.accept(step);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void hitClient(Player player, IHoloGuiEntity entity, double cursorX, double cursorY) {
        Entity owner = entity.getEntity();
        player.level().playSound(player, owner.getX(), owner.getY(), owner.getZ(), HoloGuiSounds.GUICLICK.get(), SoundSource.PLAYERS, 1, 1);
    }
}
