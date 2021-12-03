package mcjty.ariente.items;

import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.setup.Registration;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
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
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.header;

import net.minecraft.item.Item.Properties;

public class FluxLevitatorItem extends Item implements ITooltipSettings {

    private final TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(header());

    public FluxLevitatorItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
        tooltipBuilder.makeTooltip(getRegistryName(), stack, tooltip, flag);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        BlockState state = world.getBlockState(pos);

        if (state.getBlock() != Registration.FLUX_BEAM.get() && state.getBlock() != Registration.FLUX_BEND_BEAM.get()) {
            return ActionResultType.FAIL;
        } else {
            ItemStack itemstack = player.getItemInHand(hand);

            if (!world.isClientSide) {
                RailShape dir = FluxLevitatorEntity.getBeamDirection(state);
                double d0 = 0.0D;

                if (dir.isAscending()) {
                    d0 = 0.5D;
                }

                FluxLevitatorEntity levitator = FluxLevitatorEntity.create(world, pos.getX() + 0.5D, pos.getY() + 0.0625D + d0, pos.getZ() + 0.5D);

                if (itemstack.hasCustomHoverName()) {
                    levitator.setCustomName(itemstack.getHoverName());
                }
                world.addFreshEntity(levitator);
            }

            itemstack.shrink(1);
            return ActionResultType.SUCCESS;
        }
    }

}