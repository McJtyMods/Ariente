package mcjty.ariente.client;

import com.mojang.blaze3d.platform.InputConstants;
import mcjty.ariente.Ariente;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

/** Exercises configured movement keys through the real client-to-server control packet. */
public class FluxCartKeyboardChecks {

    private static InputConstants.Key forwardKey;
    private static InputConstants.Key backKey;
    private static int coastingSpeed;
    private static int turningSpeed;
    private static double startZ;

    public static boolean tick(int tick, FluxLevitatorEntity cart, Minecraft mc) {
        if (tick == 0) {
            require(cart.getSpeed() == 0, "Keyboard check must start with a stopped cart");
            forwardKey = mc.options.keyUp.getKey();
            backKey = mc.options.keyDown.getKey();
            mc.options.keyUp.setKey(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_I));
            mc.options.keyDown.setKey(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_K));
            KeyMapping.resetMapping();
            reposition(mc, new Vec3(.5, 200.0625, .5));
            face(mc, 0);
        } else if (tick == 10) {
            face(mc, 0);
            startZ = cart.getZ();
            mc.options.keyUp.setDown(true);
        } else if (tick == 30) {
            require(Math.abs(cart.getSpeed()) >= 10 && Math.abs(cart.getSpeed()) <= 25
                            && cart.getZ() > startZ + 1 && cart.getZ() > cart.zOld,
                    "Remapped Forward must gradually accelerate south when the rider faces south; speed=" + cart.getSpeed()
                            + "; position=" + cart.position() + "; startZ=" + startZ + "; oldZ=" + cart.zOld
                            + "; yaw=" + mc.player.getYRot());
            mc.options.keyUp.setDown(false);
        } else if (tick == 35) {
            coastingSpeed = cart.getSpeed();
        } else if (tick == 45) {
            require(cart.getSpeed() == coastingSpeed, "Releasing Forward must retain the selected speed");
            mc.options.keyDown.setDown(true);
        } else if (tick == 52) {
            require(Math.abs(cart.getSpeed()) > 0 && Math.abs(cart.getSpeed()) < Math.abs(coastingSpeed),
                    "Back must slow down gradually, not stop instantly");
        } else if (tick == 85) {
            require(cart.getSpeed() == 0, "Holding Back must stop without automatically reversing");
            mc.options.keyDown.setDown(false);
            face(mc, 180);
        } else if (tick == 95) {
            mc.options.keyUp.setDown(true);
        } else if (tick == 110) {
            require(Math.abs(cart.getSpeed()) >= 10 && Math.abs(cart.getSpeed()) <= 20 && cart.getZ() < cart.zOld,
                    "Forward must start north when the rider turns to face north");
            mc.options.keyUp.setDown(false);
        } else if (tick == 120) {
            coastingSpeed = cart.getSpeed();
            mc.options.keyDown.setDown(true);
        } else if (tick == 127) {
            require(Math.abs(cart.getSpeed()) > 0 && Math.abs(cart.getSpeed()) < Math.abs(coastingSpeed),
                    "Back must gradually slow reverse travel");
        } else if (tick == 160) {
            require(cart.getSpeed() == 0, "Reverse braking must stop at zero");
            mc.options.keyDown.setDown(false);
            face(mc, 0);
        } else if (tick == 170) {
            mc.options.keyUp.setDown(true);
        } else if (tick == 185) {
            require(cart.getZ() > cart.zOld && Math.abs(cart.getSpeed()) >= 10,
                    "Forward must restart towards the current facing");
            turningSpeed = Math.abs(cart.getSpeed());
            face(mc, 180);
        } else if (tick == 192) {
            require(Math.abs(cart.getSpeed()) > 0 && Math.abs(cart.getSpeed()) < turningSpeed
                            && cart.getZ() > cart.zOld,
                    "Turning around while holding Forward must first brake gradually");
        } else if (tick == 225) {
            require(Math.abs(cart.getSpeed()) >= 15 && cart.getZ() < cart.zOld,
                    "Holding Forward after turning around must pass through zero and reverse");
            mc.options.keyUp.setDown(false);
            mc.options.keyDown.setDown(true);
        } else if (tick == 270) {
            require(cart.getSpeed() == 0, "Back must stop the reversed cart");
            mc.options.keyDown.setDown(false);
            mc.setScreen(new ChatScreen(""));
            mc.options.keyUp.setDown(true);
        } else if (tick == 290) {
            require(cart.getSpeed() == 0, "Typing in chat must not accelerate the cart");
            mc.options.keyUp.setDown(false);
            mc.setScreen(null);
            mc.options.keyUp.setDown(true);
            mc.options.keyDown.setDown(true);
        } else if (tick == 310) {
            require(cart.getSpeed() == 0, "Pressing both movement keys must leave the throttle neutral");
            mc.options.keyUp.setDown(false);
            mc.options.keyDown.setDown(false);
            reposition(mc, new Vec3(8.75, 200.0625, .75));
            face(mc, -90);
        } else if (tick == 325) {
            require(cart.getSpeed() == 0, "Corner check must start from rest");
            mc.options.keyUp.setDown(true);
        } else if (tick == 350) {
            require(Math.abs(cart.getSpeed()) >= 15 && cart.getX() > 9 && cart.getX() > cart.xOld,
                    "Forward must restart a stopped bend towards the exit the rider faces; speed=" + cart.getSpeed()
                            + "; position=" + cart.position() + "; oldX=" + cart.xOld + "; yaw=" + mc.player.getYRot());
            mc.options.keyUp.setDown(false);
            mc.options.keyDown.setDown(true);
        } else if (tick == 390) {
            require(cart.getSpeed() == 0, "The bend restart must remain controllable");
            mc.options.keyDown.setDown(false);
            mc.options.keyUp.setKey(forwardKey);
            mc.options.keyDown.setKey(backKey);
            KeyMapping.resetMapping();
            Ariente.setup.getLogger().info("FLUX CART KEYBOARD CHECK PASSED: remapped keys, facing, gradual reversal, braking, coasting, chat and bend restart");
            return true;
        }
        return false;
    }

    private static void reposition(Minecraft mc, Vec3 position) {
        var playerId = mc.player.getUUID();
        var server = mc.getSingleplayerServer();
        server.execute(() -> {
            var player = server.getPlayerList().getPlayer(playerId);
            var cart = (FluxLevitatorEntity) player.getVehicle();
            cart.changeSpeed(0);
            cart.setDeltaMovement(Vec3.ZERO);
            cart.setPos(position);
            cart.positionRider(player);
        });
    }

    private static void face(Minecraft mc, float yaw) {
        mc.player.setYRot(yaw);
        mc.player.yRotO = yaw;
        mc.player.setXRot(0);
        mc.player.xRotO = 0;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
