package mcjty.ariente.bindings;

import mcjty.ariente.items.armor.PacketConfigureArmor;
import mcjty.ariente.items.armor.PacketArmorHotkey;
import org.lwjgl.glfw.GLFW;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.entities.fluxship.FlyAction;
import mcjty.ariente.entities.fluxship.PacketShipAction;
import mcjty.ariente.network.ArienteMessages;
import net.minecraft.client.Minecraft;

public class KeyBindings {

    public static Binding fullHealth;
    public static Binding configureArmor;
    public static Binding armorHotkey1;
    public static Binding armorHotkey2;
    public static Binding armorHotkey3;
    public static Binding armorHotkey4;
    public static Binding flyForward;
    public static Binding flyBackwards;
    public static Binding turnLeft;
    public static Binding turnRight;
    public static Binding flyUp;
    public static Binding flyDown;
    public static Binding startFlying;
    public static Binding startLanding;

    public static void init() {



        // @todo 1.14
        configureArmor = Binding.create("key.configurearmor", GLFW.GLFW_KEY_0, () -> toServer(new PacketConfigureArmor()));
        armorHotkey1 = Binding.create("key.hotkey1", GLFW.GLFW_KEY_KP_7, () -> toServer(new PacketArmorHotkey(1)));
        armorHotkey2 = Binding.create("key.hotkey2", GLFW.GLFW_KEY_KP_8, () -> toServer(new PacketArmorHotkey(2)));
        armorHotkey3 = Binding.create("key.hotkey3", GLFW.GLFW_KEY_UNKNOWN, () -> toServer(new PacketArmorHotkey(3)));
        armorHotkey4 = Binding.create("key.hotkey4", GLFW.GLFW_KEY_UNKNOWN, () -> toServer(new PacketArmorHotkey(4)));
        flyForward = Binding.create("key.flyforward", GLFW.GLFW_KEY_W, () -> performFlyAction(FlyAction.FORWARD));
        flyBackwards = Binding.create("key.flybackwards", GLFW.GLFW_KEY_S, () -> performFlyAction(FlyAction.BACKWARD));
        turnLeft = Binding.create("key.turnleft", GLFW.GLFW_KEY_A, () -> performFlyAction(FlyAction.TURNLEFT));
        turnRight = Binding.create("key.turnright", GLFW.GLFW_KEY_D, () -> performFlyAction(FlyAction.TURNRIGHT));
        flyUp = Binding.create("key.flyup", GLFW.GLFW_KEY_UP, () -> performFlyAction(FlyAction.UP));
        flyDown = Binding.create("key.flydown", GLFW.GLFW_KEY_DOWN, () -> performFlyAction(FlyAction.DOWN));
        startFlying = Binding.create("key.startflying", GLFW.GLFW_KEY_SPACE, () -> performFlyAction(FlyAction.START));
        startLanding = Binding.create("key.startlanding", GLFW.GLFW_KEY_BACKSPACE, () -> performFlyAction(FlyAction.LAND));
    }

    private static void performFlyAction(FlyAction forward) {
        // First check if we're actually on a ship
        if (Minecraft.getInstance().player.getVehicle() instanceof FluxShipEntity) {
            toServer(new PacketShipAction(forward));
        }
    }

    private static void toServer(Object packet) {
        ArienteMessages.INSTANCE.sendToServer(packet);
    }
}
