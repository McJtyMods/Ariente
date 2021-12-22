package mcjty.ariente.items;

import mcjty.ariente.Ariente;
import mcjty.ariente.security.IKeyCardSlot;
import mcjty.ariente.setup.Registration;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static mcjty.lib.builder.TooltipBuilder.*;

public class KeyCardItem extends Item implements ITooltipSettings {

    private final TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(header(),
                    gold(stack -> getSecurityTags(stack).isEmpty()),
                    parameter("info", stack -> getDescription(stack) != null, KeyCardItem::getDescription),
                    repeatingParameter("tag", stack -> getSecurityTags(stack).stream()));

    public KeyCardItem() {
        super(new Properties().stacksTo(1).tab(Ariente.setup.getTab()));
    }

    public static boolean hasPlayerKeycard(Player player, String tag) {
        for (int i = 0 ; i < player.getInventory().getContainerSize() ; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == Registration.KEY_CARD.get()) {
                if (hasSecurityTag(stack, tag)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
        tooltipBuilder.makeTooltip(getRegistryName(), stack, tooltip, flag);
    }


    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        InteractionHand hand = context.getHand();
        Player player = context.getPlayer();
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof IKeyCardSlot) {
            ((IKeyCardSlot) te).acceptKeyCard(player.getItemInHand(hand));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.SUCCESS;
    }

    public static Set<String> getSecurityTags(ItemStack stack) {
        if (!stack.hasTag()) {
            return Collections.emptySet();
        }
        Set<String> tags = new HashSet<>();
        CompoundTag compound = stack.getTag();
        ListTag tagList = compound.getList("tags", Tag.TAG_STRING);
        for (int i = 0; i < tagList.size(); i++) {
            String tag = tagList.getString(i);
            tags.add(tag);
        }
        return tags;
    }

    public static boolean hasSecurityTag(ItemStack stack, String tag) {
        if (!stack.hasTag()) {
            return false;
        }
        CompoundTag compound = stack.getTag();
        ListTag tagList = compound.getList("tags", Tag.TAG_STRING);
        for (int i = 0; i < tagList.size(); i++) {
            String t = tagList.getString(i);
            if (tag.equals(t)) {
                return true;
            }
        }
        return false;
    }

    public static void addSecurityTag(ItemStack stack, String tag) {
        Set<String> tags = new HashSet<>(getSecurityTags(stack));
        tags.add(tag);
        ListTag tagList = new ListTag();
        for (String t : tags) {
            tagList.add(StringTag.valueOf(t));
        }
        stack.getOrCreateTag().put("tags", tagList);
    }

    @Nullable
    public static String getDescription(ItemStack stack) {
        if (!stack.hasTag()) {
            return null;
        }
        if (stack.getTag().contains("description")) {
            return stack.getTag().getString("description");
        }
        return null;
    }

    public static void setDescription(ItemStack stack, String description) {
        stack.getOrCreateTag().putString("description", description);
    }
}