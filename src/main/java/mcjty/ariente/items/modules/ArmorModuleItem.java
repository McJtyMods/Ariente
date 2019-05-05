package mcjty.ariente.items.modules;

import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.items.GenericItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ArmorModuleItem extends GenericItem {

    private final ArmorUpgradeType type;

    public ArmorModuleItem(String name, ArmorUpgradeType type) {
        super(name);
        this.type = type;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (type.getPowerUsage() > 0) {
            tooltip.add(TextFormatting.GRAY + "Power usage: " + TextFormatting.YELLOW + type.getPowerUsage());
        } else if (type.getPowerUsage() == -1) {
            tooltip.add(TextFormatting.GRAY + "Power: " + TextFormatting.YELLOW + "30% more efficient");
        }
    }

    public ArmorUpgradeType getType() {
        return type;
    }
}