package mcjty.ariente.items;

import java.util.List;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Tools;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import static mcjty.lib.builder.TooltipBuilder.header;

public class CircuitItem extends Item implements ITooltipSettings {

    private final TooltipBuilder tooltipBuilder = new TooltipBuilder().info(header());

    public CircuitItem(boolean advanced) {
        super(new Properties().stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
        tooltipBuilder.makeTooltip(Tools.getId(this), stack, tooltip, flag);
    }
}
