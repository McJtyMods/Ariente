package mcjty.ariente.facade;

import mcjty.ariente.Ariente;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class FacadeItemBlock extends BlockItem {

    public FacadeItemBlock(FacadeBlock block) {
        super(block, new Properties()
                .group(Ariente.setup.getTab()));
    }

    public static void setMimicBlock(@Nonnull ItemStack item, BlockState mimicBlock) {
        CompoundNBT tagCompound = new CompoundNBT();
        CompoundNBT nbt = NBTUtil.writeBlockState(mimicBlock);
        tagCompound.put("mimic", nbt);
        item.setTag(tagCompound);
    }

    public static BlockState getMimicBlock(@Nonnull ItemStack stack) {
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null || !tagCompound.contains("mimic")) {
            return Blocks.COBBLESTONE.getDefaultState();
        } else {
            return NBTUtil.readBlockState(tagCompound.getCompound("mimic"));
        }
    }

    @Override
    protected boolean canPlace(BlockItemUseContext context, BlockState state) {
        return true;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        ItemStack itemstack = player.getHeldItem(hand);

        if (!itemstack.isEmpty()) {

            TileEntity te = world.getTileEntity(pos);
            if (te instanceof IFacadeSupport) {
                IFacadeSupport facadeSupport = (IFacadeSupport) te;
                if (facadeSupport.getMimicBlock() == null) {
                    facadeSupport.setMimicBlock(getMimicBlock(itemstack));
                    SoundType soundtype = world.getBlockState(pos).getBlock().getSoundType(world.getBlockState(pos), world, pos, player);
                    world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    int amount = -1;
                    itemstack.grow(amount);
                } else {
                    return ActionResultType.FAIL;
                }
            } else {
                setMimicBlock(itemstack, state);
                if (world.isRemote) {
                    player.sendStatusMessage(new StringTextComponent("Facade is now mimicing " + block.getTranslationKey()), false);
                }
            }
            return ActionResultType.SUCCESS;
        } else {
            return ActionResultType.FAIL;
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World playerIn, List<ITextComponent> tooltip, ITooltipFlag advanced) {
        super.addInformation(stack, null, tooltip, advanced);
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null || !tagCompound.contains("regName")) {
            tooltip.add(new StringTextComponent(TextFormatting.BLUE + "Right or sneak-right click on block to mimic"));
            tooltip.add(new StringTextComponent(TextFormatting.BLUE + "Right or sneak-right click on cable/connector to hide"));
        } else {
            String regName = tagCompound.getString("regName");
            // @todo 1.14 meta
//            int meta = tagCompound.getInteger("meta");
            Block value = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(regName));
            if (value != null) {
                ItemStack s = new ItemStack(value, 1);
                if (s.getItem() != null) {
                    tooltip.add(new StringTextComponent(TextFormatting.BLUE + "Mimicing " + s.getDisplayName()));
                }
            }
        }
    }
}
