package mcjty.ariente.items;

import mcjty.ariente.entities.LaserEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** The held model and projectile share a muzzle and sightline, including nearby obstructions. */
public record LanceAim(Vec3 muzzle, Vec3 direction) {

    public static LanceAim calculate(LivingEntity shooter, Vec3 direction) {
        return calculate(shooter, shooter.getEyePosition(), direction, shooter.getYRot());
    }

    public static LanceAim calculate(LivingEntity shooter, Vec3 eye, Vec3 direction, float yaw) {
        var level = shooter.level();
        Vec3 aim = direction.normalize();
        HitResult target = level.clip(new ClipContext(eye, eye.add(aim.scale(64)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter));
        // A departing beam must not become the held lance's new aiming target.
        EntityHitResult entityTarget = ProjectileUtil.getEntityHitResult(shooter, eye, target.getLocation(),
                shooter.getBoundingBox().expandTowards(aim.scale(64)).inflate(1),
                candidate -> candidate.isAlive() && candidate.isPickable() && !candidate.isSpectator()
                        && !(candidate instanceof LaserEntity),
                eye.distanceToSqr(target.getLocation()));
        if (entityTarget != null) {
            target = entityTarget;
        }
        Vec3 right = new Vec3(-Mth.cos(yaw * Mth.DEG_TO_RAD), 0, -Mth.sin(yaw * Mth.DEG_TO_RAD));
        Vec3 up = right.cross(aim).normalize();
        double hand = shooter.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        double clearance = Math.min(1, eye.distanceTo(target.getLocation()) / 4);
        Vec3 muzzle = eye.add(aim.scale(1.98D * clearance)).add(right.scale(.25D * hand * clearance))
                .subtract(up.scale(.38D * clearance));
        HitResult obstruction = level.clip(new ClipContext(eye, muzzle, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter));
        if (obstruction.getType() != HitResult.Type.MISS) {
            muzzle = obstruction.getLocation().subtract(muzzle.subtract(eye).normalize().scale(.05D));
        }
        Vec3 shotDirection = target.getLocation().subtract(muzzle).normalize();
        return new LanceAim(muzzle, shotDirection.lengthSqr() < 1.0E-7D ? aim : shotDirection);
    }
}
