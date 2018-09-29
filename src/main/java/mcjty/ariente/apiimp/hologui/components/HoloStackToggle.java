package mcjty.ariente.apiimp.hologui.components;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.hologui.IEvent;
import mcjty.ariente.api.hologui.IHoloGuiEntity;
import mcjty.ariente.api.hologui.components.IStackToggle;
import mcjty.ariente.apiimp.hologui.HoloGuiRenderTools;
import mcjty.ariente.apiimp.hologui.HoloGuiSounds;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class HoloStackToggle extends AbstractHoloComponent implements IStackToggle {

    private ItemStack stack;
    private Function<EntityPlayer, Boolean> currentValue;
    private IEvent hitEvent;

    private static final ResourceLocation DARKEN = new ResourceLocation(Ariente.MODID, "textures/gui/darken.png");
    private static final ResourceLocation INVALID = new ResourceLocation(Ariente.MODID, "textures/gui/darken_red.png");

    HoloStackToggle(double x, double y, double w, double h) {
        super(x, y, w, h);
    }

    @Override
    public HoloStackToggle itemStack(@Nonnull ItemStack stack) {
        this.stack = stack;
        return this;
    }

    @Override
    public HoloStackToggle getter(Function<EntityPlayer, Boolean> getter) {
        this.currentValue = getter;
        return this;
    }

    @Override
    public void render(EntityPlayer player, IHoloGuiEntity holo, double cursorX, double cursorY) {
        Boolean state = currentValue.apply(player);
        ResourceLocation lightmap = null;
        boolean border = false;
        if (state == null) {
            lightmap = DARKEN;
        } else if (!state) {
        } else {
            border = true;
        }
        HoloGuiRenderTools.renderItem(x * 1.05, y * 0.85 + .45, stack, lightmap, border);
    }

    @Override
    public void renderTooltip(EntityPlayer player, IHoloGuiEntity holo, double cursorX, double cursorY) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.01, 0.01, 0.01);
        GlStateManager.rotate(180, 0, 1, 0);
        GlStateManager.rotate(180, 0, 0, 1);
        GlStateManager.translate(0, 0, -10);
        GlStateManager.scale(0.4, 0.4, 0.0);
        HoloGuiRenderTools.renderToolTip(stack, (int) (x * 30 - 120), (int) (y * 30 - 120));
        GlStateManager.popMatrix();
    }

    @Override
    public HoloStackToggle hitEvent(IEvent event) {
        this.hitEvent = event;
        return this;
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
