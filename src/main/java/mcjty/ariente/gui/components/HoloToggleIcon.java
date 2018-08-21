package mcjty.ariente.gui.components;

import mcjty.ariente.Ariente;
import mcjty.ariente.gui.HoloGuiEntity;
import mcjty.ariente.gui.HoloGuiRenderTools;
import mcjty.ariente.sounds.ModSounds;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

import java.util.function.Function;

public class HoloToggleIcon extends AbstractHoloComponent {

    private static final ResourceLocation image = new ResourceLocation(Ariente.MODID, "textures/gui/guielements.png");

    private int normal_u;
    private int normal_v;
    private int selected_u;
    private int selected_v;
    private Function<EntityPlayer, Boolean> currentValue;
    private IEvent hitEvent;

    public HoloToggleIcon(double x, double y, double w, double h, Function<EntityPlayer, Boolean> getter) {
        super(x, y, w, h);
        this.currentValue = getter;
    }

    public HoloToggleIcon image(int u, int v) {
        this.normal_u = u;
        this.normal_v = v;
        return this;
    }

    public HoloToggleIcon selected(int u, int v) {
        this.selected_u = u;
        this.selected_v = v;
        return this;
    }

    public HoloToggleIcon hitEvent(IEvent event) {
        this.hitEvent = event;
        return this;
    }


    @Override
    public void render(EntityPlayer player, double cursorX, double cursorY) {
        int u = normal_u;
        int v = normal_v;
        if (currentValue.apply(player)) {
            u = selected_u;
            v = selected_v;
        }
        GlStateManager.color(1, 1, 1, 1);
        HoloGuiRenderTools.renderImage(x, y, u, v, 16, 16, 256, 256, image);
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
    }

}
