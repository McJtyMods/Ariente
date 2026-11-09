package mcjty.ariente.bindings;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.entities.fluxship.FluxShipMouseControls;
import mcjty.ariente.entities.fluxship.PacketShipAction;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.entities.levitator.PacketLevitatorInput;
import mcjty.ariente.items.armor.PacketArmorHotkey;
import mcjty.ariente.items.armor.PacketConfigureArmor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mojang.blaze3d.platform.InputConstants;

public class KeyBindings {

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
    public static Binding startLanding;
    public static Binding fireShipLaser;
    public static Binding shipFreeLook;

    public static void init() {

        // @todo 1.14
        configureArmor = Binding.create("key.configurearmor", InputConstants.KEY_0, () -> toServer(new PacketConfigureArmor()));
        armorHotkey1 = Binding.create("key.hotkey1", InputConstants.KEY_NUMPAD7, () -> toServer(new PacketArmorHotkey(1)));
        armorHotkey2 = Binding.create("key.hotkey2", InputConstants.KEY_NUMPAD8, () -> toServer(new PacketArmorHotkey(2)));
        armorHotkey3 = Binding.create("key.hotkey3", InputConstants.UNKNOWN.getValue(), () -> toServer(new PacketArmorHotkey(3)));
        armorHotkey4 = Binding.create("key.hotkey4", InputConstants.UNKNOWN.getValue(), () -> toServer(new PacketArmorHotkey(4)));
        flyForward = Binding.createHeld("key.flyforward", InputConstants.KEY_W);
        flyBackwards = Binding.createHeld("key.flybackwards", InputConstants.KEY_S);
        turnLeft = Binding.createHeld("key.turnleft", InputConstants.KEY_A);
        turnRight = Binding.createHeld("key.turnright", InputConstants.KEY_D);
        flyUp = Binding.createHeld("key.flyup", InputConstants.KEY_UP);
        flyDown = Binding.createHeld("key.flydown", InputConstants.KEY_DOWN);
        startLanding = Binding.createHeld("key.startlanding", InputConstants.KEY_BACKSPACE);
        fireShipLaser = Binding.createHeld("key.ariente.ship_fire", InputConstants.KEY_SPACE);
        shipFreeLook = Binding.createHeld("key.ariente.ship_free_look", InputConstants.KEY_LALT);
    }

    public static void tickShipControls() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getVehicle() instanceof FluxShipEntity ship) {
            boolean active = mc.gui.screen() == null && !mc.isPaused() && mc.isWindowActive();
            int forward = active ? (flyForward.isDown() ? 1 : 0) - (flyBackwards.isDown() ? 1 : 0) : 0;
            int turn = active ? (turnRight.isDown() ? 1 : 0) - (turnLeft.isDown() ? 1 : 0) : 0;
            int vertical = active ? (flyUp.isDown() ? 1 : 0)
                    - (flyDown.isDown() || startLanding.isDown() ? 1 : 0) : 0;
            FluxShipMouseControls.Aim aim = FluxShipMouseControls.sample(ship, active, turn);
            toServer(new PacketShipAction(forward, turn, vertical, active && fireShipLaser.isDown(),
                    aim.flight(), aim.steering(), aim.yaw(), aim.pitch()));
        } else {
            FluxShipMouseControls.reset();
        }
    }

    public static void tickLevitatorControls() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getVehicle() instanceof FluxLevitatorEntity) {
            boolean active = mc.gui.screen() == null && !mc.isPaused();
            int input = active ? (mc.options.keyUp.isDown() ? 1 : 0) - (mc.options.keyDown.isDown() ? 1 : 0) : 0;
            toServer(new PacketLevitatorInput(input));
        }
    }

    private static void toServer(CustomPacketPayload packet) {
        ClientPacketDistributor.sendToServer(packet);
    }
}
