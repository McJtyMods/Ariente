package mcjty.ariente.items;

import java.util.List;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Tools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

import static mcjty.lib.builder.TooltipBuilder.header;

public class FluxLevitatorItem extends Item implements ITooltipSettings {

    private final TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(header());

    public FluxLevitatorItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
        tooltipBuilder.makeTooltip(Tools.getId(this), stack, tooltip, flag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        BlockState state = world.getBlockState(pos);

        if (!FluxLevitatorEntity.isValidBeamBlock(state.getBlock())) {
            return InteractionResult.FAIL;
        } else {
            ItemStack itemstack = player.getItemInHand(hand);

            if (!world.isClientSide) {
                RailShape dir = FluxLevitatorEntity.getBeamDirection(state);
                double d0 = 0.0D;

                if (dir.isAscending()) {
                    d0 = 0.5D;
                }

                FluxLevitatorEntity levitator = FluxLevitatorEntity.create(world, pos.getX() + 0.5D, pos.getY() + 0.0625D + d0, pos.getZ() + 0.5D);

                if (itemstack.has(DataComponents.CUSTOM_NAME)) {
                    levitator.setCustomName(itemstack.getHoverName());
                }
                world.addFreshEntity(levitator);
            }

            itemstack.shrink(1);
            return InteractionResult.SUCCESS;
        }
    }

}
