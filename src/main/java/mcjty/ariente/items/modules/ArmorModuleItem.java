package mcjty.ariente.items.modules;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.*;

public class ArmorModuleItem extends Item implements ITooltipSettings {

    private final ArmorUpgradeType type;

    private final TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(header(),
                    gold(this::is30MoreEfficient),
                    parameter("info", stack -> !is30MoreEfficient(stack), this::getPowerUsageString));


    private String getPowerUsageString(ItemStack stack) {
        return Integer.toString(type.getPowerUsage());
    }
    private boolean is30MoreEfficient(ItemStack stack) {
        return type.getPowerUsage() == -1;
    }

    public ArmorModuleItem(ArmorUpgradeType type) {
        super(new Properties().tab(Ariente.setup.getTab()));
        this.type = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
        tooltipBuilder.makeTooltip(new ResourceLocation(Ariente.MODID, "armormodule"), stack, tooltip, flag);
    }

    public ArmorUpgradeType getType() {
        return type;
    }
}