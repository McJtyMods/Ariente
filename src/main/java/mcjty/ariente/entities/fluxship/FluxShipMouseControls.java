package mcjty.ariente.entities.fluxship;

import mcjty.ariente.bindings.KeyBindings;
import mcjty.ariente.config.UtilityConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

/** Vanilla mouse sensitivity and inversion already apply to the player's look direction. */
public class FluxShipMouseControls {

    public record Aim(boolean flight, boolean steering, float yaw, float pitch) {
    }

    private static FluxShipEntity currentShip;
    private static boolean lookingAround;
    private static float heldPitch;

    public static Aim sample(FluxShipEntity ship, boolean active, int keyboardTurn) {
        Minecraft mc = Minecraft.getInstance();
        if (!UtilityConfiguration.FLUX_SHIP_MOUSE_CONTROL.get()) {
            reset();
            return new Aim(false, false, 0, 0);
        }
        if (currentShip != ship) {
            reset();
            currentShip = ship;
            recenter(ship);
        }
        boolean freeLook = !active || KeyBindings.shipFreeLook.isDown();
        ship.setLocalMouseControls(true, freeLook || keyboardTurn != 0);
        if (freeLook) {
            if (!lookingAround) {
                heldPitch = ship.getXRot();
            }
            lookingAround = true;
            return new Aim(true, false, ship.getYRot(), heldPitch);
        }
        if (lookingAround) {
            recenter(ship);
            lookingAround = false;
        }
        heldPitch = Mth.clamp(mc.player.getXRot(), -55, 55);
        mc.player.setXRot(heldPitch);
        return new Aim(true, true, mc.player.getYRot(), heldPitch);
    }

    private static void recenter(FluxShipEntity ship) {
        var player = Minecraft.getInstance().player;
        player.setYRot(ship.getYRot());
        player.yRotO = player.getYRot();
        player.setXRot(ship.getXRot());
        player.xRotO = player.getXRot();
        heldPitch = player.getXRot();
    }

    public static void reset() {
        if (currentShip != null) {
            currentShip.setLocalMouseControls(false, true);
        }
        currentShip = null;
        lookingAround = false;
    }
}
