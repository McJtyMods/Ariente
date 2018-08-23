package mcjty.ariente.bindings;

import mcjty.ariente.items.armor.PacketArmorHotkey;
import mcjty.ariente.items.armor.PacketConfigureArmor;
import mcjty.ariente.network.ArienteMessages;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class KeyInputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
//        if (KeyBindings.fullHealth.isPressed()) {
//            ArienteMessages.INSTANCE.sendToServer(new PacketFullHealth());
        if (KeyBindings.configureArmor.isPressed()) {
            ArienteMessages.INSTANCE.sendToServer(new PacketConfigureArmor());
        } else if (KeyBindings.armorHotkey1.isPressed()) {
            ArienteMessages.INSTANCE.sendToServer(new PacketArmorHotkey(1));
        } else if (KeyBindings.armorHotkey2.isPressed()) {
            ArienteMessages.INSTANCE.sendToServer(new PacketArmorHotkey(2));
        } else if (KeyBindings.armorHotkey3.isPressed()) {
            ArienteMessages.INSTANCE.sendToServer(new PacketArmorHotkey(3));
        } else if (KeyBindings.armorHotkey4.isPressed()) {
            ArienteMessages.INSTANCE.sendToServer(new PacketArmorHotkey(4));
        }
    }
}
