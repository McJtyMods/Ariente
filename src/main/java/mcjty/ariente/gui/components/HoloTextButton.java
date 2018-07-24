package mcjty.ariente.gui.components;

import mcjty.ariente.ModSounds;
import mcjty.ariente.gui.HoloGuiEntity;
import mcjty.ariente.gui.HoloGuiRenderTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;

public class HoloTextButton extends AbstractHoloComponent {

    private IEvent hitEvent;
    private IEvent hitClientEvent;
    private int color;
    private int hoverColor;
    private String text;

    public HoloTextButton(double x, double y, double w, double h, String text) {
        super(x, y, w, h);
        this.text = text;
        this.color = 0x777777;
        this.hoverColor = 0xffffff;
    }

    public HoloTextButton color(int color) {
        this.color = color;
        return this;
    }

    public HoloTextButton hover(int hoverColor) {
        this.hoverColor = hoverColor;
        return this;
    }

    public HoloTextButton hitEvent(IEvent event) {
        this.hitEvent = event;
        return this;
    }

    public HoloTextButton hitClientEvent(IEvent event) {
        this.hitClientEvent = event;
        return this;
    }

    @Override
    public void render(double cursorX, double cursorY) {
        int color;
        if (isInside(cursorX, cursorY)) {
            color = this.hoverColor;
        } else {
            color = this.color;
        }
        HoloGuiRenderTools.renderText(x, y, text, color);
    }

    @Override
    public void hit(EntityPlayer player, HoloGuiEntity entity, double cursorX, double cursorY) {
        if (hitEvent != null) {
            hitEvent.hit(this, player, entity, cursorX, cursorY);
        }
    }

    @Override
    public void hitClient(EntityPlayer player, HoloGuiEntity entity, double cursorX, double cursorY) {
        player.world.playSound(entity.posX, entity.posY, entity.posZ, ModSounds.guiclick, SoundCategory.PLAYERS, 1.0f, 1.0f, true);
        if (hitClientEvent != null) {
            hitClientEvent.hit(this, player, entity, cursorX, cursorY);
        }
    }
}
