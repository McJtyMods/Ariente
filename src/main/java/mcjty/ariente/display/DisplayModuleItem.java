package mcjty.ariente.display;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import mcjty.ariente.Ariente;
import mcjty.ariente.items.ItemData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class DisplayModuleItem extends Item {

    private final DisplayModuleType type;

    public DisplayModuleItem(DisplayModuleType type) {
        super(new Properties().stacksTo(1));
        this.type = type;
    }

    public DisplayModuleType type() {
        return type;
    }

    public static CompoundTag config(ItemStack stack) {
        if (stack.isEmpty()) {
            return new CompoundTag();
        }
        return ItemData.section(stack, "Display");
    }

    public static void editConfig(ItemStack stack, Consumer<CompoundTag> change) {
        CompoundTag config = config(stack);
        change.accept(config);
        ItemData.setSection(stack, "Display", config);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
            if (!context.getLevel().isClientSide) {
                BlockPos pos = context.getClickedPos();
                if (type.canLink(context.getLevel(), pos, context.getClickedFace())) {
                    CompoundTag link = new CompoundTag();
                    link.putString("Dimension", context.getLevel().dimension().location().toString());
                    link.putLong("Pos", pos.asLong());
                    link.putInt("Side", context.getClickedFace().get3DDataValue());
                    link.putString("Block", BuiltInRegistries.BLOCK.getKey(context.getLevel().getBlockState(pos).getBlock()).toString());
                    player.displayClientMessage(type.link(stack, link, context.getLevel().getBlockState(pos).getBlock().getName()), true);
                } else {
                    player.displayClientMessage(Component.literal("This module cannot link to that block."), true);
                }
            }
            return InteractionResult.SUCCESS;
        }
        open(player, context.getHand());
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                type.clearLinks(stack);
                player.displayClientMessage(Component.literal("Module link cleared."), true);
            } else {
                open(player, hand);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private void open(Player player, InteractionHand hand) {
        if (!player.level().isClientSide) {
            ItemStack stack = player.getItemInHand(hand);
            if (!ItemData.get(stack).hasUUID("ModuleId")) {
                ItemData.update(stack, data -> data.putUUID("ModuleId", UUID.randomUUID()));
            }
            player.containerMenu.broadcastChanges();
            Ariente.guiHandler.openHoloGui(player, ModuleSettings.guiId(hand, false), 1);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext level, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.literal("Right click: configure | Aim at display: install").withStyle(ChatFormatting.AQUA));
        lines.add(Component.literal("Sneak + right click: link | In air: clear link").withStyle(ChatFormatting.GRAY));
        type.appendLinkTooltip(stack, lines);
    }
}
