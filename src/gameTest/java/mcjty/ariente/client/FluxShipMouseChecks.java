package mcjty.ariente.client;

import com.mojang.blaze3d.platform.InputConstants;
import mcjty.ariente.Ariente;
import mcjty.ariente.bindings.KeyBindings;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.entities.fluxship.FluxShipClient;
import mcjty.ariente.entities.fluxship.FluxShipMouseControls;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.ViewportEvent;
import org.lwjgl.glfw.GLFW;

/** Opt-in mouse-flight profile of the existing real keyboard/network/camera fixture. */
public class FluxShipMouseChecks {

    private static double altitude;
    private static float heading;
    private static float heldPitch;
    private static float cruiseSpeed;

    public static void tick(Minecraft mc, FluxShipEntity ship, int tick) {
        mc.setWindowActive(true);
        if (tick == 25) {
            require(UtilityConfiguration.FLUX_SHIP_MOUSE_CONTROL.get(), "Mouse flight must be enabled in this profile");
            altitude = ship.getY();
        } else if (tick == 30) {
            key(GLFW.GLFW_KEY_W, true);
        } else if (tick == 50) {
            mc.player.turn(35 / .15, -18 / .15);
        } else if (tick == 85) {
            require(Math.abs(Mth.wrapDegrees(ship.getYRot() - 35)) < 4 && ship.getXRot() < -15
                            && ship.getY() > altitude + 5 && ship.getSpeed() > 90 && ship.getSpeed() < 104.1,
                    "Vanilla mouse turn must steer, climb and bank the actual server ship: yaw=" + ship.getYRot()
                            + ", pitch=" + ship.getXRot() + ", speed=" + ship.getSpeed());
            var angles = new ViewportEvent.ComputeCameraAngles(mc.gameRenderer, mc.gameRenderer.mainCamera(), 1,
                    mc.player.getYRot(), mc.player.getXRot(), 0);
            FluxShipClient.cameraAngles(angles);
            require(angles.getPitch() == mc.player.getXRot(), "Mouse flight must not double the hull pitch in the camera");
            screenshot(mc, "ship-mouse-climb");
            heading = ship.getYRot();
            key(GLFW.GLFW_KEY_D, true);
        } else if (tick == 110) {
            require(ship.getYRot() > heading + 25 && ship.getBank(1) > 5,
                    "D must add a sustained banked turn alongside mouse steering");
            key(GLFW.GLFW_KEY_D, false);
        } else if (tick == 130) {
            heading = ship.getYRot();
            heldPitch = ship.getXRot();
            key(GLFW.GLFW_KEY_LEFT_ALT, true);
        } else if (tick == 133) {
            mc.player.turn(90 / .15, 25 / .15);
        } else if (tick == 160) {
            require(Math.abs(Mth.wrapDegrees(ship.getYRot() - heading)) < 6 && Math.abs(ship.getXRot() - heldPitch) < 2,
                    "Alt free look must keep the ship's heading and flight pitch");
            require(FluxShipClient.flightHint().startsWith("FREE LOOK"), "The cockpit must identify active free look");
            screenshot(mc, "ship-mouse-free-look");
            key(GLFW.GLFW_KEY_LEFT_ALT, false);
        } else if (tick == 165) {
            require(Math.abs(Mth.wrapDegrees(mc.player.getYRot() - ship.getYRot())) < 3
                            && Math.abs(mc.player.getXRot() - ship.getXRot()) < 2,
                    "Releasing free look must recenter without steering toward the sideways view");
        } else if (tick == 170) {
            mc.player.turn(-20 / .15, 38 / .15);
        } else if (tick == 205) {
            require(ship.getXRot() > 15 && ship.getY() - ship.yo < -.5,
                    "Looking down must pitch the ship into a real descending run: pitch=" + ship.getXRot());
            screenshot(mc, "ship-mouse-dive");
            heading = ship.getYRot();
            cruiseSpeed = ship.getSpeed();
            key(GLFW.GLFW_KEY_W, false);
            mc.gui.setScreen(new InventoryScreen(mc.player));
        } else if (tick == 210) {
            mc.player.turn(90 / .15, 0);
        } else if (tick == 230) {
            require(ship.getSpeed() > 0 && ship.getSpeed() < cruiseSpeed * .4
                            && Math.abs(Mth.wrapDegrees(ship.getYRot() - heading)) < 5,
                    "Opening a screen must stop steering and brake smoothly");
            mc.gui.setScreen(null);
        } else if (tick == 231) {
            // A modifier's release can be lost while the game is unfocused.
            key(GLFW.GLFW_KEY_LEFT_ALT, true);
            mc.setWindowActive(false);
            KeyBindings.tickShipControls();
            require(!KeyBindings.shipFreeLook.isDown(), "Losing focus must release stale free-look input");
            mc.setWindowActive(true);
            var aim = FluxShipMouseControls.sample(ship, true, 0);
            require(aim.steering() && !FluxShipClient.flightHint().startsWith("FREE LOOK"),
                    "Returning to the game must restore mouse steering without another Alt press");
            mc.player.turn(0, (-25 - mc.player.getXRot()) / .15);
        } else if (tick == 280) {
            require(ship.getXRot() < -23, "Mouse pitch must reach the server ship after recovering focus: " + ship.getXRot());
        } else if (tick == 380) {
            require(ship.getSpeed() == 0, "Releasing W must eventually settle into a hover");
            Ariente.setup.getLogger().info("FLUX SHIP MOUSE CHECKS PASSED: real mouse turns, keyboard assist, climb/dive, bounded speed, camera pitch, Alt free look, focus recovery, screen gating and smooth braking");
            mc.stop();
        }
    }

    private static void key(int code, boolean down) {
        KeyMapping.set(InputConstants.Type.KEYSYM.getOrCreate(code), down);
    }

    private static void screenshot(Minecraft mc, String name) {
        try (var image = Screenshot.takeScreenshot(mc.gameRenderer.mainRenderTarget())) {
            image.writeToFile(mc.gameDirectory.toPath().resolve(name + ".png"));
        } catch (Exception error) {
            throw new IllegalStateException(error);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
