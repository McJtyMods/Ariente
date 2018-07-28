package mcjty.ariente.gui.components;

import mcjty.ariente.Ariente;
import mcjty.ariente.sounds.ModSounds;
import mcjty.ariente.gui.HoloGuiEntity;
import mcjty.ariente.gui.HoloGuiRenderTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class HoloMode extends AbstractHoloComponent {

    private static final ResourceLocation image = new ResourceLocation(Ariente.MODID, "textures/gui/guielements.png");

    private List<Pair<Integer, Integer>> choices = new ArrayList<>();
    private int currentChoice = 0;
    private IEvent hitEvent;
    private Supplier<Integer> currentValue;

    public HoloMode(double x, double y, double w, double h, Supplier<Integer> getter) {
        super(x, y, w, h);
        currentValue = getter;
    }

    public HoloMode hitEvent(IEvent event) {
        this.hitEvent = event;
        return this;
    }


    public HoloMode choice(int u, int v) {
        choices.add(Pair.of(u, v));
        return this;
    }

    @Override
    public void render(double cursorX, double cursorY) {
        currentChoice = currentValue.get();
        int u = choices.get(currentChoice).getLeft();
        int v = choices.get(currentChoice).getRight();
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
    }

}
