package mcjty.ariente.entities.fluxship;

import mcjty.ariente.Ariente;
import mcjty.ariente.bindings.KeyBindings;
import mcjty.ariente.config.UtilityConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public class FluxShipClient {

    @SubscribeEvent
    public static void cameraAngles(ViewportEvent.ComputeCameraAngles event) {
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
        return KeyBindings.flyForward.getKeyName() + " / " + KeyBindings.flyBackwards.getKeyName() + "  Forward / reverse";
    }

    public static String turnKeys() {
        return KeyBindings.turnLeft.getKeyName() + " / " + KeyBindings.turnRight.getKeyName() + "  Turn left / right";
    }

    public static String heightKeys() {
        return KeyBindings.flyUp.getKeyName() + " / " + KeyBindings.flyDown.getKeyName() + "  Up / down";
    }

    public static String alternateHeightKeys() {
        return KeyBindings.startFlying.getKeyName() + " / " + KeyBindings.startLanding.getKeyName() + "  Rise / descend";
    }

    public static String dismountKey() {
        return Minecraft.getInstance().options.keyShift.getTranslatedKeyMessage().getString() + "  Dismount";
    }
}
