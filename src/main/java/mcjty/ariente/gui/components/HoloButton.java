package mcjty.ariente.gui.components;

import mcjty.ariente.Ariente;
import mcjty.ariente.entities.HoloGuiEntity;
import mcjty.ariente.gui.HoloGuiRenderTools;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class HoloButton extends AbstractHoloComponent {

    private static final ResourceLocation image = new ResourceLocation(Ariente.MODID, "textures/gui/guielements.png");

    private int normal_u;
    private int normal_v;
    private int hover_u;
    private int hover_v;

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
    }
}
