package mcjty.ariente.items;

import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;

import javax.annotation.Nullable;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.warning;

public class FluxShipItem extends Item implements ITooltipSettings {

    private final TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(header(), warning());

    public FluxShipItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
        tooltipBuilder.makeTooltip(getRegistryName(), stack, tooltip, flag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        BlockState state = world.getBlockState(pos);

        ItemStack itemstack = player.getItemInHand(hand);

        if (!world.isClientSide) {
            FluxShipEntity ship = FluxShipEntity.create(world, pos.getX() + 0.5D, pos.getY() + 0.0625D, pos.getZ() + 0.5D);

            if (itemstack.hasCustomHoverName()) {
                ship.setCustomName(itemstack.getHoverName());
            }
            world.addFreshEntity(ship);
        }

        itemstack.shrink(1);
        return InteractionResult.SUCCESS;
    }

}