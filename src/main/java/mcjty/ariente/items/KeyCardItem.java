package mcjty.ariente.items;

import mcjty.ariente.security.IKeyCardSlot;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KeyCardItem extends GenericItem {

    public KeyCardItem() {
        super("key_card");
        this.maxStackSize = 1;
    }

    public static boolean hasPlayerKeycard(EntityPlayer player, String tag) {
        for (int i = 0 ; i < player.inventory.getSizeInventory() ; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == ModItems.keyCardItem) {
                if (hasSecurityTag(stack, tag)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add("Security card");
        Set<String> tags = getSecurityTags(stack);
        if (tags.isEmpty()) {
            tooltip.add(TextFormatting.YELLOW + "Security card is empty!");
        } else {
            tooltip.add(TextFormatting.YELLOW + "Tags:");
            for (String tag : tags) {
                tooltip.add("   " + TextFormatting.GREEN + tag);
            }
        }
    }


    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IKeyCardSlot) {
            ((IKeyCardSlot) te).acceptKeyCard(player.getHeldItem(hand));
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return EnumActionResult.SUCCESS;
    }

    public static Set<String> getSecurityTags(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return Collections.emptySet();
        }
        Set<String> tags = new HashSet<>();
        NBTTagCompound compound = stack.getTagCompound();
        NBTTagList tagList = compound.getTagList("tags", Constants.NBT.TAG_STRING);
        for (int i = 0; i < tagList.tagCount(); i++) {
            String tag = tagList.getStringTagAt(i);
            tags.add(tag);
        }
        return tags;
    }

    public static boolean hasSecurityTag(ItemStack stack, String tag) {
        if (!stack.hasTagCompound()) {
            return false;
        }
        Set<String> tags = new HashSet<>();
        NBTTagCompound compound = stack.getTagCompound();
        NBTTagList tagList = compound.getTagList("tags", Constants.NBT.TAG_STRING);
        for (int i = 0; i < tagList.tagCount(); i++) {
            String t = tagList.getStringTagAt(i);
            if (tag.equals(t)) {
                return true;
            }
        }
        return false;
    }

    public static void addSecurityTag(ItemStack stack, String tag) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        Set<String> tags = new HashSet<>(getSecurityTags(stack));
        tags.add(tag);
        NBTTagList tagList = new NBTTagList();
        for (String t : tags) {
            tagList.appendTag(new NBTTagString(t));
        }
        stack.getTagCompound().setTag("tags", tagList);
    }
}