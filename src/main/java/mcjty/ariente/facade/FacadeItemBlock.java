package mcjty.ariente.facade;

import mcjty.ariente.Ariente;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
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
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.*;

import net.minecraft.item.Item.Properties;

public class FacadeItemBlock extends BlockItem implements ITooltipSettings {

    private TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(header(),
                    gold(stack -> !isMimicing(stack)),
                    parameter("info", FacadeItemBlock::isMimicing, FacadeItemBlock::getMimicingString));

    private static boolean isMimicing(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        return tag != null && tag.contains("regName");
    }

    private static String getMimicingString(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag != null) {
            String regName = tag.getString("regName");
            Block value = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(regName));
            if (value != null) {
                ItemStack s = new ItemStack(value, 1);
                if (s.getItem() != null) {
                    return s.getHoverName().getString();
                }
            }
        }
        return "<unset>";
    }

    public FacadeItemBlock(FacadeBlock block) {
        super(block, new Properties()
                .tab(Ariente.setup.getTab()));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
        tooltipBuilder.makeTooltip(getRegistryName(), stack, tooltip, flag);
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
            return Blocks.COBBLESTONE.defaultBlockState();
        } else {
            return NBTUtil.readBlockState(tagCompound.getCompound("mimic"));
        }
    }

    @Override
    protected boolean canPlace(BlockItemUseContext context, BlockState state) {
        return true;
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        ItemStack itemstack = player.getItemInHand(hand);

        if (!itemstack.isEmpty()) {

            TileEntity te = world.getBlockEntity(pos);
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
                if (world.isClientSide) {
                    player.displayClientMessage(new StringTextComponent("Facade is now mimicing " + block.getDescriptionId()), false);
                }
            }
            return ActionResultType.SUCCESS;
        } else {
            return ActionResultType.FAIL;
        }
    }
}
