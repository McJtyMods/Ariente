package mcjty.ariente.bindings;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;

public class KeyInputHandler {

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        if (true) {
            KeyBindings.tickShipControls();
            KeyBindings.tickLevitatorControls();
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        Binding.checkBindings();
    }
}
