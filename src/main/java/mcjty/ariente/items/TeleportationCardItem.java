package mcjty.ariente.items;

import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.core.UUIDUtil;
import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.utility.WarperTile;
import mcjty.ariente.teleport.TeleportationCardGui;
import mcjty.ariente.teleport.WarperDestination;
import mcjty.ariente.teleport.WarperPreviews;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class TeleportationCardItem extends Item implements EarlyUseItem {

    public static final int MAX_DESTINATIONS = 64;

    public TeleportationCardItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ListTag destinations(ItemStack stack) {
        return ItemData.has(stack) ? ItemData.get(stack).getListOrEmpty("Destinations") : new ListTag();
    }

    public static int selectedIndex(ItemStack stack) {
        return ItemData.has(stack) && ItemData.get(stack).contains("Selected") ? ItemData.get(stack).getIntOr("Selected", 0) : -1;
    }

    @Nullable
    public static WarperDestination selected(ItemStack stack) {
        return destination(stack, selectedIndex(stack));
    }

    @Nullable
    public static WarperDestination destination(ItemStack stack, int index) {
        ListTag list = destinations(stack);
        return index >= 0 && index < Math.min(MAX_DESTINATIONS, list.size()) ? WarperDestination.load(list.getCompoundOrEmpty(index)) : null;
    }

    @Nullable
    public static UUID id(ItemStack stack) {
        int[] value = ItemData.get(stack).getIntArray("CardId").orElseGet(() -> new int[0]);
        return value.length == 4 ? UUIDUtil.uuidFromIntArray(value) : null;
    }

    public static void ensureId(ItemStack stack) {
        if (id(stack) == null) {
            ItemData.update(stack, data -> data.putIntArray("CardId", UUIDUtil.uuidToIntArray(UUID.randomUUID())));
        }
    }

    public static String destinationName(ItemStack stack, int index) {
        WarperDestination destination = destination(stack, index);
        if (destination == null) {
            return "";
        }
        String name = destinations(stack).getCompoundOrEmpty(index).getStringOr("Name", "");
        return name.isBlank() ? destination.coordinates() : name;
    }

    public static void refreshName(ItemStack stack, int index, WarperTile warper) {
        ListTag list = destinations(stack);
        list.getCompoundOrEmpty(index).putString("Name", warper.getWarperName());
        ItemData.update(stack, data -> data.put("Destinations", list));
    }

    public static void sync(Player player) {
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) {
            player.containerMenu.broadcastChanges();
        }
    }

    public static boolean encode(ItemStack stack, WarperTile warper) {
        ensureId(stack);
        WarperDestination destination = new WarperDestination(warper.getLevel().dimension(), warper.getBlockPos());
        ListTag list = destinations(stack);
        for (int i = 0; i < Math.min(list.size(), MAX_DESTINATIONS); i++) {
            if (destination.equals(destination(stack, i))) {
                refreshName(stack, i, warper);
                return true;
            }
        }
        if (list.size() >= MAX_DESTINATIONS) {
            return false;
        }
        CompoundTag entry = destination.save();
        entry.putString("Name", warper.getWarperName());
        list.add(entry);
        ItemData.update(stack, data -> data.put("Destinations", list));
        return true;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof WarperTile warper) {
            if (!context.getLevel().isClientSide()) {
                if (player.isShiftKeyDown()) {
                    boolean encoded = encode(stack, warper);
                    player.sendOverlayMessage(Component.translatable(encoded ? "message.ariente.card.encoded" : "message.ariente.card.full"));
                    sync(player);
                } else if (selected(stack) != null) {
                    if (!WarperPreviews.apply(player, stack, warper)) {
                        player.sendOverlayMessage(Component.translatable("message.ariente.card.invalid"));
                    }
                } else {
                    open(player, context.getHand());
                }
            }
        } else {
            open(player, context.getHand());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        open(player, hand);
        return InteractionResult.SUCCESS;
    }

    private static void open(Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = player.getItemInHand(hand);
            ensureId(stack);
            WarperPreviews.refreshLoadedNames(serverPlayer, stack);
            sync(player);
            Ariente.guiHandler.openHoloGui(player, TeleportationCardGui.guiId(hand), 1);
            if (selected(stack) != null) {
                WarperPreviews.select(serverPlayer, stack, selectedIndex(stack));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> output, TooltipFlag flag) {
        output.accept(Component.translatable("message.ariente.card.use").withStyle(ChatFormatting.AQUA));
        output.accept(Component.translatable("message.ariente.card.encode").withStyle(ChatFormatting.GRAY));
        output.accept(Component.translatable("message.ariente.card.apply").withStyle(ChatFormatting.GRAY));
        output.accept(Component.translatable("message.ariente.card.count", destinations(stack).size(), MAX_DESTINATIONS));
        if (selected(stack) != null) {
            output.accept(Component.translatable("message.ariente.card.selected", destinationName(stack, selectedIndex(stack))).withStyle(ChatFormatting.GREEN));
        }
    }
}
