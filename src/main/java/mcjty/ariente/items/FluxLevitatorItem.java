package mcjty.ariente.items;

import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class FluxLevitatorItem extends Item {

    public FluxLevitatorItem() {
        super(new Properties().maxStackSize(1));
//        super("flux_levitator");
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(new StringTextComponent("Flux Levitator"));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        BlockState state = world.getBlockState(pos);

        if (state.getBlock() != ModBlocks.fluxBeamBlock && state.getBlock() != ModBlocks.fluxBendBeamBlock) {
            return ActionResultType.FAIL;
        } else {
            ItemStack itemstack = player.getHeldItem(hand);

            if (!world.isRemote) {
                RailShape dir = FluxLevitatorEntity.getBeamDirection(state);
                double d0 = 0.0D;

                if (dir.isAscending()) {
                    d0 = 0.5D;
                }

                FluxLevitatorEntity levitator = new FluxLevitatorEntity(world, pos.getX() + 0.5D, pos.getY() + 0.0625D + d0, pos.getZ() + 0.5D);

                if (itemstack.hasDisplayName()) {
                    levitator.setCustomName(itemstack.getDisplayName());
                }
                world.addEntity(levitator);
            }

            itemstack.shrink(1);
            return ActionResultType.SUCCESS;
        }
    }

}