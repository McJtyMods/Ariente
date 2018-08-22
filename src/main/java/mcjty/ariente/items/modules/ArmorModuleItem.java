package mcjty.ariente.items.modules;

import mcjty.ariente.bindings.KeyBindings;
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
        tooltip.add("Upgrade module for");
        tooltip.add("the power armor");
        if (KeyBindings.configureArmor != null) {
            tooltip.add(TextFormatting.GRAY + "Configure: " + TextFormatting.WHITE + "key " + KeyBindings.configureArmor.getDisplayName());
        }
        tooltip.add(TextFormatting.BLUE + type.getDescription());
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