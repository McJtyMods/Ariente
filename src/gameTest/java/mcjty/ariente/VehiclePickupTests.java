package mcjty.ariente;

import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.items.FluxShipItem;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class VehiclePickupTests {

    @GameTest(template = "empty")
    public static void sneakPickupKeepsShipChargeNameAndRemovesPanel(GameTestHelper helper) {
        for (boolean creative : new boolean[]{false, true}) {
            Player player = helper.makeMockSurvivalPlayer();
            player.getAbilities().instabuild = creative;
            player.setShiftKeyDown(true);
            FluxShipEntity ship = FluxShipEntity.create(helper.getLevel(), 189000.5, 220, 189000.5);
            helper.getLevel().addFreshEntity(ship);
            ship.tick();
            Entity panel = ship.getHoloGui().getEntity();
            ship.setCharge(123456);
            ship.setCustomName(Component.literal("Named ship"));
            try {
                helper.assertTrue(ship.interact(player, InteractionHand.MAIN_HAND).consumesAction(), "Sneak-right-click must recover a ship");
                ItemStack recovered = player.getInventory().getItem(0);
                helper.assertTrue(recovered.is(Registration.FLUX_SHIP.get()) && recovered.getCount() == 1
                                && FluxShipItem.getCharge(recovered) == 123456 && recovered.getHoverName().getString().equals("Named ship"),
                        "Survival and creative pickup must preserve charge and custom name");
                helper.assertTrue(ship.isRemoved() && panel.isRemoved(), "Pickup must remove both ship and panel");
                helper.assertTrue(ship.interact(player, InteractionHand.OFF_HAND) == InteractionResult.PASS
                                && player.getInventory().countItem(Registration.FLUX_SHIP.get()) == 1,
                        "A repeated interaction with the removed ship must not duplicate its item");
            } finally {
                ship.discard();
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sneakPickupKeepsCartNameAndRemovesBothPanels(GameTestHelper helper) {
        for (boolean creative : new boolean[]{false, true}) {
            Player player = helper.makeMockSurvivalPlayer();
            player.getAbilities().instabuild = creative;
            player.setShiftKeyDown(true);
            FluxLevitatorEntity cart = FluxLevitatorEntity.create(helper.getLevel(), 189020.5, 220, 189000.5);
            helper.getLevel().addFreshEntity(cart);
            Entity front = cart.getHoloGuiFront().getEntity();
            Entity back = cart.getHoloGuiBack().getEntity();
            cart.setCustomName(Component.literal("Named cart"));
            try {
                helper.assertTrue(cart.interact(player, InteractionHand.OFF_HAND).consumesAction(), "Either hand must recover a cart");
                ItemStack recovered = player.getInventory().getItem(0);
                helper.assertTrue(recovered.is(Registration.FLUX_LEVITATOR.get()) && recovered.getCount() == 1
                                && recovered.getHoverName().getString().equals("Named cart"),
                        "Survival and creative pickup must preserve the cart's custom name");
                helper.assertTrue(cart.isRemoved() && front.isRemoved() && back.isRemoved(), "Pickup must remove both cart panels");
                helper.assertTrue(cart.interact(player, InteractionHand.MAIN_HAND) == InteractionResult.PASS
                                && player.getInventory().countItem(Registration.FLUX_LEVITATOR.get()) == 1,
                        "A repeated interaction with the removed cart must not duplicate its item");
            } finally {
                cart.discard();
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void occupiedVehiclesRejectPickupAndNormalClickBoards(GameTestHelper helper) {
        Player rider = helper.makeMockSurvivalPlayer();
        Player collector = helper.makeMockSurvivalPlayer();
        collector.setShiftKeyDown(true);
        Entity[] vehicles = {
                FluxShipEntity.create(helper.getLevel(), 189040.5, 220, 189000.5),
                FluxLevitatorEntity.create(helper.getLevel(), 189060.5, 220, 189000.5)
        };
        try {
            for (Entity vehicle : vehicles) {
                collector.getInventory().clearContent();
                helper.getLevel().addFreshEntity(vehicle);
                vehicle.interact(rider, InteractionHand.MAIN_HAND);
                helper.assertTrue(rider.getVehicle() == vehicle, "Normal right-click must still board");
                helper.assertTrue(vehicle.interact(collector, InteractionHand.MAIN_HAND) == InteractionResult.PASS
                                && vehicle.isAlive() && rider.getVehicle() == vehicle && collector.getInventory().isEmpty(),
                        "Sneak-right-click must leave an occupied vehicle and its rider intact");
                rider.stopRiding();
                helper.assertTrue(vehicle.interact(collector, InteractionHand.MAIN_HAND).consumesAction() && vehicle.isRemoved(),
                        "Pickup must work after the rider dismounts");
            }
        } finally {
            rider.stopRiding();
            for (Entity vehicle : vehicles) {
                vehicle.discard();
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fullInventoryDropsRecoveredVehiclesWithoutLosingCharge(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        player.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(1, 2, 1))));
        player.setShiftKeyDown(true);
        for (int slot = 0; slot < player.getInventory().items.size(); slot++) {
            player.getInventory().setItem(slot, new ItemStack(Items.STONE, 64));
        }
        FluxShipEntity ship = FluxShipEntity.create(helper.getLevel(), player.getX(), player.getY(), player.getZ());
        ship.setCharge(654321);
        FluxLevitatorEntity cart = FluxLevitatorEntity.create(helper.getLevel(), player.getX() + 2, player.getY(), player.getZ());
        AABB area = player.getBoundingBox().inflate(10);
        try {
            helper.getLevel().addFreshEntity(ship);
            helper.getLevel().addFreshEntity(cart);
            ship.interact(player, InteractionHand.MAIN_HAND);
            cart.interact(player, InteractionHand.MAIN_HAND);
            var drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area);
            helper.assertTrue(ship.isRemoved() && cart.isRemoved() && drops.size() == 2,
                    "A full survival inventory must drop exactly one item per vehicle");
            helper.assertTrue(drops.stream().anyMatch(drop -> drop.getItem().is(Registration.FLUX_SHIP.get())
                            && FluxShipItem.getCharge(drop.getItem()) == 654321)
                            && drops.stream().anyMatch(drop -> drop.getItem().is(Registration.FLUX_LEVITATOR.get())),
                    "The dropped ship must retain its charge and the cart must be recoverable");
        } finally {
            ship.discard();
            cart.discard();
            helper.getLevel().getEntitiesOfClass(ItemEntity.class, area).forEach(Entity::discard);
        }
        helper.succeed();
    }
}
