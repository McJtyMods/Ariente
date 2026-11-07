package mcjty.ariente.display;

import net.minecraft.world.phys.Vec3;

/** A world-upright orthographic basis, independent of the camera's mounting direction. */
public record CameraView(Vec3 right, Vec3 up, Vec3 direction) {

    public static CameraView of(double yawDegrees, int tiltDegrees) {
        double yaw = Math.toRadians(yawDegrees), tilt = Math.toRadians(tiltDegrees);
        return new CameraView(new Vec3(Math.cos(yaw), 0, -Math.sin(yaw)),
                new Vec3(-Math.sin(yaw) * Math.sin(tilt), Math.cos(tilt), -Math.cos(yaw) * Math.sin(tilt)),
                new Vec3(Math.sin(yaw) * Math.cos(tilt), Math.sin(tilt), Math.cos(yaw) * Math.cos(tilt)));
    }

    public float screenX(float x, float y, float z) {
        return (float) (x * right.x + y * right.y + z * right.z);
    }

    public float screenY(float x, float y, float z) {
        return (float) -(x * up.x + y * up.y + z * up.z);
    }

    public float depth(float x, float y, float z) {
        return (float) (x * direction.x + y * direction.y + z * direction.z);
    }
}
