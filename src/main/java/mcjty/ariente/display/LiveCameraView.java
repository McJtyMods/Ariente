package mcjty.ariente.display;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Pan and tilt are relative to the mounted lens. Zero always looks away from its base. */
public record LiveCameraView(Vec3 eye, Vec3 forward, float yaw, float pitch) {

    public Matrix4f rotation() {
        // Minecraft's camera looks along +Z at yaw 0; OpenGL's viewing direction is -Z.
        return new Matrix4f().rotateX((float) Math.toRadians(pitch)).rotateY((float) Math.toRadians(yaw + 180));
    }

    public static LiveCameraView of(BlockPos pos, Direction facing, double panDegrees, double tiltDegrees) {
        Vec3 normal = Vec3.atLowerCornerOf(facing.getNormal());
        Vec3 up = facing.getAxis() == Direction.Axis.Y
                ? new Vec3(0, 0, facing == Direction.UP ? -1 : 1) : new Vec3(0, 1, 0);
        Vec3 right = normal.cross(up);
        double pan = Math.toRadians(panDegrees), tilt = Math.toRadians(tiltDegrees);
        Vec3 horizontal = normal.scale(Math.cos(pan)).add(right.scale(Math.sin(pan)));
        Vec3 forward = horizontal.scale(Math.cos(tilt)).add(up.scale(Math.sin(tilt))).normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-forward.x, forward.z));
        float pitch = (float) -Math.toDegrees(Math.asin(Math.max(-1, Math.min(1, forward.y))));
        // The glass cube ends 1/16 block beyond the block center; keep the eye just outside it.
        return new LiveCameraView(Vec3.atCenterOf(pos).add(normal.scale(.125)), forward, yaw, pitch);
    }
}
