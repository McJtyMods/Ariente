package mcjty.ariente.items;

import mcjty.ariente.Ariente;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.security.IKeyCardSlot;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KeyCardItem extends Item {

    public KeyCardItem() {
        super(new Properties().maxStackSize(1).group(Ariente.setup.getTab()));
    }

    public static boolean hasPlayerKeycard(PlayerEntity player, String tag) {
        for (int i = 0 ; i < player.inventory.getSizeInventory() ; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == Registration.KEY_CARD.get()) {
                if (hasSecurityTag(stack, tag)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(new StringTextComponent("Security card"));
        Set<String> tags = getSecurityTags(stack);
        if (tags.isEmpty()) {
            tooltip.add(new StringTextComponent(TextFormatting.YELLOW + "Security card is empty!"));
        } else {
            tooltip.add(new StringTextComponent(TextFormatting.YELLOW + "Tags:"));
            for (String tag : tags) {
                tooltip.add(new StringTextComponent("   " + TextFormatting.GREEN + tag));
            }
        }
        String description = getDescription(stack);
        if (description != null) {
            tooltip.add(new StringTextComponent(TextFormatting.BLUE + description));
        }
    }


    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        Hand hand = context.getHand();
        PlayerEntity player = context.getPlayer();
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IKeyCardSlot) {
            ((IKeyCardSlot) te).acceptKeyCard(player.getHeldItem(hand));
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        return ActionResultType.SUCCESS;
    }

    public static Set<String> getSecurityTags(ItemStack stack) {
        if (!stack.hasTag()) {
            return Collections.emptySet();
        }
        Set<String> tags = new HashSet<>();
        CompoundNBT compound = stack.getTag();
        ListNBT tagList = compound.getList("tags", Constants.NBT.TAG_STRING);
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
        CompoundNBT compound = stack.getTag();
        ListNBT tagList = compound.getList("tags", Constants.NBT.TAG_STRING);
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
        ListNBT tagList = new ListNBT();
        for (String t : tags) {
            tagList.add(StringNBT.valueOf(t));
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