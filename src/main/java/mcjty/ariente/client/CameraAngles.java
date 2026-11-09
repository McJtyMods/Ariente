package mcjty.ariente.client;

import net.minecraft.client.Camera;

public final class CameraAngles {

    private final Camera camera;
    private final float partialTick;
    private float yaw;
    private float pitch;
    private float roll;

    public CameraAngles(Camera camera, float partialTick) {
        this.camera = camera;
        this.partialTick = partialTick;
        yaw = camera.yRot();
        pitch = camera.xRot();
    }

    public Camera getCamera() { return camera; }

    public float getPartialTick() { return partialTick; }

    public float getYaw() { return yaw; }

    public float getPitch() { return pitch; }

    public float getRoll() { return roll; }

    public void setYaw(float yaw) { this.yaw = yaw; }

    public void setPitch(float pitch) { this.pitch = pitch; }

    public void setRoll(float roll) { this.roll = roll; }
}
