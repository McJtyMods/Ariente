package mcjty.ariente.apiimp.hologui.components;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.hologui.IEvent;
import mcjty.ariente.api.hologui.IHoloGuiEntity;
import mcjty.ariente.api.hologui.components.IModeToggle;
import mcjty.ariente.apiimp.hologui.HoloGuiRenderTools;
import mcjty.ariente.apiimp.hologui.HoloGuiSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class HoloMode extends AbstractHoloComponent implements IModeToggle {

    private static final ResourceLocation image = new ResourceLocation(Ariente.MODID, "textures/gui/guielements.png");

    private List<Pair<Integer, Integer>> choices = new ArrayList<>();
    private int currentChoice = 0;
    private IEvent hitEvent;
    private Supplier<Integer> currentValue;

    HoloMode(double x, double y, double w, double h) {
        super(x, y, w, h);
    }

    @Override
    public HoloMode getter(Supplier<Integer> getter) {
        this.currentValue = getter;
        return this;
    }

    @Override
    public HoloMode hitEvent(IEvent event) {
        this.hitEvent = event;
        return this;
    }


    @Override
    public HoloMode choice(int u, int v) {
        choices.add(Pair.of(u, v));
        return this;
    }

    @Override
    public void render(EntityPlayer player, IHoloGuiEntity holo, double cursorX, double cursorY) {
        currentChoice = currentValue.get();
        int u = choices.get(currentChoice).getLeft();
        int v = choices.get(currentChoice).getRight();
        HoloGuiRenderTools.renderImage(x, y, u, v, 16, 16, 256, 256, image);
//        HoloGuiRenderTools.renderText(x, y, "x", 0xffffff);
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
    }

}
