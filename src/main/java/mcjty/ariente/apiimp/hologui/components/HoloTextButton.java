package mcjty.ariente.apiimp.hologui.components;

import mcjty.ariente.api.hologui.IEvent;
import mcjty.ariente.api.hologui.IHoloGuiEntity;
import mcjty.ariente.api.hologui.components.IButton;
import mcjty.ariente.apiimp.hologui.HoloGuiRenderTools;
import mcjty.ariente.apiimp.hologui.HoloGuiSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;

public class HoloTextButton extends AbstractHoloComponent implements IButton {

    private IEvent hitEvent;
    private IEvent hitClientEvent;
    private int color;
    private int hoverColor;
    private String text;

    HoloTextButton(double x, double y, double w, double h) {
        super(x, y, w, h);
        this.color = 0x777777;
        this.hoverColor = 0xffffff;
    }

    @Override
    public HoloTextButton text(String text) {
        this.text = text;
        return this;
    }

    @Override
    public HoloTextButton color(int color) {
        this.color = color;
        return this;
    }

    public HoloTextButton hover(int hoverColor) {
        this.hoverColor = hoverColor;
        return this;
    }

    @Override
    public HoloTextButton hitEvent(IEvent event) {
        this.hitEvent = event;
        return this;
    }

    @Override
    public HoloTextButton hitClientEvent(IEvent event) {
        this.hitClientEvent = event;
        return this;
    }

    @Override
    public void render(EntityPlayer player, IHoloGuiEntity holo, double cursorX, double cursorY) {
        int color;
        if (isInside(cursorX, cursorY)) {
            color = this.hoverColor;
        } else {
            color = this.color;
        }
        HoloGuiRenderTools.renderText(x, y, text, color);
    }

    @Override
    public void hit(EntityPlayer player, IHoloGuiEntity entity, double cursorX, double cursorY) {
        if (hitEvent != null) {
            hitEvent.hit(this, player, entity, cursorX, cursorY);
        }
    }

    @Override
    public void hitClient(EntityPlayer player, IHoloGuiEntity entity, double cursorX, double cursorY) {
        Entity ent = entity.getEntity();
        player.world.playSound(ent.posX, ent.posY, ent.posZ, HoloGuiSounds.guiclick, SoundCategory.PLAYERS, 1.0f, 1.0f, true);
        if (hitClientEvent != null) {
            hitClientEvent.hit(this, player, entity, cursorX, cursorY);
        }
    }
}
