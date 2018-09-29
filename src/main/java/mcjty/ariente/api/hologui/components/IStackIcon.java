package mcjty.ariente.api.hologui.components;

import mcjty.ariente.api.hologui.IGuiComponent;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface IStackIcon extends IGuiComponent {

    IStackIcon itemStack(@Nonnull ItemStack stack);
}
