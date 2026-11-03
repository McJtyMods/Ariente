package mcjty.ariente;

import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class FluxLevitatorTests {

    @GameTest(template = "empty")
    public static void slopesCarryRidersBothWaysInEveryDirection(GameTestHelper helper) {
        for (Direction uphill : Direction.Plane.HORIZONTAL) {
            BlockPos bottom = new BlockPos(166000 + uphill.get2DDataValue() * 100, 200, 166000);
            for (int distance = -8; distance <= 20; distance++) {
                int height = Math.max(0, Math.min(12, distance));
                BlockPos rail = bottom.relative(uphill, distance).above(height);
                for (BlockPos pos : BlockPos.betweenClosed(rail.offset(-2, 0, -2), rail.offset(2, 5, 2))) {
                    helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
                helper.getLevel().setBlock(rail.below(), Blocks.STONE.defaultBlockState(), 2);
            }
            for (int distance = -8; distance <= 20; distance++) {
                int height = Math.max(0, Math.min(12, distance));
                boolean slope = distance >= 0 && distance < 12;
                helper.getLevel().setBlock(bottom.relative(uphill, distance).above(height),
                        (slope ? Registration.FLUX_SLOPE_BEAM : Registration.FLUX_BEAM).get().defaultBlockState()
                                .setValue(BlockStateProperties.HORIZONTAL_FACING, slope ? uphill
                                        : uphill.getAxis() == Direction.Axis.X ? Direction.NORTH : Direction.EAST), 2);
            }
            for (int speed : new int[]{1, 5, 40, 80, -1, -5, -40, -80}) {
                BlockPos startRail = bottom.relative(uphill, speed > 0 ? -5 : 17).above(speed > 0 ? 0 : 12);
                Vec3 start = Vec3.atBottomCenterOf(startRail).add(0, .0625, 0);
                FluxLevitatorEntity cart = FluxLevitatorEntity.create(helper.getLevel(), start.x, start.y, start.z);
                CompoundTag saved = cart.saveWithoutId(new CompoundTag());
                saved.putDouble("forwardDirectionX", uphill.getStepX());
                saved.putDouble("forwardDirectionZ", uphill.getStepZ());
                cart.load(saved);
                helper.getLevel().addFreshEntity(cart);
                Player rider = helper.makeMockPlayer();
                rider.startRiding(cart, true);
                cart.changeSpeed(speed);
                boolean crossedSlope = false;
                boolean stopped = false;
                try {
                    for (int tick = 0; tick < 1500; tick++) {
                        Vec3 previous = cart.position();
                        cart.tick();
                        cart.positionRider(rider);
                        cart.positionRider(cart.getHoloGuiFront().getEntity());
                        cart.positionRider(cart.getHoloGuiBack().getEntity());
                        Vec3 track = cart.getPos(cart.getX(), cart.getY(), cart.getZ());
                        helper.assertTrue(track != null && Math.abs(track.y - cart.getY()) < .00001,
                                "Cart left slope " + uphill + " at speed " + speed + ": " + cart.position());
                        helper.assertTrue(cart.getSpeed() == speed, "Slopes must retain the speed setting");
                        helper.assertTrue(Math.abs(cart.getY() - previous.y) <= .401,
                                "Slope entry/exit must not jump vertically");
                        helper.assertTrue(rider.getVehicle() == cart && rider.distanceToSqr(cart) < 1,
                                "Rider must remain inside the cabin on slopes");
                        Entity front = cart.getHoloGuiFront().getEntity();
                        Entity back = cart.getHoloGuiBack().getEntity();
                        helper.assertTrue(Math.abs(front.distanceTo(back) - 2) < .001
                                        && front.getLookAngle().dot(back.getLookAngle()) < -.999,
                                "Both control panels must follow opposite ends of the incline");
                        double progress = cart.position().subtract(Vec3.atCenterOf(bottom))
                                .dot(new Vec3(uphill.getStepX(), 0, uphill.getStepZ()));
                        if (progress > 2 && progress < 9) {
                            crossedSlope = true;
                            Vec3 head = rider.getEyePosition().subtract(cart.position().add(0, .375, 0));
                            Vec3 incline = new Vec3(uphill.getStepX(), 1, uphill.getStepZ()).normalize();
                            Vec3 cabinUp = new Vec3(-uphill.getStepX(), 1, -uphill.getStepZ()).normalize();
                            helper.assertTrue(Math.abs(head.dot(incline)) < .95 && head.dot(cabinUp) < 1.1
                                            && head.dot(cabinUp) > .3,
                                    "Rider's head must fit below the roof and between the tilted end walls");
                            helper.assertTrue(Math.abs(front.getY() - back.getY()) > 1.3,
                                    "Control panels must tilt with the cabin");
                            if (!stopped) {
                                cart.changeSpeed(0);
                                cart.setDeltaMovement(Vec3.ZERO);
                                cart.tick();
                                Vec3 parked = cart.position();
                                cart.tick();
                                helper.assertTrue(cart.position().distanceToSqr(parked) < .000001,
                                        "A stopped cart must hold its position on a slope");
                                // Reload while parked on the incline, then resume in the same direction.
                                cart.load(cart.saveWithoutId(new CompoundTag()));
                                cart.changeSpeed(speed);
                                stopped = true;
                            }
                        }
                        if (speed > 0 ? progress > 14 : progress < -3) {
                            break;
                        }
                    }
                    double height = cart.getY() - bottom.getY();
                    helper.assertTrue(crossedSlope && (speed > 0 ? height > 12 : height < 1),
                            "Cart must finish the climb/descent: " + uphill + ", " + speed + ", " + cart.position());
                } finally {
                    rider.stopRiding();
                    discardCart(cart);
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void panelsOccupyOppositeEndsAndRecoverFromOverlapOrRemoval(GameTestHelper helper) {
        FluxLevitatorEntity cart = cart(helper, 160000);
        try {
            cart.tick();
            Entity front = cart.getHoloGuiFront().getEntity();
            Entity back = cart.getHoloGuiBack().getEntity();
            checkPanels(helper, cart);
            back.setPos(front.position());
            back.setYRot(front.getYRot());
            cart.positionRider(back);
            checkPanels(helper, cart);
            back.discard();
            Entity replacement = cart.getHoloGuiBack().getEntity();
            helper.assertTrue(replacement != back && replacement.isAlive(), "A removed panel must be replaced");
            checkPanels(helper, cart);
        } finally {
            discardCart(cart);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void heldInputAcceleratesAndDeceleratesWithoutReversing(GameTestHelper helper) {
        FluxLevitatorEntity cart = cart(helper, 160200);
        var driver = helper.makeMockPlayer();
        var other = helper.makeMockPlayer();
        driver.setYRot(-90);
        driver.startRiding(cart, true);
        try {
            cart.changeSpeed(12);
            cart.handleInput(driver, -1);
            cart.tick();
            helper.assertTrue(cart.getSpeed() == 11, "Back must decelerate one step, not stop instantly");
            for (int tick = 0; tick < 20; tick++) {
                cart.handleInput(driver, -1);
                cart.tick();
            }
            helper.assertTrue(cart.getSpeed() == 0, "Holding Back must stop without reversing");
            for (int tick = 0; tick < 10; tick++) {
                cart.handleInput(driver, 1);
                cart.tick();
            }
            helper.assertTrue(cart.getSpeed() == 10, "Forward must accelerate gradually");
            cart.handleInput(driver, 0);
            cart.tick();
            helper.assertTrue(cart.getSpeed() == 10, "Releasing the keys must keep the speed");
            cart.changeSpeed(80);
            cart.handleInput(driver, 1);
            cart.tick();
            helper.assertTrue(cart.getSpeed() == 80, "Keyboard controls must respect the speed limit");
            cart.changeSpeed(-12);
            driver.setYRot(90);
            cart.handleInput(driver, 1);
            cart.tick();
            helper.assertTrue(cart.getSpeed() == -13, "Forward must accelerate in the selected reverse direction");
            cart.handleInput(driver, -1);
            cart.tick();
            helper.assertTrue(cart.getSpeed() == -12, "Back must also slow reverse travel gradually");
            for (int tick = 0; tick < 20; tick++) {
                cart.handleInput(driver, -1);
                cart.tick();
            }
            helper.assertTrue(cart.getSpeed() == 0, "Braking in reverse must clamp at zero");
            cart.handleInput(driver, 0);
            cart.handleInput(other, 1);
            cart.handleInput(driver, 127);
            cart.tick();
            helper.assertTrue(cart.getSpeed() == 0, "Unmounted players and invalid input must be ignored");
            other.startRiding(cart, true);
            cart.handleInput(other, 1);
            cart.tick();
            helper.assertTrue(cart.getSpeed() == 0, "Only the first player aboard may drive");
            other.stopRiding();
            driver.setYRot(-90);
            cart.handleInput(driver, 1);
            for (int tick = 0; tick < 12; tick++) {
                cart.tick();
            }
            helper.assertTrue(cart.getSpeed() == 5, "Missing input packets must expire after five ticks");
            cart.changeSpeed(0);
            cart.handleInput(driver, 1);
            driver.stopRiding();
            cart.tick();
            helper.assertTrue(cart.getSpeed() == 0, "Dismounting must immediately release the throttle");
        } finally {
            driver.stopRiding();
            other.stopRiding();
            discardCart(cart);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void forwardFollowsFacingAndBrakesThroughZeroBeforeReversing(GameTestHelper helper) {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            FluxLevitatorEntity cart = cart(helper, 160600 + facing.get2DDataValue() * 200);
            var driver = helper.makeMockPlayer();
            if (facing.getAxis() == Direction.Axis.Z) {
                for (int z = -80; z <= 80; z++) {
                    helper.getLevel().setBlock(BlockPos.containing(cart.getX(), 240, 160000 + z),
                            Registration.FLUX_BEAM.get().defaultBlockState()
                                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST), 2);
                }
            }
            driver.setYRot(facing.toYRot());
            driver.startRiding(cart, true);
            Vec3 expected = new Vec3(facing.getStepX(), 0, facing.getStepZ());
            try {
                Vec3 start = cart.position();
                for (int tick = 0; tick < 15; tick++) {
                    cart.handleInput(driver, 1);
                    cart.tick();
                }
                helper.assertTrue(Math.abs(cart.getSpeed()) == 15 && cart.getDeltaMovement().dot(expected) > 0
                                && cart.position().subtract(start).dot(expected) > 1,
                        "Forward must accelerate where the rider faces: " + facing);
                driver.setYRot(facing.getOpposite().toYRot());
                cart.handleInput(driver, 1);
                cart.tick();
                helper.assertTrue(Math.abs(cart.getSpeed()) == 14 && cart.getDeltaMovement().dot(expected) > 0,
                        "Turning around must first decelerate existing motion gradually");
                for (int tick = 0; tick < 30; tick++) {
                    cart.handleInput(driver, 1);
                    cart.tick();
                }
                helper.assertTrue(Math.abs(cart.getSpeed()) == 16 && cart.getDeltaMovement().dot(expected) < 0,
                        "Continuing Forward after turning around must reverse along the beam: " + facing);
                for (int tick = 0; tick < 25; tick++) {
                    cart.handleInput(driver, -1);
                    cart.tick();
                }
                helper.assertTrue(cart.getSpeed() == 0, "Back must remain a gradual brake in every facing");
            } finally {
                driver.stopRiding();
                discardCart(cart);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void stoppedCartsRestartBothWaysAtEveryBend(GameTestHelper helper) {
        Direction[] facings = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        Direction[][] exits = {{Direction.SOUTH, Direction.WEST}, {Direction.NORTH, Direction.EAST},
                {Direction.SOUTH, Direction.EAST}, {Direction.NORTH, Direction.WEST}};
        for (int index = 0; index < facings.length; index++) {
            BlockPos corner = new BlockPos(162000 + index * 32, 240, 160000);
            for (BlockPos pos : BlockPos.betweenClosed(corner.offset(-7, -1, -7), corner.offset(7, 3, 7))) {
                helper.getLevel().setBlock(pos, pos.getY() == 239 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
            }
            helper.getLevel().setBlock(corner, Registration.FLUX_BEND_BEAM.get().defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, facings[index]), 2);
            for (Direction exit : exits[index]) {
                for (int distance = 1; distance <= 6; distance++) {
                    helper.getLevel().setBlock(corner.relative(exit, distance), Registration.FLUX_BEAM.get().defaultBlockState()
                            .setValue(BlockStateProperties.HORIZONTAL_FACING,
                                    exit.getAxis() == Direction.Axis.X ? Direction.NORTH : Direction.EAST), 2);
                }
            }
            Vec3 center = Vec3.atBottomCenterOf(corner).add(0, .0625, 0);
            Vec3 first = new Vec3(exits[index][0].getStepX(), 0, exits[index][0].getStepZ()).scale(.5);
            Vec3 second = new Vec3(exits[index][1].getStepX(), 0, exits[index][1].getStepZ()).scale(.5);
            for (double fraction : new double[]{.1, .5, .9}) {
                for (int speed : new int[]{1, -1, 20, -20}) {
                    Vec3 start = center.add(first.scale(1 - fraction)).add(second.scale(fraction));
                    FluxLevitatorEntity cart = FluxLevitatorEntity.create(helper.getLevel(), start.x, start.y, start.z);
                    helper.getLevel().addFreshEntity(cart);
                    try {
                        cart.tick();
                        cart.changeSpeed(speed);
                        cart.tick();
                        helper.assertTrue(cart.getSpeed() == speed && cart.position().distanceToSqr(start) > .0000001,
                                "A stopped cart must restart at bend " + facings[index] + ", speed=" + speed + ", fraction=" + fraction);
                        for (int tick = 0; tick < 260 && BlockPos.containing(cart.position()).equals(corner); tick++) {
                            cart.tick();
                        }
                        Direction exit = exits[index][speed > 0 ? 1 : 0];
                        Vec3 exitDirection = new Vec3(exit.getStepX(), 0, exit.getStepZ());
                        helper.assertTrue(cart.getSpeed() == speed && cart.position().subtract(center).dot(exitDirection) > .5,
                                "Restarted cart must leave through the selected exit of " + facings[index] + ": " + cart.position());
                        cart.changeSpeed(0);
                        cart.setDeltaMovement(Vec3.ZERO);
                        CompoundTag saved = cart.saveWithoutId(new CompoundTag());
                        FluxLevitatorEntity reloaded = FluxLevitatorEntity.create(helper.getLevel(), 0, 0, 0);
                        reloaded.load(saved);
                        discardCart(cart);
                        cart = reloaded;
                        helper.getLevel().addFreshEntity(cart);
                        cart.changeSpeed(speed);
                        cart.tick();
                        helper.assertTrue(cart.getSpeed() == speed && cart.getDeltaMovement().dot(exitDirection) > 0,
                                "Stopping and reloading must retain the route direction after " + facings[index]);
                    } finally {
                        discardCart(cart);
                    }
                }
            }
            for (Direction exit : exits[index]) {
                Vec3 start = center.add(first.add(second).scale(.5));
                FluxLevitatorEntity cart = FluxLevitatorEntity.create(helper.getLevel(), start.x, start.y, start.z);
                helper.getLevel().addFreshEntity(cart);
                var driver = helper.makeMockPlayer();
                driver.setYRot(exit.toYRot());
                driver.startRiding(cart, true);
                try {
                    for (int tick = 0; tick < 25; tick++) {
                        cart.handleInput(driver, 1);
                        cart.tick();
                    }
                    Vec3 exitDirection = new Vec3(exit.getStepX(), 0, exit.getStepZ());
                    helper.assertTrue(Math.abs(cart.getSpeed()) == 25 && cart.position().subtract(center).dot(exitDirection) > .5,
                            "Gradual keyboard acceleration must build enough motion to leave bend " + facings[index]
                                    + " towards " + exit + ": " + cart.position());
                } finally {
                    driver.stopRiding();
                    discardCart(cart);
                }
            }
        }
        helper.succeed();
    }

    private static FluxLevitatorEntity cart(GameTestHelper helper, int x) {
        var level = helper.getLevel();
        for (BlockPos pos : BlockPos.betweenClosed(x - 100, 239, 159998, x + 100, 243, 160002)) {
            level.setBlock(pos, pos.getY() == 239 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
        }
        for (int i = -100; i < 100; i++) {
            level.setBlock(new BlockPos(x + i, 240, 160000), Registration.FLUX_BEAM.get().defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH), 2);
        }
        FluxLevitatorEntity cart = FluxLevitatorEntity.create(level, x + 2.5, 240.0625, 160000.5);
        level.addFreshEntity(cart);
        return cart;
    }

    private static void checkPanels(GameTestHelper helper, FluxLevitatorEntity cart) {
        Entity front = cart.getHoloGuiFront().getEntity();
        Entity back = cart.getHoloGuiBack().getEntity();
        helper.assertTrue(front != back && front.getVehicle() == cart && back.getVehicle() == cart,
                "Both distinct panels must be mounted on their cart");
        helper.assertTrue(Math.abs(front.position().distanceTo(back.position()) - 2) < .0001
                        && front.getLookAngle().dot(back.getLookAngle()) < -.999,
                "Panels must occupy opposite ends and face opposite directions");
    }

    private static void discardCart(FluxLevitatorEntity cart) {
        cart.getPassengers().stream().filter(entity -> !(entity instanceof Player))
                .toList().forEach(Entity::discard);
        cart.discard();
    }
}
