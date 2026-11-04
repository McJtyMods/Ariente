package mcjty.ariente.items;

import mcjty.lib.varia.Tools;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.entities.fluxship.FluxShipEnergy;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;

import javax.annotation.Nullable;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.general;

public class FluxShipItem extends Item implements ITooltipSettings {

    private final TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(header(), general("placement"), general("charging"));

    public FluxShipItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
        tooltipBuilder.makeTooltip(Tools.getId(this), stack, tooltip, flag);
        int charge = stack.hasTag() && stack.getTag().contains(FluxShipEnergy.CHARGE_TAG)
                ? stack.getTag().getInt(FluxShipEnergy.CHARGE_TAG) : FluxShipEnergy.CAPACITY;
        tooltip.add(Component.translatable("message.ariente.ship_charge", Math.round(100f * charge / FluxShipEnergy.CAPACITY)));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }
        BlockPos target = pos.relative(context.getClickedFace());
        if (!player.mayUseItemAt(target, context.getClickedFace(), context.getItemInHand())) {
            player.displayClientMessage(Component.translatable("message.ariente.flux_ship.placement_failed"), true);
            return InteractionResult.FAIL;
        }
        ItemStack stack = context.getItemInHand();
        FluxShipEntity ship = FluxShipEntity.create(world, target.getX() + .5, target.getY() + .0625, target.getZ() + .5);
        ship.setYRot(player.getYRot());
        if (stack.hasTag() && stack.getTag().contains(FluxShipEnergy.CHARGE_TAG)) {
            ship.setCharge(stack.getTag().getInt(FluxShipEnergy.CHARGE_TAG));
        }
        if (!world.noCollision(ship, ship.getBoundingBox())) {
            player.displayClientMessage(Component.translatable("message.ariente.flux_ship.no_space"), true);
            return InteractionResult.FAIL;
        }
        if (!world.isClientSide) {
            if (stack.hasCustomHoverName()) {
                ship.setCustomName(stack.getHoverName());
            }
            if (!world.addFreshEntity(ship)) {
                player.displayClientMessage(Component.translatable("message.ariente.flux_ship.placement_failed"), true);
                return InteractionResult.FAIL;
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }
}
