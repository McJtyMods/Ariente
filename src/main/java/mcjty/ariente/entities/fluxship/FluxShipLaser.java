package mcjty.ariente.entities.fluxship;

import mcjty.ariente.api.IFluxShipPilot;
import mcjty.ariente.entities.LaserEntity;
import mcjty.ariente.items.EnergyLanceItem;
import mcjty.ariente.items.EnergySabreItem;
import mcjty.hologui.api.IHoloGuiEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** The muzzle, projectile and cockpit sight all use the hull's forward axis. */
public class FluxShipLaser {

    public static final int SHOT_INTERVAL = 4;
    public static final int SHOT_LIFETIME = 16;
    public static final double SHOT_SPEED = 9.6;
    public static final double RANGE = SHOT_LIFETIME * SHOT_SPEED;
    public static final Vec3 MOUNT = new Vec3(0, .12, 2.35);
    public static final Vec3 MUZZLE = new Vec3(0, .12, 3.12);
    // An unupgraded player lance: the sabre's damage plus the player's base attack, multiplied by 1.3.
    public static final float DAMAGE = (EnergySabreItem.BASE_DAMAGE + 1) * EnergyLanceItem.DAMAGE_MULTIPLIER;
    public static final float SCOUT_DAMAGE = 8.0F;
    private static final double PANEL_DISTANCE = 1.5;

    public static Vec3 direction(FluxShipEntity ship, float partialTick) {
        return ship.rotateCabinOffset(new Vec3(0, 0, 1), partialTick).normalize();
    }

    private static Vec3 cabinPoint(FluxShipEntity ship, Vec3 point, float partialTick) {
        return ship.getPosition(partialTick).add(0, .5, 0).add(ship.rotateCabinOffset(point, partialTick));
    }

    public static Vec3 muzzle(FluxShipEntity ship, float partialTick) {
        // Start inside the hull so even a wall clipping into the cannon mount blocks the shot.
        Vec3 base = cabinPoint(ship, new Vec3(0, MUZZLE.y, 0), partialTick);
        Vec3 end = cabinPoint(ship, MUZZLE, partialTick);
        HitResult obstruction = ship.level().clip(new ClipContext(base, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, ship));
        return obstruction.getType() == HitResult.Type.MISS ? end
                : obstruction.getLocation().subtract(direction(ship, partialTick).scale(.01));
    }

    public static boolean fire(FluxShipEntity ship, LivingEntity pilot, int cooldown) {
        if (ship.level().isClientSide() || cooldown > 0 || ship.getControllingPassenger() != pilot || !pilot.isAlive()) {
            return false;
        }
        return ship.level().addFreshEntity(LaserEntity.createShipShot(ship.level(), pilot,
                muzzle(ship, 1), direction(ship, 1), getShotDamage(pilot)));
    }

    public static float getShotDamage(LivingEntity pilot) {
        return pilot instanceof IFluxShipPilot ? SCOUT_DAMAGE : DAMAGE;
    }

    private static double eyeHeight(FluxShipEntity ship) {
        LivingEntity pilot = ship.getControllingPassenger();
        return pilot == null ? 1.67 : ship.getPassengersRidingOffset() + (-pilot.getVehicleAttachmentPoint(ship).y) + pilot.getEyeHeight();
    }

    public static Vec3 panelCenter(FluxShipEntity ship, float partialTick) {
        double height = Mth.lerp(PANEL_DISTANCE / (MUZZLE.z + RANGE), eyeHeight(ship) - .5, MUZZLE.y);
        return cabinPoint(ship, new Vec3(0, height, PANEL_DISTANCE), partialTick);
    }

    public static Vec3 target(FluxShipEntity ship, float partialTick) {
        Vec3 start = muzzle(ship, partialTick);
        Vec3 direction = direction(ship, partialTick);
        HitResult block = ship.level().clip(new ClipContext(start, start.add(direction.scale(RANGE)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, ship));
        EntityHitResult entity = ProjectileUtil.getEntityHitResult(ship, start, block.getLocation(),
                ship.getBoundingBox().expandTowards(direction.scale(RANGE)).inflate(1),
                candidate -> candidate.isAlive() && candidate.isPickable() && !candidate.isSpectator()
                        && !(candidate instanceof LaserEntity) && !(candidate instanceof IHoloGuiEntity)
                        && !candidate.isPassengerOfSameVehicle(ship), start.distanceToSqr(block.getLocation()));
        return entity == null ? block.getLocation() : entity.getLocation();
    }

    public static Vec3 sightOnPanel(FluxShipEntity ship, float partialTick) {
        LivingEntity pilot = ship.getControllingPassenger();
        Vec3 eye = pilot == null ? cabinPoint(ship, new Vec3(0, eyeHeight(ship) - .5, 0), partialTick)
                : pilot.getEyePosition(partialTick);
        Vec3 sight = target(ship, partialTick).subtract(eye);
        Vec3 normal = direction(ship, partialTick);
        double distance = panelCenter(ship, partialTick).subtract(eye).dot(normal);
        double forwardDistance = sight.dot(normal);
        return Math.abs(forwardDistance) < 1.0E-8 ? panelCenter(ship, partialTick)
                : eye.add(sight.scale(distance / forwardDistance));
    }
}
