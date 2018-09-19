package mcjty.ariente.items;

import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.entities.FluxLevitatorEntity;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class FluxLevitatorItem extends GenericItem {

    public FluxLevitatorItem() {
        super("flux_levitator");
        this.maxStackSize = 1;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add("Flux Levitator");
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        IBlockState state = world.getBlockState(pos);

        if (state.getBlock() != ModBlocks.fluxBeamBlock) {
            return EnumActionResult.FAIL;
        } else {
            ItemStack itemstack = player.getHeldItem(hand);

            if (!world.isRemote) {
                BlockRailBase.EnumRailDirection dir = FluxLevitatorEntity.getBeamDirection(state);
                double d0 = 0.0D;

                if (dir.isAscending()) {
                    d0 = 0.5D;
                }

                FluxLevitatorEntity levitator = new FluxLevitatorEntity(world, pos.getX() + 0.5D, pos.getY() + 0.0625D + d0, pos.getZ() + 0.5D);

                if (itemstack.hasDisplayName()) {
                    levitator.setCustomNameTag(itemstack.getDisplayName());
                }
                world.spawnEntity(levitator);
            }

            itemstack.shrink(1);
            return EnumActionResult.SUCCESS;
        }
    }

}