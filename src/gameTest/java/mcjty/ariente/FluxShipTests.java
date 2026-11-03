package mcjty.ariente;

import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.gui.ModGuis;
import mcjty.hologui.gui.HoloGuiEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class FluxShipTests {

    @GameTest(template = "empty")
    public static void mountedPanelAllowsOnePilotAndSurvivesReload(GameTestHelper helper) {
        FluxShipEntity ship = ship(helper, 181000);
        Player pilot = helper.makeMockPlayer();
        try {
            ship.tick();
            HoloGuiEntity panel = (HoloGuiEntity) ship.getHoloGui();
            helper.assertTrue(panel.getVehicle() == ship && ModGuis.GUI_FLUX_SHIP.equals(panel.getGuiId())
                            && !panel.canClose() && !panel.canPin(), "The cockpit panel must stay mounted");
            helper.assertTrue(panel.getGui(pilot).findChild("ship_speed").isPresent()
                            && panel.getGui(pilot).findChild("ship_altitude").isPresent(),
                    "Both instruments must be available on a dedicated server");
            ship.interact(pilot, InteractionHand.MAIN_HAND);
            helper.assertTrue(pilot.getVehicle() == ship && ship.getControllingPassenger() == pilot,
                    "A panel must not occupy the pilot's seat");
            Player second = helper.makeMockPlayer();
            helper.assertTrue(!second.startRiding(ship), "The ship must still carry only one pilot");
            pilot.stopRiding();
            CompoundTag saved = new CompoundTag();
            helper.assertTrue(ship.save(saved), "The ship must save with its entity type and passengers");
            pilot.getAbilities().instabuild = true;
            pilot.setShiftKeyDown(true);
            helper.assertTrue(ship.hurt(helper.getLevel().damageSources().playerAttack(pilot), 1),
                    "A sneak-hit must recover an unattended ship even with its panel mounted");
            helper.assertTrue(panel.isRemoved(), "Recovering a ship must remove its mounted panel");
            Entity restoredShip = EntityType.loadEntityRecursive(saved, helper.getLevel(), entity -> entity);
            helper.assertTrue(restoredShip instanceof FluxShipEntity, "The complete ship must reload");
            ship = (FluxShipEntity) restoredShip;
            ship.tick();
            Entity restored = ship.getHoloGui().getEntity();
            helper.assertTrue(ship.getPassengers().size() == 1 && restored.getUUID().equals(panel.getUUID()),
                    "Reload must reuse the saved panel without a duplicate");
            restored.discard();
            ship.tick();
            helper.assertTrue(ship.getHoloGui() != null && ship.getPassengers().size() == 1,
                    "A missing cockpit panel must be recreated");
        } finally {
            pilot.stopRiding();
            ship.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void banksIntoTurnsAndPitchesWithForwardAndReverseClimbs(GameTestHelper helper) {
        FluxShipEntity ship = ship(helper, 181200);
        Player pilot = helper.makeMockPlayer();
        pilot.startRiding(ship);
        try {
            fly(ship, pilot, 1, 0, 0, 50);
            double speed = ship.getSpeed();
            helper.assertTrue(speed > 12 && speed <= 13.01 && Math.abs(ship.getBank(1)) < .01,
                    "Straight flight must accelerate to a bounded cruise without banking");
            ship.handleInput(pilot, 1, 1, 0);
            float yaw = ship.getYRot();
            ship.tick();
            helper.assertTrue(ship.getYRot() - yaw > 0 && ship.getYRot() - yaw < 1,
                    "Turning must build up gradually");
            fly(ship, pilot, 1, 1, 1, 50);
            helper.assertTrue(ship.getBank(1) > 10 && ship.getBank(1) <= 22 && ship.getXRot() < -10,
                    "Forward right turns must bank right and climbs must raise the nose");
            fly(ship, pilot, 1, -1, -1, 65);
            helper.assertTrue(ship.getBank(1) < -10 && ship.getXRot() > 10,
                    "Left turns and descents must tilt in the opposite direction");
            fly(ship, pilot, -1, 0, 1, 90);
            helper.assertTrue(ship.getXRot() > 10, "Reversing uphill must raise the tail");
            fly(ship, pilot, 0, 0, 0, 120);
            helper.assertTrue(ship.getSpeed() == 0 && Math.abs(ship.getBank(1)) < .01 && Math.abs(ship.getXRot()) < .01,
                    "Hover braking must stop and level the ship");
            fly(ship, pilot, 0, 1, 0, 50);
            helper.assertTrue(Math.abs(ship.getBank(1)) < .01, "Turning in place must not produce a cruise bank");
        } finally {
            pilot.stopRiding();
            ship.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void cockpitAndPilotFollowTheHullInEveryHeading(GameTestHelper helper) {
        FluxShipEntity ship = ship(helper, 181400);
        Player pilot = helper.makeMockPlayer();
        pilot.startRiding(ship);
        try {
            fly(ship, pilot, 1, 1, 1, 70);
            for (float yaw : new float[]{0, 90, 180, -90, 359}) {
                ship.setYRot(yaw);
                ship.positionRider(pilot);
                Entity panel = ship.getHoloGui().getEntity();
                ship.positionRider(panel);
                Vec3 forward = Vec3.directionFromRotation(0, yaw);
                Vec3 panelOffset = panel.position().subtract(ship.position());
                helper.assertTrue(panelOffset.dot(forward) > 1 && panel.getYRot() == yaw,
                        "The dashboard must stay ahead of the pilot at yaw " + yaw);
                helper.assertTrue(pilot.getEyePosition().distanceTo(ship.position().add(0, .5, 0)) < 1.3,
                        "The pilot's eyes must stay inside the tilted cabin");
                Vec3 up = ship.rotateCabinOffset(new Vec3(0, 1, 0));
                Vec3 right = ship.rotateCabinOffset(new Vec3(-1, 0, 0));
                helper.assertTrue(up.dot(forward) < 0 && right.y < 0,
                        "Climbing and right banking must match the rendered hull at every heading");
            }
        } finally {
            pilot.stopRiding();
            ship.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void staleUnauthorizedAndMalformedInputCannotKeepFlying(GameTestHelper helper) {
        FluxShipEntity ship = ship(helper, 181600);
        Player pilot = helper.makeMockPlayer();
        Player other = helper.makeMockPlayer();
        pilot.startRiding(ship);
        try {
            ship.handleInput(other, 1, 1, 1);
            ship.handleInput(pilot, Integer.MIN_VALUE, 0, 0);
            ship.tick();
            helper.assertTrue(ship.getSpeed() == 0 && ship.getYRot() == 0,
                    "Only the pilot's valid bounded input may move the ship");
            fly(ship, pilot, 1, 1, 1, 30);
            for (int i = 0; i < 150; i++) {
                ship.tick();
            }
            helper.assertTrue(ship.getSpeed() == 0 && Math.abs(ship.getBank(1)) < .01,
                    "Missing input packets must engage hover braking");
            fly(ship, pilot, 1, 1, 1, 30);
            pilot.stopRiding();
            for (int i = 0; i < 150; i++) {
                ship.tick();
            }
            helper.assertTrue(ship.getSpeed() == 0 && Math.abs(ship.getXRot()) < .01,
                    "Dismounting must leave a parked, level ship");
            pilot.startRiding(ship);
            fly(ship, pilot, 1, 1, 1, 5);
            helper.assertTrue(ship.getSpeed() > 0 && ship.getXRot() != 0,
                    "The reload check must start with an active ship");
            ship.load(ship.saveWithoutId(new CompoundTag()));
            helper.assertTrue(ship.getDeltaMovement().equals(Vec3.ZERO) && ship.getXRot() == 0
                            && ship.getBank(1) == 0 && ship.getSpeed() == 0,
                    "Reload must clear all transient flight state");
        } finally {
            pilot.stopRiding();
            ship.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void instrumentsMeasureActualTravelWhenBlocked(GameTestHelper helper) {
        FluxShipEntity ship = ship(helper, 181800);
        Player pilot = helper.makeMockPlayer();
        pilot.startRiding(ship);
        BlockPos wall = BlockPos.containing(ship.position()).south(4);
        try {
            for (BlockPos pos : BlockPos.betweenClosed(wall.offset(-4, -2, 0), wall.offset(4, 5, 0))) {
                helper.getLevel().setBlock(pos, Blocks.STONE.defaultBlockState(), 2);
            }
            fly(ship, pilot, 1, 0, 0, 70);
            helper.assertTrue(ship.getSpeed() == 0 && ship.getZ() < wall.getZ(),
                    "A blocked ship must report zero speed without entering the wall");
        } finally {
            pilot.stopRiding();
            ship.discard();
        }
        helper.succeed();
    }

    private static FluxShipEntity ship(GameTestHelper helper, int x) {
        FluxShipEntity ship = FluxShipEntity.create(helper.getLevel(), x + .5, 220, 181000.5);
        helper.getLevel().addFreshEntity(ship);
        return ship;
    }

    private static void fly(FluxShipEntity ship, Player pilot, int forward, int turn, int vertical, int ticks) {
        for (int i = 0; i < ticks; i++) {
            ship.handleInput(pilot, forward, turn, vertical);
            ship.tick();
        }
    }
}
