package mcjty.ariente.items.modules;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.*;

import net.minecraft.item.Item.Properties;

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
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
        tooltipBuilder.makeTooltip(new ResourceLocation(Ariente.MODID, "armormodule"), stack, tooltip, flag);
    }

    public ArmorUpgradeType getType() {
        return type;
    }
}