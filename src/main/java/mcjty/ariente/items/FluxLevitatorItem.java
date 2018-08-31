package mcjty.ariente.items;

import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.entities.FluxLevitatorEntity;
import mcjty.ariente.gui.HoloGuiEntity;
import mcjty.ariente.gui.HoloGuiHandler;
import mcjty.ariente.gui.ModGuis;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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

                FluxLevitatorEntity entity = new FluxLevitatorEntity(world, pos.getX() + 0.5D, pos.getY() + 0.0625D + d0, pos.getZ() + 0.5D);

                if (itemstack.hasDisplayName()) {
                    entity.setCustomNameTag(itemstack.getDisplayName());
                }

                world.spawnEntity(entity);

                HoloGuiEntity holoGui = HoloGuiHandler.openHoloGuiRelative(world, pos, player, entity, new Vec3d(0, .5, 1), ModGuis.GUI_LEVITATOR);
                holoGui.setTimeout(2000000000); // Never timeout
                holoGui.setSmall(true);
                entity.setHoloGui(holoGui);
            }

            itemstack.shrink(1);
            return EnumActionResult.SUCCESS;
        }
    }

}