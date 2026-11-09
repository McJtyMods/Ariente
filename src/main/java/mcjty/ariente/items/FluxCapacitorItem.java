package mcjty.ariente.items;

import java.util.ArrayList;

import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
import java.util.List;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Tools;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import static mcjty.lib.builder.TooltipBuilder.header;

public class FluxCapacitorItem extends Item implements ITooltipSettings {

    private final TooltipBuilder tooltipBuilder = new TooltipBuilder().info(header());

    public FluxCapacitorItem(Item.Properties properties) {
        super(properties.stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> output, TooltipFlag flag) {
        List<Component> lines = new ArrayList<>();
        appendHoverText(stack, context, lines, flag);
        lines.forEach(output);
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flag) {

        tooltipBuilder.makeTooltip(Tools.getId(this), stack, tooltip, flag);
    }
}
