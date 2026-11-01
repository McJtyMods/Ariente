package mcjty.ariente.items.modules;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.ariente.setup.Registration;
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
            .info(gold(this::is30MoreEfficient),
                    parameter("info", stack -> !is30MoreEfficient(stack), this::getPowerUsageString));


    private String getPowerUsageString(ItemStack stack) {
        return Integer.toString(type.getPowerUsage());
    }

    private boolean is30MoreEfficient(ItemStack stack) {
        return type.getPowerUsage() == -1;
    }

    public ArmorModuleItem(ArmorUpgradeType type) {
        super(new Properties());
        this.type = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
        tooltip.add(getInstallationDescription().withStyle(ChatFormatting.GRAY));
        tooltipBuilder.makeTooltip(new ResourceLocation(Ariente.MODID, "armormodule"), stack, tooltip, flag);
    }

    public MutableComponent getInstallationDescription() {
        MutableComponent equipment = Component.empty();
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND}) {
            if (!type.supportsSlot(slot)) {
                continue;
            }
            if (!equipment.getSiblings().isEmpty()) {
                equipment.append(", ");
            }
            if (slot == EquipmentSlot.MAINHAND) {
                equipment.append(Registration.ENHANCED_ENERGY_SABRE.get().getDescription())
                        .append(", ").append(Registration.ENERGY_LANCE.get().getDescription());
            } else {
                Item item = switch (slot) {
                    case HEAD -> Registration.POWERSUIT_HEAD.get();
                    case CHEST -> Registration.POWERSUIT_CHEST.get();
                    case LEGS -> Registration.POWERSUIT_LEGS.get();
                    case FEET -> Registration.POWERSUIT_FEET.get();
                    default -> throw new IllegalArgumentException("Not an armor slot: " + slot);
                };
                equipment.append(item.getDescription());
            }
        }
        return Component.translatable("message.ariente.armormodule.header", equipment,
                Component.translatable("message.ariente.armormodule.effect." + type.getName()));
    }

    public ArmorUpgradeType getType() {
        return type;
    }
}
