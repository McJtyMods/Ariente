package mcjty.ariente.entities.fluxship;

import mcjty.ariente.bindings.KeyBindings;
import mcjty.ariente.client.CameraAngles;
import mcjty.ariente.config.UtilityConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FluxShipClient {

    public static void cameraAngles(CameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.options.getCameraType().isFirstPerson() || event.getCamera().getEntity() != mc.player
                || mc.player == null || !(mc.player.getVehicle() instanceof FluxShipEntity ship)) {
            return;
        }
        float partialTick = (float) event.getPartialTick();
        float strength = UtilityConfiguration.FLUX_SHIP_CAMERA_TILT.get().floatValue();
        if (strength == 0) {
            return;
        }
        float heading = ship.getFlightYaw(partialTick);
        float relativeYaw = Mth.wrapDegrees(event.getYaw() - heading);
        if (UtilityConfiguration.FLUX_SHIP_MOUSE_CONTROL.get()) {
            // Mouse look is the world-space flight aim. Do not add the hull pitch to it a second time.
            event.setRoll(event.getRoll() + ship.getBank(partialTick) * strength * Mth.cos(relativeYaw * Mth.DEG_TO_RAD));
            return;
        }
        // Compose the cabin and free-look rotations exactly. Adding Euler angles
        // separately causes the cockpit to wander when pitching and banking together.
        Quaternionf rotation = new Quaternionf().rotationY(-heading * Mth.DEG_TO_RAD)
                .rotateX(ship.getFlightPitch(partialTick) * strength * Mth.DEG_TO_RAD)
                .rotateZ(ship.getBank(partialTick) * strength * Mth.DEG_TO_RAD)
                .rotateY(-relativeYaw * Mth.DEG_TO_RAD)
                .rotateX(event.getPitch() * Mth.DEG_TO_RAD);
        Vector3f angles = rotation.getEulerAnglesYXZ(new Vector3f());
        event.setYaw(event.getYaw() + Mth.wrapDegrees(-angles.y * Mth.RAD_TO_DEG - event.getYaw()));
        event.setPitch(angles.x * Mth.RAD_TO_DEG);
        event.setRoll(event.getRoll() + angles.z * Mth.RAD_TO_DEG);
    }

    public static String movementKeys() {
        return KeyBindings.flyForward.getKeyName() + "/" + KeyBindings.flyBackwards.getKeyName() + " Move";
    }

    public static String turnKeys() {
        return (UtilityConfiguration.FLUX_SHIP_MOUSE_CONTROL.get() ? "Mouse / " : "")
                + KeyBindings.turnLeft.getKeyName() + "/" + KeyBindings.turnRight.getKeyName() + " Turn";
    }

    public static String heightKeys() {
        return KeyBindings.flyUp.getKeyName() + "/" + KeyBindings.flyDown.getKeyName();
    }

    public static String flightHint() {
        if (!UtilityConfiguration.FLUX_SHIP_MOUSE_CONTROL.get()) {
            return heightKeys() + " Rise/descend";
        }
        if (FluxShipMouseControls.isLookingAround()) {
            return "FREE LOOK | Release " + KeyBindings.shipFreeLook.getKeyName() + " to steer";
        }
        return KeyBindings.flyForward.getKeyName() + ": fly toward mouse | " + KeyBindings.shipFreeLook.getKeyName() + " Look around";
    }

    public static String dismountKey() {
        return Minecraft.getInstance().options.keyShift.getTranslatedKeyMessage().getString() + "  Dismount";
    }

    public static String firingKey() {
        return KeyBindings.fireShipLaser.getKeyName() + " Hold fire | Aim with hull";
    }
}
