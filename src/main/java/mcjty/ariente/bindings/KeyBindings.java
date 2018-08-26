package mcjty.ariente.bindings;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class KeyBindings {

    public static KeyBinding fullHealth;
    public static KeyBinding configureArmor;
    public static KeyBinding armorHotkey1;
    public static KeyBinding armorHotkey2;
    public static KeyBinding armorHotkey3;
    public static KeyBinding armorHotkey4;

    public static void init() {
//        fullHealth = new KeyBinding("key.fullhealth", KeyConflictContext.IN_GAME, Keyboard.KEY_O, "key.categories.ariente");
//        ClientRegistry.registerKeyBinding(fullHealth);
        configureArmor = new KeyBinding("key.configurearmor", KeyConflictContext.IN_GAME, Keyboard.KEY_0, "key.categories.ariente");
        ClientRegistry.registerKeyBinding(configureArmor);
        armorHotkey1 = new KeyBinding("key.hotkey1", KeyConflictContext.IN_GAME, Keyboard.KEY_NUMPAD7, "key.categories.ariente");
        ClientRegistry.registerKeyBinding(armorHotkey1);
        armorHotkey2 = new KeyBinding("key.hotkey2", KeyConflictContext.IN_GAME, Keyboard.KEY_NUMPAD8, "key.categories.ariente");
        ClientRegistry.registerKeyBinding(armorHotkey2);
        armorHotkey3 = new KeyBinding("key.hotkey3", KeyConflictContext.IN_GAME, Keyboard.KEY_NONE, "key.categories.ariente");
        ClientRegistry.registerKeyBinding(armorHotkey3);
        armorHotkey4 = new KeyBinding("key.hotkey4", KeyConflictContext.IN_GAME, Keyboard.KEY_NONE, "key.categories.ariente");
        ClientRegistry.registerKeyBinding(armorHotkey4);
    }
}
