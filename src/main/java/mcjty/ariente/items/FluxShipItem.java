package mcjty.ariente.items;

import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class FluxShipItem extends GenericItem {

    public FluxShipItem() {
        super("flux_ship");
        this.maxStackSize = 1;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add("Flux Ship");
        tooltip.add(TextFormatting.RED + "Work In Progress!");
        tooltip.add(TextFormatting.RED + "Does not work yet!");
    }

    @Override
    public EnumActionResult onItemUse(PlayerEntity player, World world, BlockPos pos, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
        BlockState state = world.getBlockState(pos);

        ItemStack itemstack = player.getHeldItem(hand);

        if (!world.isRemote) {
            FluxShipEntity ship = new FluxShipEntity(world, pos.getX() + 0.5D, pos.getY() + 0.0625D, pos.getZ() + 0.5D);

            if (itemstack.hasDisplayName()) {
                ship.setCustomNameTag(itemstack.getDisplayName());
            }
            world.spawnEntity(ship);
        }

        itemstack.shrink(1);
        return EnumActionResult.SUCCESS;
    }

}