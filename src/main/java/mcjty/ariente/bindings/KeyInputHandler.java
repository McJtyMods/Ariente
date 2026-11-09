package mcjty.ariente.bindings;


public class KeyInputHandler {

    public void onClientTick() {
        if (true) {
            KeyBindings.tickShipControls();
            KeyBindings.tickLevitatorControls();
        }
    }

    public void onKeyInput() {
        Binding.checkBindings();
    }
}
