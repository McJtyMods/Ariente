package mcjty.ariente;

import mcjty.ariente.entities.fluxship.FluxShipMotion;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.entities.LaserEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class FluxShipMotionTests {

    @GameTest(template = "empty")
    public static void cannonHitsReachAPlayerInsideAnotherShip(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = helper.makeMockPlayer();
        var ship = FluxShipEntity.create(level, 0, 220, 0);
        player.startRiding(ship);
        try {
            float health = player.getHealth();
            DamageSource cannon = new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(LaserEntity.SHIP_DAMAGE));
            helper.assertTrue(ship.hurt(cannon, 4) && player.getHealth() < health,
                    "A player's vehicle hull must not make flying players immune to scout cannon fire");
        } finally {
            player.stopRiding();
            ship.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void delayedPacketsKeepVelocityContinuousAndStoppingConvergesWithoutDrift(GameTestHelper helper) {
        FluxShipMotion motion = new FluxShipMotion();
        Vec3 actualVelocity = new Vec3(0, 0, 1.5);
        Vec3 rendered = Vec3.ZERO;
        Vec3 vanilla = Vec3.ZERO;
        Vec3 target = Vec3.ZERO;
        motion.reset(target);
        int steps = 0;
        double oldSpeed = 0;
        double newSpeed = 0;
        double oldJerk = 0;
        double newJerk = 0;
        for (int tick = 1; tick <= 300; tick++) {
            // Delayed/batched packets while the server continues to fly at constant velocity.
            if (tick % 7 != 0 && tick % 13 != 0 && tick % 13 != 1) {
                target = actualVelocity.scale(tick);
                motion.accept(target);
                steps = 3;
            }
            Vec3 next = motion.advance(rendered, actualVelocity);
            Vec3 oldNext = steps > 0 ? vanilla.lerp(target, 1.0 / steps--) : vanilla;
            double speed = next.distanceTo(rendered);
            double legacySpeed = oldNext.distanceTo(vanilla);
            if (tick > 30) {
                newJerk += Math.abs(speed - newSpeed);
                oldJerk += Math.abs(legacySpeed - oldSpeed);
            }
            newSpeed = speed;
            oldSpeed = legacySpeed;
            rendered = next;
            vanilla = oldNext;
        }
        helper.assertTrue(newJerk < oldJerk * .5, "Remote motion must remove packet catch-up surges: new=" + newJerk + ", old=" + oldJerk);
        Vec3 stop = actualVelocity.scale(300);
        motion.accept(stop);
        for (int tick = 0; tick < 150; tick++) {
            rendered = motion.advance(rendered, Vec3.ZERO);
        }
        helper.assertTrue(rendered.distanceTo(stop) < .001, "Braking must settle at the authoritative stop without residual drift");
        motion.reset(Vec3.ZERO);
        rendered = Vec3.ZERO;
        for (int tick = 0; tick < 150; tick++) {
            rendered = motion.advance(rendered, actualVelocity);
        }
        helper.assertTrue(rendered.length() < 10, "A lost stream must not extrapolate a ghost ship indefinitely");
        Ariente.setup.getLogger().info("FLUX SHIP MOTION CHECK: accumulated velocity changes {} -> {} with delayed packets", oldJerk, newJerk);
        helper.succeed();
    }
}
