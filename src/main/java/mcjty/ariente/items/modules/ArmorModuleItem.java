package mcjty.ariente.items.modules;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
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
            .info(gold(this::isEfficiencyModule),
                    parameter("info", stack -> !isEfficiencyModule(stack), this::getPowerUsageString));

    private String getPowerUsageString(ItemStack stack) {
        return Integer.toString(type.getPowerUsage());
    }

    private boolean isEfficiencyModule(ItemStack stack) {
        return type.getPowerUsage() == -1;
    }

    public ArmorModuleItem(ArmorUpgradeType type) {
        super(new Properties());
        this.type = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
        tooltip.add(Component.translatable("message.ariente.armormodule.effect." + type.getName()).withStyle(ChatFormatting.GRAY));
        tooltip.add(getInstallationDescription().withStyle(ChatFormatting.GRAY));
        tooltipBuilder.makeTooltip(new ResourceLocation(Ariente.MODID, "armormodule"), stack, tooltip, flag);
        tooltip.add(Component.translatable("message.ariente.armormodule.manual").withStyle(ChatFormatting.DARK_GRAY));
    }

    public MutableComponent getInstallationDescription() {
        String equipment = switch (type) {
            case ENERGY -> "armor_weapons";
            case ARMOR -> "armor";
            default -> type.supportsSlot(EquipmentSlot.MAINHAND) ? "weapons"
                    : type.getEquipmentSlots().iterator().next().getName();
        };
        return Component.translatable("message.ariente.armormodule.header",
                Component.translatable("message.ariente.armormodule.equipment." + equipment));
    }

    public ArmorUpgradeType getType() {
        return type;
    }
}
