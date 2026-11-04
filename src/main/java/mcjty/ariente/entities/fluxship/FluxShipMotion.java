package mcjty.ariente.entities.fluxship;

import net.minecraft.world.phys.Vec3;

/** Remote ships retain velocity between position packets instead of repeatedly restarting a short lerp. */
public class FluxShipMotion {

    private Vec3 target;
    private Vec3 velocity;
    private int age;

    public void accept(Vec3 position) {
        target = position;
        age = 0;
    }

    public void reset(Vec3 position) {
        accept(position);
        velocity = null;
    }

    public Vec3 advance(Vec3 position, Vec3 motion) {
        if (target == null) {
            target = position;
        }
        if (position.distanceToSqr(target) > 64 * 64) {
            velocity = motion;
            return target;
        }
        if (velocity == null) {
            velocity = motion;
        }
        // Keep the usual interpolation delay, with a small amount of prediction for late packets.
        Vec3 desired = target.add(motion.scale(Math.min(age, 5) - 3));
        double freshness = Math.max(0, 1 - Math.max(0, age - 5) / 5.0);
        Vec3 correction = desired.subtract(position).scale(.18);
        Vec3 nextVelocity = velocity.lerp(motion.scale(freshness).add(correction), .35);
        Vec3 acceleration = nextVelocity.subtract(velocity);
        double limit = Math.max(.1, motion.length() * .06);
        if (motion.lengthSqr() > .000001 && acceleration.lengthSqr() > limit * limit) {
            nextVelocity = velocity.add(acceleration.normalize().scale(limit));
        }
        velocity = nextVelocity;
        age++;
        if (motion.lengthSqr() < .000001 && velocity.lengthSqr() < .000001 && position.distanceToSqr(target) < .000001) {
            velocity = Vec3.ZERO;
            return target;
        }
        return position.add(velocity);
    }
}
