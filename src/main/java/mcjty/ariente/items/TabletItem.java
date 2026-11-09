package mcjty.ariente.items;

import java.util.ArrayList;

import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
import java.util.List;
import javax.annotation.Nonnull;
import mcjty.ariente.Ariente;
import mcjty.lib.compat.patchouli.PatchouliCompatibility;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class TabletItem extends Item {

    public static final Identifier MANUAL = Identifier.fromNamespaceAndPath(Ariente.MODID, "manual");

    public TabletItem(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    @Nonnull
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            PatchouliCompatibility.openBookGUI(serverPlayer, MANUAL);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> output, TooltipFlag flag) {
        List<Component> lines = new ArrayList<>();
        appendHoverText(stack, context, lines, flag);
        lines.forEach(output);
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext level, List<Component> tooltip, TooltipFlag flags) {
        tooltip.add(Component.translatable("item.ariente.tablet.tooltip"));
    }
}
