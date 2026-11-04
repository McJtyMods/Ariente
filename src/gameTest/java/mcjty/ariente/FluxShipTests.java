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
            helper.assertTrue(speed > 96 && speed <= 104.01 && Math.abs(ship.getBank(1)) < .01,
                    "Straight flight must accelerate to a bounded cruise without banking");
            ship.handleInput(pilot, 1, 1, 0);
            float yaw = ship.getYRot();
            ship.tick();
            helper.assertTrue(ship.getYRot() - yaw > 0 && ship.getYRot() - yaw < 1,
                    "Turning must build up gradually");
            fly(ship, pilot, 1, 1, 1, 50);
            helper.assertTrue(ship.getBank(1) > 10 && ship.getBank(1) <= 22 && ship.getXRot() < -2,
                    "Forward right turns must bank right and climbs must raise the nose");
            fly(ship, pilot, 1, -1, -1, 65);
            helper.assertTrue(ship.getBank(1) < -10 && ship.getXRot() > 2,
                    "Left turns and descents must tilt in the opposite direction");
            fly(ship, pilot, -1, 0, 1, 90);
            helper.assertTrue(ship.getXRot() > 2, "Reversing uphill must raise the tail");
            fly(ship, pilot, 0, 0, 0, 160);
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
            helper.assertTrue(Math.abs(ship.getSpeed() - 1) < .01 && Math.abs(ship.getXRot()) < .01
                            && ship.getDeltaMovement().horizontalDistance() < .001 && ship.getDeltaMovement().y < 0,
                    "Dismounting must leave a level ship gently sinking without horizontal drift");
            pilot.startRiding(ship);
            fly(ship, pilot, 1, 1, 1, 10);
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
    public static void brakingAndReversingPreserveMomentum(GameTestHelper helper) {
        FluxShipEntity ship = ship(helper, 182000);
        Player pilot = helper.makeMockPlayer();
        pilot.startRiding(ship);
        try {
            fly(ship, pilot, 1, 0, 1, 80);
            helper.assertTrue(ship.getDeltaMovement().z > 5.12 && ship.getDeltaMovement().z <= 5.2,
                    "The ship must cruise above 101.92 blocks per second, 30% faster than a 78.4-block/s elytra dive");
            Vec3 previous = ship.getDeltaMovement();
            for (int i = 0; i < 15; i++) {
                fly(ship, pilot, 0, 0, 0, 1);
                Vec3 current = ship.getDeltaMovement();
                helper.assertTrue(current.z < previous.z && current.z >= previous.z * .9399
                                && current.y < previous.y && current.y >= previous.y * .9199,
                        "Releasing controls must coast down gradually on both axes");
                previous = current;
            }
            fly(ship, pilot, 1, 0, 1, 80);
            fly(ship, pilot, -1, 0, -1, 1);
            helper.assertTrue(ship.getDeltaMovement().z > 0 && ship.getDeltaMovement().y > 0,
                    "Reversing the controls must not instantly reverse travel");
            previous = ship.getDeltaMovement();
            for (int i = 0; i < 80; i++) {
                fly(ship, pilot, -1, 0, -1, 1);
                Vec3 current = ship.getDeltaMovement();
                helper.assertTrue(current.z < previous.z && previous.z - current.z <= .6241
                                && current.y < previous.y && previous.y - current.y <= .0561,
                        "Reversals must ease continuously through zero speed");
                previous = current;
            }
            helper.assertTrue(previous.z < -5.12 && previous.y < -.34,
                    "Reversed flight must eventually reach its horizontal and vertical cruise speeds");
            fly(ship, pilot, 0, 0, 0, 160);
            helper.assertTrue(ship.getSpeed() == 0, "Coasting must eventually settle into a stationary hover");
        } finally {
            pilot.stopRiding();
            ship.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void unattendedDescentSurvivesReloadAndStopsOnTheGround(GameTestHelper helper) {
        FluxShipEntity ship = ship(helper, 182200);
        Player pilot = helper.makeMockPlayer();
        pilot.startRiding(ship);
        try {
            fly(ship, pilot, 1, 0, 1, 50);
            Vec3 velocity = ship.getDeltaMovement();
            pilot.stopRiding();
            ship.tick();
            helper.assertTrue(ship.getDeltaMovement().z > velocity.z * .93
                            && ship.getDeltaMovement().y > velocity.y * .9,
                    "Losing the pilot must preserve momentum while easing toward a gentle descent");
            for (int i = 0; i < 150; i++) {
                ship.tick();
            }
            helper.assertTrue(Math.abs(ship.getDeltaMovement().y + .05) < .0001
                            && ship.getDeltaMovement().horizontalDistance() < .001,
                    "An unattended ship must settle to a descent of one block per second");
            pilot.startRiding(ship);
            fly(ship, pilot, 0, 0, 0, 130);
            helper.assertTrue(ship.getSpeed() == 0, "Boarding must smoothly arrest the unattended descent");
            pilot.stopRiding();
            ship.load(ship.saveWithoutId(new CompoundTag()));
            double altitude = ship.getY();
            ship.tick();
            helper.assertTrue(ship.getY() < altitude && ship.getDeltaMovement().y > -.01,
                    "A reloaded ship must ease back into descent even with only its panel aboard");
            ship.setPos(182200.5, 220, 181000.5);
            BlockPos floor = BlockPos.containing(ship.position()).below();
            for (BlockPos pos : BlockPos.betweenClosed(floor.offset(-3, 0, -3), floor.offset(3, 0, 3))) {
                helper.getLevel().setBlock(pos, Blocks.STONE.defaultBlockState(), 2);
            }
            for (int i = 0; i < 100; i++) {
                ship.tick();
            }
            helper.assertTrue(ship.onGround() && ship.getY() == 220 && ship.getSpeed() == 0,
                    "An unattended ship must rest on solid ground without falling through or bouncing");
            pilot.startRiding(ship);
            fly(ship, pilot, 0, 0, 1, 30);
            helper.assertTrue(ship.getY() > 225, "A landed ship must still take off when the pilot returns");
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
