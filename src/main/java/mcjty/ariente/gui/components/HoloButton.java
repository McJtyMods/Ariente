package mcjty.ariente.gui.components;

import mcjty.ariente.Ariente;
import mcjty.ariente.ModSounds;
import mcjty.ariente.gui.HoloGuiEntity;
import mcjty.ariente.gui.HoloGuiRenderTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

public class HoloButton extends AbstractHoloComponent {

    private static final ResourceLocation image = new ResourceLocation(Ariente.MODID, "textures/gui/guielements.png");

    private int normal_u;
    private int normal_v;
    private int hover_u;
    private int hover_v;
    private IEvent hitEvent;
    private IEvent hitClientEvent;

    public HoloButton(double x, double y, double w, double h) {
        super(x, y, w, h);
    }

    public HoloButton image(int u, int v) {
        this.normal_u = u;
        this.normal_v = v;
        return this;
    }

    public HoloButton hover(int u, int v) {
        this.hover_u = u;
        this.hover_v = v;
        return this;
    }

    public HoloButton hitEvent(IEvent event) {
        this.hitEvent = event;
        return this;
    }

    public HoloButton hitClientEvent(IEvent event) {
        this.hitClientEvent = event;
        return this;
    }

    @Override
    public void render(double cursorX, double cursorY) {
        int u;
        int v;
        if (isInside(cursorX, cursorY)) {
            u = hover_u;
            v = hover_v;
        } else {
            u = normal_u;
            v = normal_v;
        }
        HoloGuiRenderTools.renderImage(x, y, u, v, 16, 16, 256, 256, image);
//        HoloGuiRenderTools.renderText(x, y, "x", 0xffffff);
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
