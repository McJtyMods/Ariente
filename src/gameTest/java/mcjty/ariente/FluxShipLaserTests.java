package mcjty.ariente;

import io.netty.buffer.Unpooled;
import mcjty.ariente.entities.LaserEntity;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.entities.fluxship.FluxShipLaser;
import mcjty.ariente.entities.fluxship.PacketShipAction;
import mcjty.ariente.items.EnergyLanceItem;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class FluxShipLaserTests {

    @GameTest(template = "empty")
    public static void rapidFireIsLimitedAndOnlyAcceptsTheCurrentPilotsHeldInput(GameTestHelper helper) {
        FluxShipEntity ship = ship(helper);
        Player pilot = helper.makeMockPlayer();
        Player other = helper.makeMockPlayer();
        pilot.startRiding(ship);
        try {
            ship.handleInput(other, 0, 0, 0, true);
            ship.handleInput(pilot, 2, 0, 0, true);
            ship.tick();
            helper.assertTrue(shots(helper, ship).isEmpty(), "Unauthorized or malformed input must not fire");
            for (int tick = 0; tick < 20; tick++) {
                for (int spam = 0; spam < 20; spam++) {
                    ship.handleInput(pilot, 0, 0, 0, true);
                }
                ship.tick();
            }
            helper.assertTrue(shots(helper, ship).size() == 5, "Packet flooding must still allow only five shots per second");
            ship.handleInput(pilot, 0, 0, 0, false);
            for (int i = 0; i < 10; i++) {
                ship.tick();
            }
            helper.assertTrue(shots(helper, ship).size() == 5, "Releasing the trigger must stop firing");
            ship.handleInput(pilot, 0, 0, 0, true);
            for (int i = 0; i < 20; i++) {
                ship.tick();
            }
            helper.assertTrue(shots(helper, ship).size() == 7, "Missing input must expire after at most five ticks");
            ship.handleInput(pilot, 0, 0, 0, true);
            pilot.stopRiding();
            other.startRiding(ship);
            ship.tick();
            helper.assertTrue(shots(helper, ship).size() == 7, "A new pilot must not inherit the previous pilot's trigger");
            other.stopRiding();
            pilot.startRiding(ship);
            ship.handleInput(pilot, 0, 0, 0, true);
            ship.load(ship.saveWithoutId(new CompoundTag()));
            ship.tick();
            helper.assertTrue(shots(helper, ship).size() == 7, "Reloading must clear held fire input");
        } finally {
            pilot.stopRiding();
            other.stopRiding();
            cleanup(helper, ship);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void muzzleAndFastProjectileFollowTheHullAndSurviveSpawnAndSave(GameTestHelper helper) {
        FluxShipEntity ship = ship(helper);
        Player pilot = helper.makeMockPlayer();
        pilot.startRiding(ship);
        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        try {
            ship.setYRot(70);
            ship.setXRot(-18);
            pilot.setYRot(-100);
            pilot.setXRot(50);
            helper.assertTrue(FluxShipLaser.fire(ship, pilot, 0), "The mounted cannon must fire");
            LaserEntity laser = shots(helper, ship).get(0);
            Vec3 direction = FluxShipLaser.direction(ship, 1);
            helper.assertTrue(laser.getOwner() == pilot && laser.position().distanceTo(FluxShipLaser.muzzle(ship, 1)) < .0001,
                    "The shot must originate at the front cannon and credit its pilot");
            helper.assertTrue(laser.getBeamDirection().dot(direction) > .99999
                            && laser.getBeamDirection().dot(pilot.getLookAngle()) < .5,
                    "Mouse look must not steer the fixed cannon");
            Vec3 start = laser.position();
            for (int i = 0; i < 5; i++) {
                laser.tick();
            }
            helper.assertTrue(laser.position().distanceTo(start) > 39.9 && laser.getDeltaMovement().length() > 7.99,
                    "The projectile must keep outrunning the ship without losing speed");
            laser.writeSpawnData(data);
            LaserEntity clientCopy = new LaserEntity(Registration.ENTITY_LASER.get(), helper.getLevel());
            clientCopy.readSpawnData(data);
            clientCopy.lerpMotion(3.9, 3.9, 3.9);
            helper.assertTrue(clientCopy.isShipShot() && clientCopy.getDeltaMovement().distanceTo(laser.getDeltaMovement()) < .00001,
                    "Spawn data must preserve full velocity beyond vanilla's 3.9-block/tick limit");
            LaserEntity restored = new LaserEntity(Registration.ENTITY_LASER.get(), helper.getLevel());
            restored.load(laser.saveWithoutId(new CompoundTag()));
            helper.assertTrue(restored.isShipShot() && restored.getDeltaMovement().distanceTo(laser.getDeltaMovement()) < .00001,
                    "Saving must retain cannon projectile behavior");
            for (int i = 0; i < 20; i++) {
                laser.tick();
            }
            helper.assertTrue(laser.isRemoved(), "Missed shots must expire after their limited range");
            new PacketShipAction(1, -1, 1, true).toBytes(data);
            PacketShipAction decoded = new PacketShipAction(data);
            data.clear();
            decoded.toBytes(data);
            helper.assertTrue(data.readByte() == 1 && data.readByte() == -1 && data.readByte() == 1 && data.readBoolean(),
                    "Held fire must survive the ship input packet round trip");
        } finally {
            data.release();
            pilot.stopRiding();
            cleanup(helper, ship);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void rapidHitsDealLanceDamageAndWallsStopTheCannon(GameTestHelper helper) {
        FluxShipEntity ship = ship(helper);
        Player pilot = helper.makeMockPlayer();
        pilot.startRiding(ship);
        Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
        Map<BlockPos, BlockState> originalBlocks = new HashMap<>();
        try {
            pilot.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Registration.ENERGY_LANCE.get()));
            helper.assertTrue(Math.abs(EnergyLanceItem.getShotDamage(pilot) - FluxShipLaser.DAMAGE) < .001,
                    "The cannon must match an unupgraded lance's per-shot damage");
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
            target.setHealth(200);
            target.setNoAi(true);
            target.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
            Vec3 aim = FluxShipLaser.muzzle(ship, 1).add(0, -.6, 20);
            target.setPos(aim);
            helper.getLevel().addFreshEntity(target);
            for (int i = 0; i < 3; i++) {
                FluxShipLaser.fire(ship, pilot, 0);
                LaserEntity shot = shots(helper, ship).stream().filter(e -> !e.isRemoved()).findFirst().orElseThrow();
                for (int tick = 0; tick < 4 && !shot.isRemoved(); tick++) {
                    shot.tick();
                }
            }
            helper.assertTrue(Math.abs(target.getHealth() - (200 - FluxShipLaser.DAMAGE * 3)) < .01,
                    "Rapid hits must all damage an armored target during its ordinary hurt cooldown");
            BlockPos wall = BlockPos.containing(FluxShipLaser.muzzle(ship, 1).add(0, 0, 4));
            for (BlockPos pos : BlockPos.betweenClosed(wall.offset(-1, -1, 0), wall.offset(1, 2, 0))) {
                originalBlocks.put(pos.immutable(), helper.getLevel().getBlockState(pos));
                helper.getLevel().setBlock(pos, Blocks.STONE.defaultBlockState(), 2);
            }
            float health = target.getHealth();
            FluxShipLaser.fire(ship, pilot, 0);
            LaserEntity blocked = shots(helper, ship).stream().filter(e -> !e.isRemoved()).findFirst().orElseThrow();
            blocked.tick();
            helper.assertTrue(blocked.isRemoved() && target.getHealth() == health, "The existing laser effect must stop at a wall");
            for (int z : new int[]{3, 2}) {
                BlockPos barrelWall = BlockPos.containing(ship.position().add(0, .6, z));
                originalBlocks.put(barrelWall, helper.getLevel().getBlockState(barrelWall));
                helper.getLevel().setBlock(barrelWall, Blocks.STONE.defaultBlockState(), 2);
                helper.assertTrue(FluxShipLaser.muzzle(ship, 1).z < barrelWall.getZ(),
                        "An obstructed barrel or mount must not spawn shots through a close wall");
            }
        } finally {
            originalBlocks.forEach((pos, state) -> helper.getLevel().setBlock(pos, state, 2));
            target.discard();
            pilot.stopRiding();
            cleanup(helper, ship);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hologramSightIntersectsTheCannonLineWithAnOffsetPilotEye(GameTestHelper helper) {
        FluxShipEntity ship = ship(helper);
        Player pilot = helper.makeMockPlayer();
        pilot.startRiding(ship);
        try {
            for (float yaw : new float[]{0, 90, 180, -90}) {
                ship.setYRot(yaw);
                ship.setXRot(-15);
                ship.positionRider(pilot);
                Vec3 eye = pilot.getEyePosition();
                Vec3 sight = FluxShipLaser.sightOnPanel(ship, 1);
                Vec3 target = FluxShipLaser.target(ship, 1);
                helper.assertTrue(sight.subtract(eye).normalize().dot(target.subtract(eye).normalize()) > .99999,
                        "The reticle must point from the pilot's eye to the actual cannon line at every heading");
                helper.assertTrue(Math.abs(sight.subtract(FluxShipLaser.panelCenter(ship, 1))
                                .dot(FluxShipLaser.direction(ship, 1))) < .00001,
                        "The reticle must lie on the physical hologram plane");
            }
        } finally {
            pilot.stopRiding();
            cleanup(helper, ship);
        }
        helper.succeed();
    }

    private static FluxShipEntity ship(GameTestHelper helper) {
        BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        // Use the runner's loaded chunks so the real entity lookup and projectile collisions are active.
        FluxShipEntity ship = FluxShipEntity.create(helper.getLevel(), origin.getX() + .5, 220, origin.getZ() + .5);
        helper.getLevel().addFreshEntity(ship);
        return ship;
    }

    private static List<LaserEntity> shots(GameTestHelper helper, FluxShipEntity ship) {
        return helper.getLevel().getEntitiesOfClass(LaserEntity.class, ship.getBoundingBox().inflate(150), LaserEntity::isShipShot);
    }

    private static void cleanup(GameTestHelper helper, FluxShipEntity ship) {
        shots(helper, ship).forEach(LaserEntity::discard);
        ship.discard();
    }
}
