package mcjty.ariente.bindings;


import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KeyInputHandler {

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            KeyBindings.tickShipControls();
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        Binding.checkBindings();
    }
}
