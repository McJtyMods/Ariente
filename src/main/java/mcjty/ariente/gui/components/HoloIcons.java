package mcjty.ariente.gui.components;

import mcjty.ariente.Ariente;
import mcjty.ariente.gui.HoloGuiEntity;
import mcjty.ariente.gui.HoloGuiRenderTools;
import mcjty.ariente.sounds.ModSounds;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class HoloIcons extends AbstractHoloComponent {

    private static final ResourceLocation image = new ResourceLocation(Ariente.MODID, "textures/gui/guielements.png");

    private List<Pair<Integer, Integer>> icons = new ArrayList<>();
    private Function<EntityPlayer, Integer> currentValue;
    private IEvent hitEvent;

    public HoloIcons(double x, double y, double w, double h, Function<EntityPlayer, Integer> getter) {
        super(x, y, w, h);
        this.currentValue = getter;
    }

    public HoloIcons icon(int u, int v) {
        icons.add(Pair.of(u, v));
        return this;
    }

    public HoloIcons hitEvent(IEvent event) {
        this.hitEvent = event;
        return this;
    }

    @Override
    public void render(EntityPlayer player, double cursorX, double cursorY) {
        Integer value = currentValue.apply(player);
        if (value < 0 || value >= icons.size()) {
            return;
        }
        int u = icons.get(value).getLeft();
        int v = icons.get(value).getRight();
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
