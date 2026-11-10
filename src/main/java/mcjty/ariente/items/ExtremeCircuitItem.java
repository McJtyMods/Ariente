package mcjty.ariente.items;

import java.util.List;
import mcjty.ariente.blocks.ExtremeMachineTile;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

public class ExtremeCircuitItem extends Item  implements EarlyUseItem {

    public ExtremeCircuitItem() {
        super(new Properties().stacksTo(64).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.mayUseItemAt(context.getClickedPos(), context.getClickedFace(), stack)
                || !(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof ExtremeMachineTile machine)
                || !machine.acceptsExtremeCircuit()) {
            return InteractionResult.PASS;
        }
        if (!context.getLevel().isClientSide) {
            if (machine.installExtremeCircuit()) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                player.displayClientMessage(Component.translatable("message.ariente.extreme_circuit.installed"), true);
            } else {
                player.displayClientMessage(Component.translatable("message.ariente.extreme_circuit.already_installed"), true);
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("message.ariente.extreme_circuit.tooltip"));
        tooltip.add(Component.translatable("message.ariente.extreme_circuit.recovery"));
    }
}
