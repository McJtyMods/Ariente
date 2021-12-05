package mcjty.ariente.bindings;

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
//        fullHealth = Binding.create("key.fullhealth", Keyboard.KEY_O);


        // @todo 1.14
//        configureArmor = Binding.create("key.configurearmor", Keyboard.KEY_0, () -> toServer(new PacketConfigureArmor()));
//        armorHotkey1 = Binding.create("key.hotkey1", Keyboard.KEY_NUMPAD7, () -> toServer(new PacketArmorHotkey(1)));
//        armorHotkey2 = Binding.create("key.hotkey2", Keyboard.KEY_NUMPAD8, () -> toServer(new PacketArmorHotkey(2)));
//        armorHotkey3 = Binding.create("key.hotkey3", Keyboard.KEY_NONE, () -> toServer(new PacketArmorHotkey(3)));
//        armorHotkey4 = Binding.create("key.hotkey4", Keyboard.KEY_NONE, () -> toServer(new PacketArmorHotkey(4)));
//        flyForward = Binding.create("key.flyforward", Keyboard.KEY_W, () -> performFlyAction(FlyAction.FORWARD));
//        flyBackwards = Binding.create("key.flybackwards", Keyboard.KEY_S, () -> performFlyAction(FlyAction.BACKWARD));
//        turnLeft = Binding.create("key.turnleft", Keyboard.KEY_A, () -> performFlyAction(FlyAction.TURNLEFT));
//        turnRight = Binding.create("key.turnright", Keyboard.KEY_D, () -> performFlyAction(FlyAction.TURNRIGHT));
//        flyUp = Binding.create("key.flyup", Keyboard.KEY_UP, () -> performFlyAction(FlyAction.UP));
//        flyDown = Binding.create("key.flydown", Keyboard.KEY_DOWN, () -> performFlyAction(FlyAction.DOWN));
//        startFlying = Binding.create("key.startflying", Keyboard.KEY_SPACE, () -> performFlyAction(FlyAction.START));
//        startLanding = Binding.create("key.startlanding", Keyboard.KEY_BACK, () -> performFlyAction(FlyAction.LAND));
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
