package mcjty.ariente.items.modules;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.ArmorUpgradeType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ArmorModuleItem extends Item {

    private final ArmorUpgradeType type;

    public ArmorModuleItem(ArmorUpgradeType type) {
        super(new Properties().group(Ariente.setup.getTab()));
        this.type = type;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (type.getPowerUsage() > 0) {
            tooltip.add(new StringTextComponent(TextFormatting.GRAY + "Power usage: " + TextFormatting.YELLOW + type.getPowerUsage()));
        } else if (type.getPowerUsage() == -1) {
            tooltip.add(new StringTextComponent(TextFormatting.GRAY + "Power: " + TextFormatting.YELLOW + "30% more efficient"));
        }
    }

    public ArmorUpgradeType getType() {
        return type;
    }
}