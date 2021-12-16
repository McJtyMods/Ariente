package mcjty.ariente.facade;

import mcjty.ariente.Ariente;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.level.Level;
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
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("regName");
    }

    private static String getMimicingString(ItemStack stack) {
        CompoundTag tag = stack.getTag();
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
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<TextComponent> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
        tooltipBuilder.makeTooltip(getRegistryName(), stack, tooltip, flag);
    }

    public static void setMimicBlock(@Nonnull ItemStack item, BlockState mimicBlock) {
        CompoundTag tagCompound = new CompoundTag();
        CompoundTag nbt = NBTUtil.writeBlockState(mimicBlock);
        tagCompound.put("mimic", nbt);
        item.setTag(tagCompound);
    }

    public static BlockState getMimicBlock(@Nonnull ItemStack stack) {
        CompoundTag tagCompound = stack.getTag();
        if (tagCompound == null || !tagCompound.contains("mimic")) {
            return Block.COBBLESTONE.defaultBlockState();
        } else {
            return NBTUtil.readBlockState(tagCompound.getCompound("mimic"));
        }
    }

    @Override
    protected boolean canPlace(BlockItemUseContext context, BlockState state) {
        return true;
    }

    @Override
    public InteractionResult useOn(ItemUseContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        ItemStack itemstack = player.getItemInHand(hand);

        if (!itemstack.isEmpty()) {

            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof IFacadeSupport) {
                IFacadeSupport facadeSupport = (IFacadeSupport) te;
                if (facadeSupport.getMimicBlock() == null) {
                    facadeSupport.setMimicBlock(getMimicBlock(itemstack));
                    SoundType soundtype = world.getBlockState(pos).getBlock().getSoundType(world.getBlockState(pos), world, pos, player);
                    world.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    int amount = -1;
                    itemstack.grow(amount);
                } else {
                    return InteractionResult.FAIL;
                }
            } else {
                setMimicBlock(itemstack, state);
                if (world.isClientSide) {
                    player.displayClientMessage(new StringTextComponent("Facade is now mimicing " + block.getDescriptionId()), false);
                }
            }
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.FAIL;
        }
    }
}
