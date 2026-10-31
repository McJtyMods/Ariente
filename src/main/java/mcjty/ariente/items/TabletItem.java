package mcjty.ariente.items;

import mcjty.ariente.Ariente;
import mcjty.ariente.setup.Registration;
import mcjty.lib.compat.patchouli.PatchouliCompatibility;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TabletItem extends Item {

    public static final ResourceLocation MANUAL = new ResourceLocation(Ariente.MODID, "manual");

    public TabletItem() {
        super(Registration.createStandardProperties().stacksTo(1));
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            PatchouliCompatibility.openBookGUI(serverPlayer, MANUAL);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        tooltip.add(Component.translatable("item.ariente.tablet.tooltip"));
    }
}
