package mcjty.ariente.items;

import java.util.List;
import mcjty.ariente.entities.fluxship.FluxShipEnergy;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Tools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import static mcjty.lib.builder.TooltipBuilder.general;
import static mcjty.lib.builder.TooltipBuilder.header;

public class FluxShipItem extends Item implements ITooltipSettings {

    private final TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(header(), general("placement"), general("charging"));

    public FluxShipItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
        tooltipBuilder.makeTooltip(Tools.getId(this), stack, tooltip, flag);
        int charge = getCharge(stack);
        tooltip.add(Component.translatable("message.ariente.ship_charge", Math.round(100f * charge / FluxShipEnergy.CAPACITY)));
    }

    public static int getCharge(ItemStack stack) {
        return ItemData.has(stack) ? Mth.clamp(ItemData.get(stack).getInt(FluxShipEnergy.CHARGE_TAG), 0, FluxShipEnergy.CAPACITY) : 0;
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
        ship.setCharge(getCharge(stack));
        if (!world.noCollision(ship, ship.getBoundingBox())) {
            player.displayClientMessage(Component.translatable("message.ariente.flux_ship.no_space"), true);
            return InteractionResult.FAIL;
        }
        if (!world.isClientSide) {
            if (stack.has(DataComponents.CUSTOM_NAME)) {
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
