package mcjty.ariente.items;

import mcjty.ariente.Ariente;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class FluxCapacitorItem extends Item {

    public FluxCapacitorItem() {
        super(new Properties().group(Ariente.setup.getTab()).maxStackSize(64));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(new StringTextComponent("The Flux Capacitor is a crafting"));
        tooltip.add(new StringTextComponent("ingredient for some of the"));
        tooltip.add(new StringTextComponent("Ariente machines"));
    }
}