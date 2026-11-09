package mcjty.ariente.items;

import mcjty.ariente.entities.LaserEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
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
        Vec3 right = rightVector(aim, yaw);
        Vec3 up = right.cross(aim).normalize();
        double hand = shooter.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        double clearance = Math.min(1, eye.distanceTo(target.getLocation()) / 4);
        Vec3 muzzle = eye.add(aim.scale(1.98D * clearance)).add(right.scale(.25D * hand * clearance))
                .subtract(up.scale(.38D * clearance));
        HitResult obstruction = level.clip(new ClipContext(eye, muzzle, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter));
        if (obstruction.getType() != HitResult.Type.MISS) {
            muzzle = obstruction.getLocation().subtract(muzzle.subtract(eye).normalize().scale(.05D));
        }
        Vec3 sight = target.getLocation();
        if (target instanceof BlockHitResult block && target.getType() == HitResult.Type.BLOCK) {
            // Aim just inside the face so a converging, offset shot cannot skim past
            // an exact block edge because of floating-point rounding.
            sight = sight.subtract(Vec3.atLowerCornerOf(block.getDirection().getUnitVec3i()).scale(.001D));
        }
        Vec3 shotDirection = sight.subtract(muzzle).normalize();
        return new LanceAim(muzzle, shotDirection.lengthSqr() < 1.0E-7D ? aim : shotDirection);
    }

    public static Vec3 rightVector(Vec3 direction, float fallbackYaw) {
        Vec3 right = new Vec3(-direction.z, 0, direction.x);
        // A stationary mob's head can face away from its body. Keep "up" above the
        // sightline regardless of that body rotation, including immediately after spawning.
        return right.lengthSqr() < 1.0E-7D
                ? new Vec3(-Mth.cos(fallbackYaw * Mth.DEG_TO_RAD), 0, -Mth.sin(fallbackYaw * Mth.DEG_TO_RAD))
                : right.normalize();
    }
}
