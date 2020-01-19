package mcjty.ariente.items;

import mcjty.ariente.entities.fluxship.FluxShipEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class FluxShipItem extends Item {

    public FluxShipItem() {
        super(new Item.Properties().maxStackSize(1));
//        super("flux_ship");
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(new StringTextComponent("Flux Ship"));
        tooltip.add(new StringTextComponent(TextFormatting.RED + "Work In Progress!"));
        tooltip.add(new StringTextComponent(TextFormatting.RED + "Does not work yet!"));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        BlockState state = world.getBlockState(pos);

        ItemStack itemstack = player.getHeldItem(hand);

        if (!world.isRemote) {
            FluxShipEntity ship = new FluxShipEntity(world, pos.getX() + 0.5D, pos.getY() + 0.0625D, pos.getZ() + 0.5D);

            if (itemstack.hasDisplayName()) {
                ship.setCustomName(itemstack.getDisplayName());
            }
            world.addEntity(ship);
        }

        itemstack.shrink(1);
        return ActionResultType.SUCCESS;
    }

}