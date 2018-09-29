package mcjty.ariente.api.hologui.components;

import mcjty.ariente.api.hologui.IEvent;
import mcjty.ariente.api.hologui.IGuiComponent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.function.Function;

public interface IStackToggle extends IGuiComponent {
    IStackToggle itemStack(@Nonnull ItemStack stack);

    IStackToggle getter(Function<EntityPlayer, Boolean> getter);

    IStackToggle hitEvent(IEvent event);
}
