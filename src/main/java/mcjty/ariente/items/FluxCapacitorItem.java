package mcjty.ariente.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class FluxCapacitorItem extends GenericItem {

    public FluxCapacitorItem() {
        super("flux_capacitor");
        this.maxStackSize = 64;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add("The Flux Capacitor is a crafting");
        tooltip.add("ingredient for some of the");
        tooltip.add("Ariente machines");
    }
}