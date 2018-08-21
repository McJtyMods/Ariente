package mcjty.ariente.items.modules;

import mcjty.ariente.items.GenericItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ArmorModuleItem extends GenericItem {

    public ArmorModuleItem(String name) {
        super(name);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add("Upgrade module for");
        tooltip.add("the power armor. Press");
        tooltip.add("0 to configure armor");        // @todo fetch from keybindings
    }
}